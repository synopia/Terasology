package org.terasology.logic.behavior.core;

/**
 * Created by synopia on 11.01.2015.
 */
public interface Action<A extends Actor> {
    int getId();

    void setId(int id);

    void construct(A actor);

    boolean prune(A actor);

    BehaviorState modify(A actor, BehaviorState result);

    void destruct(A actor);
}
