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
package org.terasology.persistence.typeHandling.coreTypes;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import org.terasology.persistence.typeHandling.DeserializationContext;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.SerializationContext;
import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.persistence.typeHandling.gson.GsonPersistedData;

import java.util.Collection;
import java.util.List;

/**
 * Created by synopia on 16/01/15.
 */
public class JsonTypeHandler implements TypeHandler<JsonElement> {
    @Override
    public PersistedData serialize(JsonElement value, SerializationContext context) {
        return context.create(value.toString());
    }

    @Override
    public JsonElement deserialize(PersistedData data, DeserializationContext context) {
        Gson gson = new Gson();
        return gson.fromJson(data.getAsString(), JsonElement.class);
    }

    @Override
    public PersistedData serializeCollection(Collection<JsonElement> value, SerializationContext context) {
        return context.create("[" + value.toString() + "]");
    }

    @Override
    public List<JsonElement> deserializeCollection(PersistedData data, DeserializationContext context) {
        Gson gson = new Gson();
        return gson.fromJson(data.getAsString(), new TypeToken<List<JsonElement>>() {
        }.getType());
    }
}
