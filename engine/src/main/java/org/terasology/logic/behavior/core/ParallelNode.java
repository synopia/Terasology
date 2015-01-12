package org.terasology.logic.behavior.core;

import org.terasology.logic.behavior.core.compiler.MethodGenerator;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import static org.objectweb.asm.commons.GeneratorAdapter.EQ;
import static org.objectweb.asm.commons.GeneratorAdapter.NE;

/**
 * Created by synopia on 11.01.2015.
 */
public class ParallelNode extends CompositeNode {
    @Override
    public BehaviorNode deepCopy() {
        ParallelNode result = new ParallelNode();
        for (BehaviorNode child : children) {
            result.children.add(child.deepCopy());
        }
        return result;
    }

    @Override
    public void construct(Actor actor) {
        for (BehaviorNode child : children) {
            child.construct(actor);
        }
    }

    @Override
    public BehaviorState execute(Actor actor) {
        int successCounter = 0;
        for (int i = 0; i < children.size(); i++) {
            BehaviorNode child = children.get(i);
            BehaviorState result = child.execute(actor);
            if (result == BehaviorState.FAILURE) {
                return BehaviorState.FAILURE;
            }
            if (result == BehaviorState.SUCCESS) {
                successCounter++;
            }
        }
        return successCounter == children.size() ? BehaviorState.SUCCESS : BehaviorState.RUNNING;
    }

    @Override
    public void destruct(Actor actor) {
        for (BehaviorNode child : children) {
            child.destruct(actor);
        }
    }

    @Override
    public void assembleConstruct(MethodGenerator gen) {
        for (BehaviorNode child : children) {
            child.assembleConstruct(gen);
        }
    }

    @Override
    public void assembleExecute(MethodGenerator gen) {
        Label exit = gen.newLabel();

        int successCounter = gen.newLocal(Type.INT_TYPE);
        gen.push(0);
        gen.storeLocal(successCounter);
        for (int i = 0; i < children.size(); i++) {
            BehaviorNode child = children.get(i);

            child.assembleExecute(gen);

            gen.dup();
            gen.push(BehaviorState.FAILURE.ordinal());
            gen.ifICmp(EQ, exit);

            Label skip = gen.newLabel();
            gen.push(BehaviorState.SUCCESS.ordinal());
            gen.ifICmp(NE, skip);

            gen.loadLocal(successCounter);
            gen.push(1);
            gen.visitInsn(Opcodes.IADD);
            gen.storeLocal(successCounter);

            gen.mark(skip);
        }
        gen.push(BehaviorState.RUNNING.ordinal());

        gen.loadLocal(successCounter);
        gen.push(children.size());
        gen.ifICmp(NE, exit);

        gen.pop();
        gen.push(BehaviorState.SUCCESS.ordinal());

        gen.mark(exit);
    }

    @Override
    public void assembleDestruct(MethodGenerator gen) {
        for (BehaviorNode child : children) {
            child.assembleDestruct(gen);
        }
    }

}
