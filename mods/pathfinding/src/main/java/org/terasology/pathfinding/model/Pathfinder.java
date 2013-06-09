package org.terasology.pathfinding.model;

import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.pathfinding.rendering.InfoGrid;
import org.terasology.world.WorldProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * @author synopia
 */
public class Pathfinder {
    private WorldProvider world;
    private Map<Vector3i, HeightMap> heightMaps = new HashMap<Vector3i, HeightMap>();

    public Pathfinder(WorldProvider world) {
        this.world = world;
    }

    public Path run( WalkableBlock start, WalkableBlock end ) {
        HAStar haStar = new HAStar();
        Path path = null;
        if( haStar.run(start, end) ) {
            path = haStar.getPath();
        }
        return path;
    }

    public void init( Vector3i blockPos ) {
        Vector3i chunkPos = TeraMath.calcChunkPos(blockPos);
        HeightMap heightMap = heightMaps.get(chunkPos);
        if( heightMap==null ) {
            heightMap = new HeightMap(world, chunkPos);
            heightMap.update();
            heightMaps.put(chunkPos, heightMap);
            heightMap.connectNeighborMaps(getNeighbor(chunkPos, -1, 0), getNeighbor(chunkPos, 0, -1), getNeighbor(chunkPos, 1,0), getNeighbor(chunkPos, 0,1));
        }
    }

    private HeightMap getNeighbor(Vector3i chunkPos, int x, int z) {
        Vector3i neighborPos = new Vector3i(chunkPos);
        neighborPos.add(x, 0, z);
        return heightMaps.get(neighborPos);
    }

    public void update( Vector3i blockPos ) {
        Vector3i chunkPos = TeraMath.calcChunkPos(blockPos);
        heightMaps.remove(chunkPos);
        init(blockPos);
    }

    public WalkableBlock getBlock(Vector3i pos) {
        Vector3i chunkPos = TeraMath.calcChunkPos(pos);
        HeightMap heightMap = heightMaps.get(chunkPos);
        if( heightMap!=null ) {
            return heightMap.getBlock(pos.x, pos.y, pos.z);
        } else {
            return null;
        }
    }
}
