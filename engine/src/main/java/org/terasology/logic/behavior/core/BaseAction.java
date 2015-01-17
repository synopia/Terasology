package org.terasology.logic.behavior.core;


import org.terasology.logic.behavior.TreeName;

/**
 * Created by synopia on 12.01.2015.
 */
public abstract class BaseAction<T extends Actor> implements Action<T> {
    private int id;

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return getClass().getAnnotation(TreeName.class).value();
    }

    @Override
    public void construct(T actor) {
    }

    @Override
    public boolean prune(T actor) {
        return false;
    }

    @Override
    public BehaviorState modify(T actor, BehaviorState result) {
        return BehaviorState.UNDEFINED;
    }

    @Override
    public void destruct(T actor) {
    }
}
