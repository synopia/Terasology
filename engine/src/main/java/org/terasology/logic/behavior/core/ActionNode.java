package org.terasology.logic.behavior.core;

import org.terasology.logic.behavior.core.compiler.ClassGenerator;
import org.terasology.logic.behavior.core.compiler.MethodGenerator;

/**
 * Created by synopia on 11.01.2015.
 */
public class ActionNode implements BehaviorNode {
    protected Action action;

    public ActionNode() {
    }

    public ActionNode(Action action) {
        this.action = action;
    }

    public Action getAction() {
        return action;
    }

    @Override
    public String getName() {
        return action != null ? action.getName() : "action";
    }

    @Override
    public <T> T visit(T item, Visitor<T> visitor) {
        return visitor.visit(item, this);
    }

    @Override
    public void construct(Actor actor) {
        if (action != null) {
            action.construct(actor);
        }
    }

    @Override
    public BehaviorState execute(Actor actor) {
        if (action != null) {
            return action.modify(actor, BehaviorState.UNDEFINED);
        }
        return BehaviorState.UNDEFINED;
    }

    @Override
    public void destruct(Actor actor) {
        if (action != null) {
            action.destruct(actor);
        }
    }

    @Override
    public BehaviorNode deepCopy() {
        return new ActionNode(action);
    }

    @Override
    public void insertChild(int index, BehaviorNode child) {
        throw new IllegalArgumentException("ActionNodes cant have any children");
    }

    @Override
    public void replaceChild(int index, BehaviorNode child) {
        throw new IllegalArgumentException("ActionNodes cant have any children");
    }

    @Override
    public BehaviorNode removeChild(int index) {
        throw new IllegalArgumentException("ActionNodes cant have any children");
    }

    @Override
    public BehaviorNode getChild(int index) {
        throw new IllegalArgumentException("ActionNodes cant have any children");
    }

    @Override
    public int getChildrenCount() {
        return 0;
    }

    @Override
    public int getMaxChildren() {
        return 0;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    @Override
    public void assembleSetup(ClassGenerator gen) {

    }

    @Override
    public void assembleTeardown(ClassGenerator gen) {

    }

    @Override
    public void assembleConstruct(MethodGenerator gen) {
        gen.invokeAction(action.getId(), "void construct(org.terasology.logic.behavior.core.Actor)");
    }

    @Override
    public void assembleExecute(MethodGenerator gen) {
        gen.invokeAction(action.getId(), "org.terasology.logic.behavior.core.BehaviorState modify(org.terasology.logic.behavior.core.Actor, org.terasology.logic.behavior.core.BehaviorState)", BehaviorState.RUNNING);
    }

    @Override
    public void assembleDestruct(MethodGenerator gen) {
        gen.invokeAction(action.getId(), "void destruct(org.terasology.logic.behavior.core.Actor)");
    }
}
