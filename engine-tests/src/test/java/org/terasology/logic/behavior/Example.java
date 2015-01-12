package org.terasology.logic.behavior;

import org.terasology.logic.behavior.core.Actor;
import org.terasology.logic.behavior.core.BaseAction;
import org.terasology.logic.behavior.core.BehaviorNode;
import org.terasology.logic.behavior.core.BehaviorState;
import org.terasology.logic.behavior.core.BehaviorTreeBuilder;
import org.terasology.logic.behavior.core.DefaultActor;
import org.terasology.logic.behavior.core.DefaultBehaviorTree;
import org.terasology.logic.behavior.core.compiler.Assembler;

/**
 * Created by synopia on 12.01.2015.
 */
public class Example {
    public static class Print extends BaseAction {
        private String msg;

        @Override
        public BehaviorState modify(Actor actor, BehaviorState result) {
            System.out.print(msg);
            return BehaviorState.SUCCESS;
        }
    }

    public static class Delay extends BaseAction<DefaultActor<?>> {
        private float duration;

        @Override
        public void construct(DefaultActor<?> actor) {
            actor.setValue(getId(), duration);
        }

        @Override
        public BehaviorState modify(DefaultActor<?> actor, BehaviorState result) {
            System.out.print(".");
            float timeRemaining = actor.getValue(getId());
            timeRemaining -= actor.getDelta();
            actor.setValue(getId(), timeRemaining);
            return timeRemaining >= 0 ? BehaviorState.RUNNING : BehaviorState.SUCCESS;
        }

        @Override
        public void destruct(DefaultActor<?> actor) {

        }
    }

    public static class Repeat extends BaseAction<DefaultActor<?>> {
        private int count;

        @Override
        public void construct(DefaultActor<?> actor) {
            actor.setValue(getId(), count);
        }

        @Override
        public BehaviorState modify(DefaultActor<?> actor, BehaviorState result) {
            if (result == BehaviorState.SUCCESS) {
                int remaining = actor.getValue(getId());
                remaining--;
                actor.setValue(getId(), remaining);
                return remaining > 0 ? BehaviorState.RUNNING : BehaviorState.SUCCESS;
            }
            return result;
        }
    }

    public static void main(String[] args) {
        BehaviorTreeBuilder treeBuilder = new BehaviorTreeBuilder();

        BehaviorNode node = treeBuilder.fromJson("{ sequence:[ success, success, failure ] }");
        System.out.println(new DefaultBehaviorTree(node, null).step());
        System.out.println(new Assembler("Test", node).createInstance(null).step());

        treeBuilder.registerAction("print", Print.class);
        node = treeBuilder.fromJson("{ sequence:[ success, { print:{msg:world} } ] }");
        System.out.println(new DefaultBehaviorTree(node, null).step());
        System.out.println(new Assembler("Test", node).createInstance(null).step());

        treeBuilder.registerAction("delay", Delay.class);
        DefaultActor actor = new DefaultActor();
        actor.setDelta(0.1f);
        node = treeBuilder.fromJson("{ sequence:[ success, { delay:{duration:1}}, { print:{msg:Hello} }, { delay:{duration:1}}, { print:{msg:World} } ] }");
        DefaultBehaviorTree tree = new DefaultBehaviorTree(node, actor);
        for (int i = 0; i < 100; i++) {
            tree.step();
        }

        System.out.println();
        treeBuilder.registerDecorator("repeat", Repeat.class);
        actor = new DefaultActor();
        actor.setDelta(0.1f);
        node = treeBuilder.fromJson("{ sequence:[ {repeat :{ count:5, child:{print:{msg:x}}}}, success, { delay:{duration:1}}, { print:{msg:Hello} }, { delay:{duration:1}}, { print:{msg:World} } ] }");
        tree = new DefaultBehaviorTree(node, actor);
        for (int i = 0; i < 100; i++) {
            tree.step();
        }

    }
}
