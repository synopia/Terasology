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

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.synopia.behavior.Assembler;
import org.synopia.behavior.BehaviorNode;
import org.synopia.behavior.GlobalVariable;
import org.synopia.behavior.Visitor;
import org.synopia.behavior.nodes.ActionNode;
import org.synopia.behavior.tree.BehaviorAction;
import org.synopia.behavior.tree.BehaviorState;
import org.synopia.behavior.tree.Callback;
import org.synopia.behavior.tree.CompiledBehaviorTree;
import org.synopia.behavior.tree.Context;
import org.synopia.behavior.tree.DebugCallback;
import org.terasology.engine.SimpleUri;
import org.terasology.entitySystem.event.Event;
import org.terasology.logic.behavior.asset.BehaviorTree;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.module.sandbox.API;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.metadata.ClassMetadata;
import org.terasology.reflection.metadata.DefaultClassMetadata;
import org.terasology.reflection.metadata.FieldMetadata;
import org.terasology.reflection.reflect.ReflectFactory;
import org.terasology.reflection.reflect.ReflectionReflectFactory;

import java.util.List;
import java.util.Map;

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
    private BehaviorTree tree;
    private CompiledBehaviorTree compiledBehaviorTree;
    private Assembler assembler;
    private DebugCallback debugCallback = DebugCallback.NULL;
    private final CopyStrategyLibrary library;
    private final ReflectFactory reflectFactory;

    public Interpreter(Actor actor) {
        this.actor = actor;
        reflectFactory = new ReflectionReflectFactory();
        library = new CopyStrategyLibrary(reflectFactory);

        debugCallback = new DebugCallback() {
            @Override
            public void push(int i, BehaviorAction behaviorAction, String s, int i1) {
//                logger.info(i + " " + behaviorAction + " s " + i1);
            }

            @Override
            public void pop(int i) {

            }
        };
    }

    public void removeDebugger() {
        setDebugger(DebugCallback.NULL);
    }

    public void setDebugger(DebugCallback callback) {
        compiledBehaviorTree.debugCallback = callback;
    }

    public Actor actor() {
        return actor;
    }

    public void reset() {
        compiledBehaviorTree = assembler.createInstance();
    }

    public void tick(float delta) {
        compiledBehaviorTree.run();
    }

    public void setTree(BehaviorTree tree) {
        this.tree = tree;
        BehaviorNode root = tree.getRoot();
        assembler = new Assembler("pkg.Foo");
        assembler.assemble(root);
        final Map<Integer, BehaviorNode> nodes = Maps.newHashMap();
        final Map<Integer, ClassMetadata> meta = Maps.newHashMap();

        root.visit(null, new Visitor() {
            @Override
            public Object visit(Object o, BehaviorNode behaviorNode) {
                nodes.put(behaviorNode.getId(), behaviorNode);
                if (behaviorNode instanceof ActionNode) {
                    ActionNode actionNode = (ActionNode) behaviorNode;
                    String typeName = actionNode.getCommand();
                    try {
                        Class<? extends Event> type = (Class<? extends Event>) Class.forName(typeName);
                        DefaultClassMetadata<? extends Event> metadata = new DefaultClassMetadata<>(new SimpleUri(), type, reflectFactory, library);
                        meta.put(behaviorNode.getId(), metadata);
                    } catch (ClassNotFoundException e) {
                        logger.warn("Cannot load class " + typeName);
                    } catch (NoSuchMethodException e) {
                        logger.warn("Cannot access class " + typeName);
                    }
                }
                return null;
            }
        });
        reset();
        compiledBehaviorTree.callback = new Callback() {
            @Override
            public BehaviorState execute(int i, BehaviorAction behaviorAction, String s) {
                Context context = compiledBehaviorTree.context;

                if (behaviorAction == BehaviorAction.EXECUTE) {
                    context.rewind();
                    BehaviorNode node = nodes.get(i);
                    if (node instanceof ActionNode) {
                        ActionNode actionNode = (ActionNode) node;
                        ClassMetadata classMetadata = meta.get(i);
                        Event event = (Event) classMetadata.newInstance();
                        List<GlobalVariable> locals = actionNode.getLocals();
                        logger.info("send(" + s);

                        for (GlobalVariable local : locals) {
                            Object value = local.read(context);
                            logger.info(local.getName() + ":" + local.typeName() + " = " + value);
                            FieldMetadata field = classMetadata.getField(local.getName());
                            if (field != null) {
                                field.setValue(event, value);
                            }
                        }
                        logger.info(") -> " + event);

                        actor.minion().send(event);
                    }
                }
                return BehaviorState.SUCCESS;
            }
        };
        compiledBehaviorTree.debugCallback = debugCallback;
    }

    public String toString() {
        return actor.component(DisplayNameComponent.class).name;
    }

    public BehaviorTree getTree() {
        return tree;
    }

    public void pause() {

    }
}
