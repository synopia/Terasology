package org.terasology.logic.behavior.core;

import org.terasology.logic.behavior.core.compiler.MethodGenerator;

/**
 * Created by synopia on 11.01.2015.
 */
public class FailureNode extends LeafNode {
    @Override
    public String getName() {
        return "failure";
    }

    @Override
    public BehaviorNode deepCopy() {
        return new FailureNode();
    }

    @Override
    public void construct(Actor actor) {

    }

    @Override
    public BehaviorState execute(Actor actor) {
        return BehaviorState.FAILURE;
    }

    @Override
    public void destruct(Actor actor) {

    }

    @Override
    public void assembleExecute(MethodGenerator gen) {
        gen.push(BehaviorState.FAILURE.ordinal());
    }
}
