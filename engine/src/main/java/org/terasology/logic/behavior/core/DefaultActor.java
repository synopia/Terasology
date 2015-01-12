package org.terasology.logic.behavior.core;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Created by synopia on 12.01.2015.
 */
public class DefaultActor<T> implements Actor<T> {
    private float delta;
    private final Map<Integer, Object> dataMap = Maps.newHashMap();

    public <T> T getValue(int id) {
        return (T) dataMap.get(id);
    }

    public void setValue(int id, Object obj) {
        dataMap.put(id, obj);
    }

    public float getDelta() {
        return delta;
    }

    public void setDelta(float delta) {
        this.delta = delta;
    }

}
