/*
 * The MIT License
 *
 * Copyright (c) 2010, InfraDNA, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.infradna.tool.bridge_method_injector;

import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;
import static org.objectweb.asm.Opcodes.ACC_ABSTRACT;
import static org.objectweb.asm.Opcodes.ACC_BRIDGE;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.POP2;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

/**
 * Injects bridge methods as per {@link WithBridgeMethods}.
 *
 * @author Kohsuke Kawaguchi
 */
public class MethodInjector {
    public void handleRecursively(File f) throws IOException {
        if (f.isDirectory()) {
            for (File c : f.listFiles())
                handleRecursively(c);
        } else if (f.getName().endsWith(".class"))
            handle(f);
    }

    public void handle(File classFile) throws IOException {
        FileInputStream in = new FileInputStream(classFile);
        byte[] image;
        try {
            ClassReader cr = new ClassReader(new BufferedInputStream(in));
            /*
                working around JENKINS-22525 by not passing in 'cr'

                Javac as of 6u31 seems to produce duplicate constant pool entries in some cases.
                When such a class is transformed through ClassReader -> ClassWriter, it creates
                a class file where reference sites to those duplicate entries get reshuffled
                (for example, say #15 and #30 are the duplicate entries, and after a transformation
                some references that used to use #15 now refers to #30, and vice versa.)

                This is because ClassVisitor interfaces passes around strings, and
                ClassWriter looks up its constant pool to assign its index.

                For whatever reasons, this triggers "incompatible InnerClasses attribute" problem
                in IBM J9VM (observed with R26_Java726_SR7_20140409_1418_B195732), while
                it is happy with the original class file.

                It appears that this rare situation of having duplicate entries in constant pools
                and have various references use them in a certain way induces this J9 VM bug.

                To work around this problem, we make ASM rebuild the constant pool from scratch,
                which totally eliminates any duplicate entries.
             */
            ClassWriter cw = new ClassWriter(/*cr,*/COMPUTE_MAXS);
            cr.accept(new Transformer(new ClassAnnotationInjectorImpl(cw)),0);
            image = cw.toByteArray();
        } catch (AlreadyUpToDate _) {
            // no need to process this class. it's already up-to-date.
            return;
        } catch (IOException e) {
            throw (IOException)new IOException("Failed to process "+classFile).initCause(e);
        } catch (RuntimeException e) {
            throw (IOException)new IOException("Failed to process "+classFile).initCause(e);
        } finally {
            in.close();
        }

        // write it back
        FileOutputStream out = new FileOutputStream(classFile);
        out.write(image);
        out.close();
    }

    /**
     * Thrown to indicate that there's no need to re-process this class file.
     */
    class AlreadyUpToDate extends RuntimeException {
      private static final long serialVersionUID = 1L;
    }

    static class ClassAnnotationInjectorImpl extends ClassAnnotationInjector {
        ClassAnnotationInjectorImpl(ClassVisitor cv) {
            super(cv);
        }

        @Override
        protected void emit() {
            AnnotationVisitor av = cv.visitAnnotation(SYNTHETIC_METHODS_ADDED, false);
            av.visitEnd();
        }
    }

    private static class WithBridgeMethodsAnnotationVisitor extends AnnotationVisitor {
      protected boolean castRequired = false;
      protected String adapterMethod = null;
      protected List<Type> types = new ArrayList<Type>();
      
      public WithBridgeMethodsAnnotationVisitor(AnnotationVisitor av) {
        super(Opcodes.ASM5, av);
      }

      @Override
      public AnnotationVisitor visitArray(String name) {
        return new AnnotationVisitor(Opcodes.ASM5, super.visitArray(name)) {
           
            public void visit(String name, Object value) {
                if (value instanceof Type) {
                  // assume this is a member of the array of classes named "value" in WithBridgeMethods
                  types.add((Type) value);
                }
                super.visit(name, value);
            }

        };
      }

      @Override
      public void visit(String name, Object value) {
        if ("castRequired".equals(name) && value instanceof Boolean) {
          castRequired = (Boolean) value;
        }
        if ("adapterMethod".equals(name) && value instanceof String) {
          adapterMethod = (String) value;
        }
        super.visit(name, value);
      }
    }

    class Transformer extends ClassVisitor {
        private String internalClassName;
        /**
         * Synthetic methods to be generated.
         */
        private final List<SyntheticMethod> syntheticMethods = new ArrayList<SyntheticMethod>();

        class SyntheticMethod {
            final int access;
            final String name;
            final String desc;
            final String originalSignature;
            final String[] exceptions;
            final boolean castRequired;
            final String adapterMethod;

            /**
             * Return type of the bridge method to be inserted.
             */
            final Type returnType;
            /**
             * Return type of the declared method written in the source code.
             */
            final Type originalReturnType;

