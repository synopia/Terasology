package org.terasology.logic.behavior.core;

/**
 * Created by synopia on 12.01.2015.
 */
public interface BehaviorTreeRunner {
    BehaviorState step();

    void setActor(Actor actor);

    Actor getActor();
}
