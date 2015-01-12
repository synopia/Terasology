package org.terasology.logic.behavior.core;

/**
 * Created by synopia on 11.01.2015.
 */
public class DecoratorNode extends ActionNode {
    private BehaviorNode child;

    @Override
    public BehaviorNode deepCopy() {
        DecoratorNode node = new DecoratorNode();
        node.setAction(action);
        node.child = child;
        return node;
    }

    @Override
    public void construct(Actor actor) {
        if (action != null) {
            action.construct(actor);
        }
        child.construct(actor);
    }

    @Override
    public BehaviorState execute(Actor actor) {
        if (action != null) {
            if (action.prune(actor)) {
                return action.modify(actor, BehaviorState.UNDEFINED);
            }
            return action.modify(actor, child.execute(actor));
        }

        return child.execute(actor);
    }

    @Override
    public void destruct(Actor actor) {
        if (action != null) {
            action.destruct(actor);
        }
        child.destruct(actor);
    }

    @Override
    public void insertChild(int index, BehaviorNode child) {
        replaceChild(index, child);
    }

    @Override
    public void replaceChild(int index, BehaviorNode child) {
        if (index != 0) {
            throw new IllegalArgumentException("Decorator accepts only one child!");
        }
        this.child = child;
    }

    @Override
    public BehaviorNode removeChild(int index) {
        if (index != 0) {
            throw new IllegalArgumentException("Decorator accepts only one child!");
        }
        return child;
    }

    @Override
    public BehaviorNode getChild(int index) {
        if (index != 0) {
            throw new IllegalArgumentException("Decorator accepts only one child!");
        }
        return child;
    }

    @Override
    public int getChildrenCount() {
        return child != null ? 1 : 0;
    }

    @Override
    public int getMaxChildren() {
        return 1;
    }
}
