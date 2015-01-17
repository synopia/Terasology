package org.terasology.logic.behavior.core;

/**
 * Created by synopia on 11.01.2015.
 */
public interface BehaviorNode extends AssemblingBehaviorNode {
    String getName();

    void insertChild(int index, BehaviorNode child);

    void replaceChild(int index, BehaviorNode child);

    BehaviorNode removeChild(int index);

    BehaviorNode getChild(int index);

    int getChildrenCount();

    int getMaxChildren();

    void construct(Actor actor);

    BehaviorState execute(Actor actor);

    void destruct(Actor actor);

    BehaviorNode deepCopy();

    <T> T visit(T item, Visitor<T> visitor);
}
