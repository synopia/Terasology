package org.terasology.logic.behavior.core.compiler;

import com.google.common.collect.Maps;
import org.terasology.logic.behavior.core.Action;
import org.terasology.logic.behavior.core.ActionNode;
import org.terasology.logic.behavior.core.Actor;
import org.terasology.logic.behavior.core.BehaviorNode;
import org.terasology.logic.behavior.core.BehaviorState;
import org.terasology.logic.behavior.core.BehaviorTreeRunner;
import org.terasology.logic.behavior.core.CompositeNode;

import java.util.Map;

/**
 * Created by synopia on 11.01.2015.
 */
public abstract class CompiledBehaviorTree implements BehaviorTreeRunner {
    private Map<Integer, Action> actionMap = Maps.newHashMap();
    private BehaviorState result = BehaviorState.UNDEFINED;
    public Actor actor;

    @Override
    public void setActor(Actor actor) {
        this.actor = actor;
    }

    @Override
    public Actor getActor() {
        return actor;
    }

    public void bind(BehaviorNode node) {
        if (node instanceof ActionNode) {
            ActionNode actionNode = (ActionNode) node;
            actionMap.put(actionNode.getAction().getId(), actionNode.getAction());
        } else if (node instanceof CompositeNode) {
            CompositeNode compositeNode = (CompositeNode) node;
            for (BehaviorNode behaviorNode : compositeNode.getChildren()) {
                bind(behaviorNode);
            }
        }
    }

    public abstract int run(int state);

    @Override
    public BehaviorState step() {
        result = BehaviorState.values()[run(result.ordinal())];
        return result;
    }

    public void setAction(int id, Action action) {
        actionMap.put(id, action);
    }

    public Action getAction(int id) {
        return actionMap.get(id);
    }

    public Map<Integer, Action> getActionMap() {
        return actionMap;
    }
}
