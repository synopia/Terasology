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
package org.terasology.logic.behavior.asset;

import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AssetLoader;
import org.terasology.logic.behavior.core.BehaviorNode;
import org.terasology.logic.behavior.core.BehaviorTreeBuilder;
import org.terasology.module.Module;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;

/**
 * Loader for behavior assets. Can also save assets into json format.
 * <p/>
 * If there are both, Nodes and Renderables tree, both are loaded/saved. To ensure, the nodes get associated to
 * the correct renderable, additional ids are introduced (only in the json file).
 * <p/>
 *
 * @author synopia
 */
public class BehaviorTreeLoader implements AssetLoader<BehaviorTreeData> {
    private static final Logger logger = LoggerFactory.getLogger(BehaviorTreeLoader.class);

    public void save(OutputStream stream, BehaviorTreeData data) throws IOException {
//        try (JsonWriter write = new JsonWriter(new OutputStreamWriter(stream, Charsets.UTF_8))) {
//            write.setIndent("  ");
//            write.beginObject().name("model");
//            treeGson.saveTree(write, data.getRoot());
//            write.endObject();
//        }
    }

    @Override
    public BehaviorTreeData load(Module module, InputStream stream, List<URL> urls, List<URL> deltas) throws IOException {
        BehaviorTreeBuilder builder = new BehaviorTreeBuilder();
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(BehaviorNode.class, builder);
        BehaviorNode node = gsonBuilder.create().fromJson(new InputStreamReader(stream), BehaviorNode.class);

        BehaviorTreeData data = new BehaviorTreeData();
        data.setRoot(node);
        return data;
    }
}
