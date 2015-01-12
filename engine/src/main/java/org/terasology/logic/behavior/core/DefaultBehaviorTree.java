package org.terasology.logic.behavior.core;

/**
 * Created by synopia on 12.01.2015.
 */
public class DefaultBehaviorTree implements BehaviorTreeRunner {
    private final BehaviorNode root;
    private Actor actor;
    private BehaviorState state = BehaviorState.UNDEFINED;

    public DefaultBehaviorTree(BehaviorNode root, Actor actor) {
        this.root = root.deepCopy();
        this.actor = actor;
    }

    @Override
    public BehaviorState step() {
        if (state != BehaviorState.RUNNING) {
            root.construct(actor);
        }
        state = root.execute(actor);
        if (state != BehaviorState.RUNNING) {
            root.destruct(actor);
        }

        return state;
    }

    @Override
    public Actor getActor() {
        return actor;
    }

    @Override
    public void setActor(Actor actor) {
        this.actor = actor;
    }
}
