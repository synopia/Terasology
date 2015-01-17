package org.terasology.logic.behavior.core;

import org.terasology.logic.behavior.core.compiler.ClassGenerator;
import org.terasology.logic.behavior.core.compiler.MethodGenerator;

/**
 * Created by synopia on 11.01.2015.
 */
public class DelegateNode implements BehaviorNode {
    private final BehaviorNode delegate;

    public DelegateNode(BehaviorNode delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public BehaviorNode deepCopy() {
        return new DelegateNode(delegate.deepCopy());
    }

    @Override
    public <T> T visit(T item, Visitor<T> visitor) {
        return delegate.visit(item, visitor);
    }

    @Override
    public void insertChild(int index, BehaviorNode child) {
        delegate.insertChild(index, child);
    }

    @Override
    public void replaceChild(int index, BehaviorNode child) {
        delegate.replaceChild(index, child);
    }

    @Override
    public BehaviorNode removeChild(int index) {
        return delegate.removeChild(index);
    }

    @Override
    public BehaviorNode getChild(int index) {
        return delegate.getChild(index);
    }

    @Override
    public int getChildrenCount() {
        return delegate.getChildrenCount();
    }

    @Override
    public int getMaxChildren() {
        return delegate.getMaxChildren();
    }

    @Override
    public void assembleSetup(ClassGenerator gen) {
        delegate.assembleSetup(gen);
    }

    @Override
    public void assembleTeardown(ClassGenerator gen) {
        delegate.assembleTeardown(gen);
    }

    @Override
    public void assembleConstruct(MethodGenerator gen) {
        delegate.assembleConstruct(gen);
    }

    @Override
    public void assembleExecute(MethodGenerator gen) {
        delegate.assembleExecute(gen);
    }

    @Override
    public void assembleDestruct(MethodGenerator gen) {
        delegate.assembleDestruct(gen);
    }

    @Override
    public void construct(Actor actor) {
        delegate.construct(actor);
    }

    @Override
    public BehaviorState execute(Actor actor) {
        return delegate.execute(actor);
    }

    @Override
    public void destruct(Actor actor) {
        delegate.construct(actor);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}
