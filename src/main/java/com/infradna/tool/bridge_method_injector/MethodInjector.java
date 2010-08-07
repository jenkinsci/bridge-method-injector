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

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.ClassWriter.*;
import static org.objectweb.asm.Opcodes.*;

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
            ClassWriter cw = new ClassWriter(cr, COMPUTE_FRAMES | COMPUTE_MAXS);
            cr.accept(new Transformer(new ClassAnnotationInjectorImpl(cw)),0);
            image = cw.toByteArray();
        } catch (AlreadyUpToDate _) {
            // no need to process this class. it's already up-to-date.
            return;
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
    class AlreadyUpToDate extends RuntimeException {}

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

    class Transformer extends ClassAdapter {
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

            final Type returnType;

            SyntheticMethod(int access, String name, String desc, String originalSignature, String[] exceptions, Type returnType) {
                this.access = access;
                this.name = name;
                this.desc = desc;
                this.originalSignature = originalSignature;
                this.exceptions = exceptions;
                this.returnType = returnType;
            }

            /**
             * Injects a synthetic method and send it to cv.
             */
            public void inject(ClassVisitor cv) {
                Type[] paramTypes = Type.getArgumentTypes(desc);

                MethodVisitor mv = cv.visitMethod(access | ACC_SYNTHETIC | ACC_BRIDGE, name,
                        Type.getMethodDescriptor(returnType, paramTypes), null/*TODO:is this really correct?*/, exceptions);
                mv.visitCode();
                int sz = 1;
                mv.visitVarInsn(ALOAD,0);
                for (Type p : paramTypes) {
                    mv.visitVarInsn(p.getOpcode(ILOAD), sz);
                    sz += p.getSize();
                }
                mv.visitMethodInsn(INVOKEVIRTUAL,internalClassName,name,desc);
                mv.visitInsn(returnType.getOpcode(IRETURN));
                mv.visitMaxs(sz,0);
                mv.visitEnd();
            }
        }

        Transformer(ClassVisitor cv) {
            super(cv);
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
            return new MethodAdapter(mv) {
                @Override
                public AnnotationVisitor visitAnnotation(String adesc, boolean visible) {
                    final AnnotationVisitor av = super.visitAnnotation(adesc, visible);
                    if (adesc.equals(WITH_SYNTHETIC_METHODS))
                        return new AnnotationNode(adesc) {
                            @Override
                            public void visitEnd() {
                                super.visitEnd();
                                // forward this annotation to the receiver
                                accept(av);
                                for (Type t : (List<Type>)values.get(1))
                                    syntheticMethods.add(new SyntheticMethod(
                                         access,name,mdesc,signature,exceptions,t
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
