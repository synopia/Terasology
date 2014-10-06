/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.logic.behavior.tree;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.synopia.behavior.Assembler;
import org.synopia.behavior.BehaviorNode;
import org.synopia.behavior.commands.Dispatcher;
import org.synopia.behavior.tree.BehaviorAction;
import org.synopia.behavior.tree.CompiledBehaviorTree;
import org.synopia.behavior.tree.DebugCallback;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.module.sandbox.API;

/**
 * An interpreter evaluates a behavior tree. This is done by creating tasks for an actor for the nodes of the BT.
 * If a task returns RUNNING, the task is placed to the active list and asked next tick again.
 * Finished nodes may create new tasks, which are placed to the active list.
 * <p/>
 *
 * @author synopia
 */
@API
public class Interpreter {
    private static final Logger logger = LoggerFactory.getLogger(Interpreter.class);

    private Actor actor;
    private CompiledBehaviorTree compiledBehaviorTree;
    private Assembler assembler;
    private Dispatcher dispatcher;
    private DebugCallback debugCallback = DebugCallback.NULL;

    public Interpreter(Actor actor) {
        this.actor = actor;
        debugCallback = new DebugCallback() {
            @Override
            public void push(int i, BehaviorAction behaviorAction, String s, int i1) {
                logger.info(i + " " + behaviorAction + " s " + i1);
            }

            @Override
            public void pop(int i) {

            }
        };
    }

    public void setDebugger(Object o) {

    }

    public Actor actor() {
        return actor;
    }

    public void reset() {
        compiledBehaviorTree = assembler.createInstance();
    }

    public void tick(float delta) {
        dispatcher.run();
    }

    public void start(BehaviorNode root) {
        assembler = new Assembler();
        assembler.assemble(root);
        reset();
        dispatcher = new Dispatcher(compiledBehaviorTree);
        compiledBehaviorTree.debugCallback = debugCallback;

    }

    public String toString() {
        return actor.component(DisplayNameComponent.class).name;
    }

    public void pause() {

    }
}
