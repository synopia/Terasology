/*
 * Copyright 2013 MovingBlocks
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

package org.terasology.world.internal;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.SimpleUri;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.math.Region3i;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.WorldChangeListener;
import org.terasology.world.WorldComponent;
import org.terasology.world.biomes.Biome;
import org.terasology.world.biomes.BiomeManager;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.ChunkProvider;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.chunks.LitChunk;
import org.terasology.world.chunks.RenderableChunk;
import org.terasology.world.chunks.internal.GeneratingChunkProvider;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.World;
import org.terasology.world.liquid.LiquidData;
import org.terasology.world.propagation.*;
import org.terasology.world.propagation.light.LightPropagationRules;
import org.terasology.world.propagation.light.LightWorldView;
import org.terasology.world.propagation.light.SunlightPropagationRules;
import org.terasology.world.propagation.light.SunlightRegenPropagationRules;
import org.terasology.world.propagation.light.SunlightRegenWorldView;
import org.terasology.world.propagation.light.SunlightWorldView;
import org.terasology.world.time.WorldTime;
import org.terasology.world.time.WorldTimeImpl;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Immortius
 */
public class WorldProviderCoreImpl implements WorldProviderCore {
    private static final Logger logger = LoggerFactory.getLogger(WorldProviderCoreImpl.class);

    private String title;
    private String seed = "";
    private SimpleUri worldGenerator;

    private GeneratingChunkProvider chunkProvider;
    private WorldTime worldTime;

    private final List<WorldChangeListener> listeners = Lists.newArrayList();

    private Map<Vector3i, BlockChange> blockChanges = Maps.newHashMap();
    private Map<Vector3i, BiomeChange> biomeChanges = Maps.newHashMap();
    private List<BatchPropagator> propagators = Lists.newArrayList();

    public WorldProviderCoreImpl(String title, String seed, long time, SimpleUri worldGenerator, GeneratingChunkProvider chunkProvider) {
        this.title = (title == null) ? seed : title;
        this.seed = seed;
        this.worldGenerator = worldGenerator;
        this.chunkProvider = chunkProvider;
        CoreRegistry.put(ChunkProvider.class, chunkProvider);
        this.worldTime = new WorldTimeImpl();
        worldTime.setMilliseconds(time);

        propagators.add(new StandardBatchPropagator(new LightPropagationRules(), new LightWorldView(chunkProvider)));
        PropagatorWorldView regenWorldView = new SunlightRegenWorldView(chunkProvider);
        PropagationRules sunlightRules = new SunlightPropagationRules(regenWorldView);
        PropagatorWorldView sunlightWorldView = new SunlightWorldView(chunkProvider);
        BatchPropagator sunlightPropagator = new StandardBatchPropagator(sunlightRules, sunlightWorldView);
        propagators.add(new SunlightRegenBatchPropagator(new SunlightRegenPropagationRules(), regenWorldView, sunlightPropagator, sunlightWorldView));
        propagators.add(sunlightPropagator);
    }

    public WorldProviderCoreImpl(WorldInfo info, GeneratingChunkProvider chunkProvider) {
        this(info.getTitle(), info.getSeed(), info.getTime(), info.getWorldGenerator(), chunkProvider);
    }

    @Override
    public EntityRef getWorldEntity() {
        Iterator<EntityRef> iterator = CoreRegistry.get(EntityManager.class).getEntitiesWith(WorldComponent.class).iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        }
        return EntityRef.NULL;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getSeed() {
        return seed;
    }

    @Override
    public WorldInfo getWorldInfo() {
        return new WorldInfo(title, seed, worldTime.getMilliseconds(), worldGenerator);
    }

    @Override
    public void processPropagation() {
        for (BatchPropagator propagator : propagators) {
            propagator.process(blockChanges.values());
        }
        blockChanges.clear();
    }

