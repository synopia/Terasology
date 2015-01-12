package org.terasology.logic.behavior.core;

import com.google.common.collect.Lists;
import org.terasology.logic.behavior.core.compiler.ClassGenerator;
import org.terasology.logic.behavior.core.compiler.MethodGenerator;

import java.util.List;

/**
 * Created by synopia on 11.01.2015.
 */
public abstract class CompositeNode implements BehaviorNode {
    protected final List<BehaviorNode> children = Lists.newArrayList();

    @Override
    public <T> T visit(T item, Visitor<T> visitor) {
        T childItem = visitor.visit(item, this);
        for (BehaviorNode child : children) {
            child.visit(childItem, visitor);
        }
        return childItem;
    }

    @Override
    public void insertChild(int index, BehaviorNode child) {
        children.add(index, child);
    }

    @Override
    public void replaceChild(int index, BehaviorNode child) {
        if (index < children.size()) {
            children.add(index, child);
        } else {
            children.set(index, child);
        }
    }

    @Override
    public BehaviorNode removeChild(int index) {
        return children.remove(index);
    }

    @Override
    public BehaviorNode getChild(int index) {
        return children.get(index);
    }

    @Override
    public int getChildrenCount() {
        return children.size();
    }

    @Override
    public int getMaxChildren() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void assembleSetup(ClassGenerator gen) {
        for (BehaviorNode child : children) {
            child.assembleSetup(gen);
        }
    }

    @Override
    public void assembleTeardown(ClassGenerator gen) {
        for (BehaviorNode child : children) {
            child.assembleTeardown(gen);
        }
    }

    public List<BehaviorNode> getChildren() {
        return children;
    }

    @Override
    public void assembleConstruct(MethodGenerator gen) {

    }

    @Override
    public void assembleExecute(MethodGenerator gen) {

    }

    @Override
    public void assembleDestruct(MethodGenerator gen) {

    }

    @Override
    public void construct(Actor actor) {

    }

    @Override
    public BehaviorState execute(Actor actor) {
        return null;
    }

    @Override
    public void destruct(Actor actor) {

    }
}
