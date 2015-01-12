package org.terasology.logic.behavior.core;

import org.terasology.logic.behavior.core.compiler.MethodGenerator;

/**
 * Created by synopia on 11.01.2015.
 */
public class RunningNode extends LeafNode {
    @Override
    public BehaviorNode deepCopy() {
        return new RunningNode();
    }

    @Override
    public void construct(Actor actor) {

    }

    @Override
    public BehaviorState execute(Actor actor) {
        return BehaviorState.RUNNING;
    }

    @Override
    public void destruct(Actor actor) {

    }

    @Override
    public void assembleExecute(MethodGenerator gen) {
        gen.push(BehaviorState.RUNNING.ordinal());
    }

}
