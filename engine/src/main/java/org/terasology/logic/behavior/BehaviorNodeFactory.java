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
package org.terasology.logic.behavior;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AssetManager;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.engine.module.ModuleManager;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.behavior.core.BehaviorNode;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.itemRendering.StringTextRenderer;
import org.terasology.rendering.nui.properties.OneOfProviderFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Factory to create node instances from node entities.
 *
 * @author synopia
 */
@RegisterSystem
public class BehaviorNodeFactory extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(BehaviorNodeFactory.class);

    private Map<Class<?>, BehaviorNodeComponent> nodes = Maps.newHashMap();
    private Map<String, List<BehaviorNodeComponent>> categoryComponents = Maps.newHashMap();
    private List<String> categories;

    @In
    private ModuleManager moduleManager;
    @In
    private EntityManager entityManager;
    @In
    private PrefabManager prefabManager;
    @In
    private AssetManager assetManager;
    @In
    private OneOfProviderFactory providerFactory;

    private List<AssetUri> sounds = Lists.newArrayList();
    private List<AssetUri> music = Lists.newArrayList();

    public BehaviorNodeFactory() {
        CoreRegistry.put(BehaviorNodeFactory.class, this);
    }

    @Override
    public void postBegin() {
        refreshLibrary();
    }

    public void refreshLibrary() {
        for (AssetUri uri : assetManager.listAssets(AssetType.SOUND)) {
            sounds.add(uri);
        }
        for (AssetUri uri : assetManager.listAssets(AssetType.MUSIC)) {
            music.add(uri);
        }
        providerFactory.register("sounds", new ReadOnlyBinding<List<AssetUri>>() {
                    @Override
                    public List<AssetUri> get() {
                        return sounds;
                    }
                }, new StringTextRenderer<AssetUri>() {
                    @Override
                    public String getString(AssetUri value) {
                        return value.getAssetName().toString();
                    }
                }
        );
        providerFactory.register("music", new ReadOnlyBinding<List<AssetUri>>() {
                    @Override
                    public List<AssetUri> get() {
                        return music;
                    }
                }, new StringTextRenderer<AssetUri>() {
                    @Override
                    public String getString(AssetUri value) {
                        return value.getAssetName().toString();
                    }
                }
        );
        refreshPrefabs();
        sortLibrary();
    }

    private void sortLibrary() {
        categories = Lists.newArrayList(categoryComponents.keySet());
        Collections.sort(categories);
        for (String category : categories) {
            Collections.sort(categoryComponents.get(category), new Comparator<BehaviorNodeComponent>() {
                @Override
                public int compare(BehaviorNodeComponent o1, BehaviorNodeComponent o2) {
                    return o1.name.compareTo(o2.name);
                }
            });
        }
    }

    private void refreshPrefabs() {
        Collection<Prefab> prefabs = prefabManager.listPrefabs(BehaviorNodeComponent.class);
        if (prefabs.size() == 0) {
            // called from main menu
            List<String> nodes = Arrays.asList("action", "decorator", "dynselector", "fail", "parallel", "playMusic", "playSound", "running", "selector", "sequence", "succeed");
            prefabs = Lists.newArrayList();
            for (String node : nodes) {
                prefabs.add(prefabManager.getPrefab("engine:" + node));
            }
        }
        for (Prefab prefab : prefabs) {
            EntityRef entityRef = entityManager.create(prefab);
            entityRef.setPersistent(false);
            BehaviorNodeComponent component = entityRef.getComponent(BehaviorNodeComponent.class);
            try {
                Class<?> type = Class.forName("org.terasology.logic.behavior.core." + component.type);
                nodes.put(type, component);
                logger.debug("Found behavior node for class " + component.type + " name=" + component.name);
                List<BehaviorNodeComponent> list = categoryComponents.get(component.category);
                if (list == null) {
                    list = Lists.newArrayList();
                    categoryComponents.put(component.category, list);
                }
                list.add(component);
            } catch (ClassNotFoundException e) {
                logger.warn("Node cannot be constructed! -> ignoring " + component.type + " name=" + component.name, e);
            }
        }
    }

    public BehaviorNodeComponent getNodeComponent(BehaviorNode node) {
        Class<? extends BehaviorNode> type = node.getClass();
        BehaviorNodeComponent nodeComponent = nodes.get(type);
        if (nodeComponent == null) {
            return BehaviorNodeComponent.DEFAULT;
        }
        return nodeComponent;
    }

    public BehaviorNode getNode(BehaviorNodeComponent nodeComponent) {
        for (Map.Entry<Class<?>, BehaviorNodeComponent> entry : nodes.entrySet()) {
            if (nodeComponent == entry.getValue()) {
                Class<?> type = entry.getKey();
                try {
                    return (BehaviorNode) type.newInstance();
                } catch (InstantiationException e) {
                    logger.warn("Node cannot be constructed! -> ignoring " + nodeComponent.type + " name=" + nodeComponent.name, e);
                } catch (IllegalAccessException e) {
                    logger.warn("Node cannot be constructed! -> ignoring " + nodeComponent.type + " name=" + nodeComponent.name, e);
                }
            }
        }
        return null;
    }

    public Collection<BehaviorNodeComponent> getNodeComponents() {
        return nodes.values();
    }

    public List<String> getCategories() {
        return categories;
    }

    public List<BehaviorNodeComponent> getNodesComponents(String category) {
        return categoryComponents.get(category);
    }
}