    @Override
    public void registerListener(WorldChangeListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    @Override
    public void unregisterListener(WorldChangeListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    @Override
    public ChunkViewCore getLocalView(Vector3i chunkPos) {
        return chunkProvider.getLocalView(chunkPos);
    }

    @Override
    public ChunkViewCore getWorldViewAround(Vector3i chunk) {
        return chunkProvider.getSubviewAroundChunk(chunk);
    }

    @Override
    public boolean isBlockRelevant(int x, int y, int z) {
        return chunkProvider.isChunkReady(TeraMath.calcChunkPos(x, y, z));
    }

    @Override
    public boolean isRegionRelevant(Region3i region) {
        for (Vector3i chunkPos : TeraMath.calcChunkPos(region)) {
            if (!chunkProvider.isChunkReady(chunkPos)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Block setBlock(Vector3i worldPos, Block type) {
        Vector3i chunkPos = TeraMath.calcChunkPos(worldPos);
        CoreChunk chunk = chunkProvider.getChunk(chunkPos);
        if (chunk != null) {
            Vector3i blockPos = TeraMath.calcBlockPos(worldPos);
            chunk.lock();
            Block oldBlockType = chunk.setBlock(blockPos, type);
            chunk.unlock();
            if (oldBlockType != type) {
                BlockChange oldChange = blockChanges.get(worldPos);
                if (oldChange == null) {
                    blockChanges.put(worldPos, new BlockChange(worldPos, oldBlockType, type));
                } else {
                    oldChange.setTo(type);
                }
                for (Vector3i pos : TeraMath.getChunkRegionAroundWorldPos(worldPos, 1)) {
                    RenderableChunk dirtiedChunk = chunkProvider.getChunk(pos);
                    if (dirtiedChunk != null) {
                        dirtiedChunk.setDirty(true);
                    }
                }
                notifyBlockChanged(worldPos, type, oldBlockType);
            }
            return oldBlockType;

        }
        return null;
    }

    private void notifyBlockChanged(Vector3i pos, Block type, Block oldType) {
        // TODO: Could use a read/write lock.
        // TODO: Review, should only happen on main thread (as should changes to listeners)
        synchronized (listeners) {
            for (WorldChangeListener listener : listeners) {
                listener.onBlockChanged(pos, type, oldType);
            }
        }
    }

    private void notifyBiomeChanged(Vector3i pos, Biome newBiome, Biome originalBiome) {
        // TODO: Could use a read/write lock.
        // TODO: Review, should only happen on main thread (as should changes to listeners)
        synchronized (listeners) {
            for (WorldChangeListener listener : listeners) {
                listener.onBiomeChanged(pos, newBiome, originalBiome);
            }
        }
    }

    @Override
    public boolean setLiquid(int x, int y, int z, LiquidData newState, LiquidData oldState) {
        Vector3i chunkPos = TeraMath.calcChunkPos(x, y, z);
        CoreChunk chunk = chunkProvider.getChunk(chunkPos);
        if (chunk != null) {
            chunk.lock();
            try {
                Vector3i blockPos = TeraMath.calcBlockPos(x, y, z);
                LiquidData liquidState = chunk.getLiquid(blockPos);
                if (liquidState.equals(oldState)) {
                    chunk.setLiquid(blockPos, newState);
                    return true;
                }
            } finally {
                chunk.unlock();
            }
        }
        return false;
    }

    @Override
    public LiquidData getLiquid(int x, int y, int z) {
        Vector3i chunkPos = TeraMath.calcChunkPos(x, y, z);
        CoreChunk chunk = chunkProvider.getChunk(chunkPos);
        if (chunk != null) {
            Vector3i blockPos = TeraMath.calcBlockPos(x, y, z);
            return chunk.getLiquid(blockPos);
        }
        logger.warn("Attempted to access unavailable chunk via liquid data at {}, {}, {}", x, y, z);
        return new LiquidData();
    }

    @Override
    public Block getBlock(int x, int y, int z) {
        Vector3i chunkPos = TeraMath.calcChunkPos(x, y, z);
        CoreChunk chunk = chunkProvider.getChunk(chunkPos);
        if (chunk != null) {
            Vector3i blockPos = TeraMath.calcBlockPos(x, y, z);
            return chunk.getBlock(blockPos);
        }
        logger.warn("Attempted to access unavailable chunk via block at {}, {}, {}", x, y, z);
        return BlockManager.getAir();
    }

    @Override
    public Biome getBiome(Vector3i pos) {
        Vector3i chunkPos = TeraMath.calcChunkPos(pos);
        CoreChunk chunk = chunkProvider.getChunk(chunkPos);
        if (chunk != null) {
            Vector3i blockPos = TeraMath.calcBlockPos(pos);
            return chunk.getBiome(blockPos.x, blockPos.y, blockPos.z);
        }
        logger.warn("Attempted to access unavailable chunk via block at {}, {}, {}", pos.x, pos.y, pos.z);
        return BiomeManager.getUnknownBiome();
    }

    @Override
    public Biome setBiome(Vector3i worldPos, Biome biome) {
        Vector3i chunkPos = TeraMath.calcChunkPos(worldPos);
        CoreChunk chunk = chunkProvider.getChunk(chunkPos);
        if (chunk != null) {
            Vector3i blockPos = TeraMath.calcBlockPos(worldPos);
            chunk.lock();
            Biome oldBiomeType = chunk.setBiome(blockPos.x, blockPos.y, blockPos.z, biome);
            chunk.unlock();
            if (oldBiomeType != biome) {
                BiomeChange oldChange = biomeChanges.get(worldPos);
                if (oldChange == null) {
                    biomeChanges.put(worldPos, new BiomeChange(worldPos, oldBiomeType, biome));
                } else {
                    oldChange.setTo(biome);
                }
                for (Vector3i pos : TeraMath.getChunkRegionAroundWorldPos(worldPos, 1)) {
                    RenderableChunk dirtiedChunk = chunkProvider.getChunk(pos);
                    if (dirtiedChunk != null) {
                        dirtiedChunk.setDirty(true);
                    }
                }
                notifyBiomeChanged(worldPos, biome, oldBiomeType);
            }
            return oldBiomeType;

        }
        return null;
    }

    @Override
    public byte getLight(int x, int y, int z) {
        Vector3i chunkPos = TeraMath.calcChunkPos(x, y, z);
        LitChunk chunk = chunkProvider.getChunk(chunkPos);
        if (chunk != null) {
            Vector3i blockPos = TeraMath.calcBlockPos(x, y, z);
            return chunk.getLight(blockPos);
        }
        logger.warn("Attempted to access unavailable chunk via light at {}, {}, {}", x, y, z);
        return 0;
    }

    @Override
    public byte getSunlight(int x, int y, int z) {
        Vector3i chunkPos = TeraMath.calcChunkPos(x, y, z);
        LitChunk chunk = chunkProvider.getChunk(chunkPos);
        if (chunk != null) {
            Vector3i blockPos = TeraMath.calcBlockPos(x, y, z);
            return chunk.getSunlight(blockPos);
        }
        logger.warn("Attempted to access unavailable chunk via sunlight at {}, {}, {}", x, y, z);
        return 0;
    }

    @Override
    public byte getTotalLight(int x, int y, int z) {
        Vector3i chunkPos = TeraMath.calcChunkPos(x, y, z);
        LitChunk chunk = chunkProvider.getChunk(chunkPos);
        if (chunk != null) {
            Vector3i blockPos = TeraMath.calcBlockPos(x, y, z);
            return (byte) Math.max(chunk.getSunlight(blockPos), chunk.getLight(blockPos));
        }
        logger.warn("Attempted to access unavailable chunk via total light at {}, {}, {}", x, y, z);
        return 0;
    }

    @Override
    public void dispose() {
        chunkProvider.dispose();

    }

    @Override
    public WorldTime getTime() {
        return worldTime;
    }

}
