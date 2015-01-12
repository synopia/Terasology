package org.terasology.logic.behavior.core.compiler;

import org.terasology.logic.behavior.core.Action;
import org.terasology.logic.behavior.core.Actor;
import org.terasology.logic.behavior.core.BehaviorState;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

/**
 * Created by synopia on 11.01.2015.
 */
public class MethodGenerator extends GeneratorAdapter {
    private ClassGenerator classGen;

    public MethodGenerator(ClassGenerator classGen, int api, MethodVisitor mv, int access, String name, String desc) {
        super(api, mv, access, name, desc);
        this.classGen = classGen;
    }

    public void loadField(String name) {
        getField(classGen.getType(), name, classGen.getFieldType(name));
    }

    public void storeField(String name) {
        putField(classGen.getType(), name, classGen.getFieldType(name));
    }

    public void invokeAction(int id, String method) {
        invokeAction(id, method, null);
    }

    public void invokeAction(int id, String method, BehaviorState state) {
        Method m = Method.getMethod("org.terasology.logic.behavior.core.Action getAction(int)");
        Method callback = Method.getMethod(method);
        loadThis();
        push(id);
        invokeVirtual(classGen.getType(), m);

        loadThis();
        getField(Type.getType(CompiledBehaviorTree.class), "actor", Type.getType(Actor.class));

        if (state != null) {
            Method values = Method.getMethod("org.terasology.logic.behavior.core.BehaviorState[] values()");
            invokeStatic(Type.getType(BehaviorState.class), values);
            push(state.ordinal());
            arrayLoad(Type.getType(BehaviorState.class));
        }

        invokeInterface(Type.getType(Action.class), callback);

        if (state != null) {
            Method ordinal = Method.getMethod("int ordinal()");
            invokeVirtual(Type.getType(BehaviorState.class), ordinal);
        }
    }
}