            SyntheticMethod(int access, String name, String desc, String originalSignature, String[] exceptions, Type returnType, boolean castRequired, String adapterMethod) {
                this.access = access;
                this.name = name;
                this.desc = desc;
                this.originalSignature = originalSignature;
                this.exceptions = exceptions;
                this.returnType = returnType;
                this.castRequired = castRequired;
                this.adapterMethod = adapterMethod;
                originalReturnType = Type.getReturnType(desc);
            }

            /**
             * Injects a synthetic method and send it to cv.
             */
            public void inject(ClassVisitor cv) {
                Type[] paramTypes = Type.getArgumentTypes(desc);

                int access = this.access | ACC_SYNTHETIC | ACC_BRIDGE;
                String methodDescriptor = Type.getMethodDescriptor(returnType, paramTypes);
                MethodVisitor mv = cv.visitMethod(access, name,
                        methodDescriptor, null/*TODO:is this really correct?*/, exceptions);
                if ((access&ACC_ABSTRACT)==0) {
                    GeneratorAdapter ga = new GeneratorAdapter(mv, access, name, methodDescriptor);
                    mv.visitCode();

                    int sz = 0;

                    if (hasAdapterMethod()) {
                        // the LHS of the adapter method invocation
                        ga.loadThis();
                        sz++;
                    }

                    boolean isStatic = (access & ACC_STATIC) != 0;
                    if (!isStatic) {
                        ga.loadThis();
                        sz++;
                    }

                    for (Type p : paramTypes) {
                        mv.visitVarInsn(p.getOpcode(ILOAD), sz);
                        sz += p.getSize();
                    }
                    mv.visitMethodInsn(
                      isStatic ? INVOKESTATIC : INVOKEVIRTUAL,internalClassName,name,desc);
                    if (hasAdapterMethod()) {
                        insertAdapterMethod(ga);
                    } else
                    if (castRequired) {
                        ga.unbox(returnType);
                    } else {
                        ga.box(originalReturnType);
                    }
                    if (returnType.equals(Type.VOID_TYPE) || returnType.getClassName().equals("java.lang.Void")) {
                        // bridge to void, which means disregard the return value from the original method
                        switch (originalReturnType.getSize()) {
                        case 1:
                            mv.visitInsn(POP);
                            break;
                        case 2:
                            mv.visitInsn(POP2);
                            break;
                        default:
                            throw new AssertionError("Unexpected operand size: "+originalReturnType);
                        }
                    }
                    mv.visitInsn(returnType.getOpcode(IRETURN));
                    mv.visitMaxs(sz,0);
                }
                mv.visitEnd();
            }

            private boolean hasAdapterMethod() {
                return adapterMethod!=null && adapterMethod.length()>0;
            }

            private void insertAdapterMethod(GeneratorAdapter ga) {
                ga.push(returnType);
                ga.visitMethodInsn(INVOKEVIRTUAL, internalClassName, adapterMethod,
                        Type.getMethodDescriptor(
                                Type.getType(Object.class), // return type
                                originalReturnType,
                                Type.getType(Class.class)
                        ));
                ga.unbox(returnType);
            }
        }

        Transformer(ClassVisitor cv) {
            super(Opcodes.ASM5, cv);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            this.internalClassName = name;
            super.visit(version, access, name, signature, superName, interfaces);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            if (desc.equals(SYNTHETIC_METHODS_ADDED))
                throw new AlreadyUpToDate();    // no need to process this class
            return super.visitAnnotation(desc, visible);
        }

        /**
         * Look for methods annotated with {@link WithBridgeMethods}.
         */
        @Override
        public MethodVisitor visitMethod(final int access, final String name, final String mdesc, final String signature, final String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, mdesc, signature, exceptions);
            return new MethodVisitor(Opcodes.ASM5, mv) {
                @Override
                public AnnotationVisitor visitAnnotation(String adesc, boolean visible) {
                    AnnotationVisitor av = super.visitAnnotation(adesc, visible);
                    if (adesc.equals(WITH_SYNTHETIC_METHODS) && (access & ACC_SYNTHETIC) == 0)
                        return new WithBridgeMethodsAnnotationVisitor(av) {
                        
                            @Override
                            public void visitEnd() {
                                super.visitEnd(); 
                                for (Type type : this.types)
                                    syntheticMethods.add(new SyntheticMethod(
                                         access,name,mdesc,signature,exceptions,type, this.castRequired, this.adapterMethod
                                    ));
                            }
                        };
                    return av;
                }
            };
        }

        /**
         * Inject methods at the end.
         */
        @Override
        public void visitEnd() {
            for (SyntheticMethod m : syntheticMethods)
                m.inject(cv);
            super.visitEnd();
        }
    }

    public static void main(String[] args) throws IOException {
        MethodInjector mi = new MethodInjector();
        for (String a : args) {
            mi.handleRecursively(new File(a));
        }
    }

    private static final String SYNTHETIC_METHODS_ADDED = Type.getDescriptor(BridgeMethodsAdded.class);
    private static final String WITH_SYNTHETIC_METHODS = Type.getDescriptor(WithBridgeMethods.class);
}
