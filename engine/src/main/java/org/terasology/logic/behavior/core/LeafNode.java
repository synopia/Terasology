/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.logic.behavior.core;

import org.terasology.logic.behavior.core.compiler.ClassGenerator;
import org.terasology.logic.behavior.core.compiler.MethodGenerator;
import org.terasology.rendering.nui.properties.PropertyProvider;

/**
 * Node without children.
 */
public abstract class LeafNode implements BehaviorNode {

    @Override
    public PropertyProvider<?> getProperties() {
        return null;
    }

    @Override
    public <T> T visit(T item, Visitor<T> visitor) {
        return visitor.visit(item, this);
    }

    @Override
    public void insertChild(int index, BehaviorNode child) {
        throw new IllegalArgumentException("Leaf nodes does not accept children");
    }

    @Override
    public void replaceChild(int index, BehaviorNode child) {
        throw new IllegalArgumentException("Leaf nodes does not accept children");
    }

    @Override
    public BehaviorNode removeChild(int index) {
        throw new IllegalArgumentException("Leaf nodes does not accept children");
    }

    @Override
    public BehaviorNode getChild(int index) {
        throw new IllegalArgumentException("Leaf nodes does not accept children");
    }

    @Override
    public int getChildrenCount() {
        return 0;
    }

    @Override
    public int getMaxChildren() {
        return 0;
    }

    @Override
    public void assembleSetup(ClassGenerator gen) {

    }

    @Override
    public void assembleTeardown(ClassGenerator gen) {

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