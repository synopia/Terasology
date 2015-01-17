package org.terasology.logic.behavior.core;

import com.google.common.collect.Maps;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.terasology.registry.InjectionHelper;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * Created by synopia on 11.01.2015.
 */
public class BehaviorTreeBuilder implements JsonDeserializer<BehaviorNode> {
    private Map<String, Class<? extends Action>> actions = Maps.newHashMap();
    private Map<String, Class<? extends Action>> decorators = Maps.newHashMap();

    private int nextId = 1;

    public BehaviorNode fromJson(String json) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(BehaviorNode.class, this);
        return gsonBuilder.create().fromJson(json, BehaviorNode.class);
    }

    public void registerAction(String name, Class<? extends Action> action) {
        actions.put(name, action);
    }

    public void registerDecorator(String name, Class<? extends Action> action) {
        decorators.put(name, action);
    }

    @Override
    public BehaviorNode deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        BehaviorNode node;
        if (json.isJsonPrimitive()) {
            node = getPrimitiveNode(json, context);
        } else {
            node = getCompositeNode(json, context);
        }
        node = createNode(node);
        return node;
    }

    public BehaviorNode createNode(BehaviorNode node) {
        return node;
    }

    private BehaviorNode getPrimitiveNode(JsonElement json, JsonDeserializationContext context) {
        String type = json.getAsString();
        BehaviorNode node = createNode(type);
        if (actions.containsKey(type)) {
            Action action = context.deserialize(new JsonObject(), actions.get(type));
            addAction((ActionNode) node, action);
        }
        return node;
    }

    private void addAction(ActionNode node, Action action) {
        action.setId(nextId);
        nextId++;
        node.setAction(action);
        InjectionHelper.inject(action);
    }

    private BehaviorNode getCompositeNode(JsonElement json, JsonDeserializationContext context) {
        String type;
        JsonObject obj = json.getAsJsonObject();
        Map.Entry<String, JsonElement> entry = obj.entrySet().iterator().next();
        type = entry.getKey();
        json = entry.getValue();

        BehaviorNode node = createNode(type);

        if (actions.containsKey(type)) {
            Action action = context.deserialize(json, actions.get(type));
            addAction((ActionNode) node, action);
        } else if (decorators.containsKey(type)) {
            Action action = context.deserialize(json, decorators.get(type));
            addAction((ActionNode) node, action);
            JsonElement childJson = json.getAsJsonObject().get("child");
            BehaviorNode child = context.deserialize(childJson, BehaviorNode.class);
            node.insertChild(0, child);
        } else if (node instanceof CompositeNode) {
            List<BehaviorNode> children = context.deserialize(json, new TypeToken<List<BehaviorNode>>() {
            }.getType());
            ((CompositeNode) node).children.addAll(children);
        }

        return node;
    }

    private BehaviorNode createNode(String type) {
        switch (type) {
            case "sequence":
                return new SequenceNode();
            case "selector":
                return new SelectorNode();
            case "dynamic":
                return new DynamicSelectorNode();
            case "parallel":
                return new ParallelNode();
            case "failure":
                return new FailureNode();
            case "success":
                return new SuccessNode();
            case "running":
                return new RunningNode();
            case "action":
                return new ActionNode();
            case "decorator":
                return new DecoratorNode();
            default:
                if (actions.containsKey(type)) {
                    return new ActionNode();
                }
                if (decorators.containsKey(type)) {
                    return new DecoratorNode();
                }
                throw new IllegalArgumentException("Unknown behavior node type " + type);
        }
    }
}
