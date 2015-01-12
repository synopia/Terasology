package org.terasology.logic.behavior.core.compiler;

import com.google.common.collect.Maps;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;

import java.util.Map;

/**
 * Created by synopia on 11.01.2015.
 */
public class ClassGenerator extends ClassVisitor {
    private int fieldCount;
    private Map<String, Type> fieldTypes = Maps.newHashMap();

    private Type type;

    public ClassGenerator(ClassVisitor cv, String className) {
        super(Opcodes.ASM5, cv);

        type = Type.getType(className.replace("\\.", "/"));
        visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, type.toString(), null, Type.getInternalName(CompiledBehaviorTree.class), null);
    }

    public void generateMethod(String method, Consumer<MethodGenerator> closure) {
        Method m = Method.getMethod(method);
        MethodVisitor v = visitMethod(Opcodes.ACC_PUBLIC, m.getName(), m.getDescriptor(), null, null);
        MethodGenerator gen = new MethodGenerator(this, Opcodes.ASM5, v, Opcodes.ACC_PUBLIC, m.getName(), m.getDescriptor());
        closure.accept(gen);

        gen.endMethod();
    }

    public String generateField(Type type) {
        String name = "field" + fieldCount;
        fieldCount++;
        fieldTypes.put(name, type);

        visitField(Opcodes.ACC_PUBLIC, name, type.getDescriptor(), null, null);
        return name;
    }

    public Type getType() {
        return type;
    }

    public Type getFieldType(String name) {
        return fieldTypes.get(name);
    }
}
