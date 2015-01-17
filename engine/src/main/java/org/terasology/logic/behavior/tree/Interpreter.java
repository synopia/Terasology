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
import org.terasology.logic.behavior.asset.BehaviorTree;
import org.terasology.logic.behavior.core.BehaviorTreeRunner;
import org.terasology.logic.behavior.core.DefaultBehaviorTree;
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

    private EntityActor actor;
    private BehaviorTreeRunner treeRunner;
    private BehaviorTree tree;

    public Interpreter(EntityActor actor) {
        this.actor = actor;
    }

    public void setDebugger(Object o) {

    }

    public EntityActor actor() {
        return actor;
    }

    public void reset() {
        treeRunner = null;
    }

    public void tick(float delta) {
        actor.setDelta(delta);
        if (treeRunner == null) {
            treeRunner = new DefaultBehaviorTree(tree.getRoot(), actor);
//        Assembler assembler = new Assembler("Test", tree.getRoot());
//        treeRunner = assembler.createInstance(actor);
        }
        treeRunner.step();
    }

    public void run() {
    }

    public void setTree(BehaviorTree tree) {
        this.tree = tree;
        reset();
    }

    public String toString() {
        return actor.component(DisplayNameComponent.class).name;
    }

    public void pause() {

    }

    public BehaviorTree getTree() {
        return tree;
    }
}
