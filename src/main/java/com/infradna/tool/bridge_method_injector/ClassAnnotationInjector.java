package com.infradna.tool.bridge_method_injector;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

/**
 * Adds class annotations.
 *
 * @author Kohsuke Kawaguchi
 */
abstract class ClassAnnotationInjector extends ClassAdapter {
    ClassAnnotationInjector(ClassVisitor cv) {
        super(cv);
    }

    private boolean emitted = false;

    private void maybeEmit() {
        if (!emitted) {
            emitted = true;
            emit();
        }
    }

    protected abstract void emit();

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        maybeEmit();
        super.visitInnerClass(name, outerName, innerName, access);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        maybeEmit();
        return super.visitField(access, name, desc, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        maybeEmit();
        return super.visitMethod(access, name, desc, signature, exceptions);
    }

    @Override
    public void visitEnd() {
        maybeEmit();
        super.visitEnd();
    }
}
