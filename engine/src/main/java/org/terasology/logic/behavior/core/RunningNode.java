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

import org.terasology.logic.behavior.core.compiler.MethodGenerator;

/**
 * Runs always.
 */
public class RunningNode extends LeafNode {
    @Override
    public String getName() {
        return "running";
    }

    @Override
    public BehaviorNode deepCopy() {
        return new RunningNode();
    }

    @Override
    public void construct(Actor actor) {

    }

    @Override
    public BehaviorState execute(Actor actor) {
        return BehaviorState.RUNNING;
    }

    @Override
    public void destruct(Actor actor) {

    }

    @Override
    public void assembleExecute(MethodGenerator gen) {
        gen.push(BehaviorState.RUNNING.ordinal());
    }

}