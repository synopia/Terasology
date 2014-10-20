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
package org.terasology.world.generation;

import com.google.common.collect.Maps;
import org.terasology.entitySystem.Component;
import org.terasology.world.generator.WorldConfigurator;

import java.util.Map;

public class FacetedWorldConfigurator implements WorldConfigurator {
    Map<String, Component> properties = Maps.newHashMap();

    public void addProperty(String name, Component data) {
        if (!properties.containsKey(name)) {
            properties.put(name, data);
        }
    }

    public Map<String, Component> getProperties() {
        return properties;
    }
}

