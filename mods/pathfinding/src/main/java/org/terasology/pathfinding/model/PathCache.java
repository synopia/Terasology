package org.terasology.pathfinding.model;

import java.util.HashMap;
import java.util.Map;

/**
 * @author synopia
 */
public class PathCache {
    private Map<WalkableBlock, Map<WalkableBlock, Path>> pathes = new HashMap<WalkableBlock, Map<WalkableBlock, Path>>();

//    public Path getCachedPath( WalkableBlock from, WalkableBlock to ) {
//
//    }

    public Path findPath( WalkableBlock from, WalkableBlock to ) {
        //if( from.getBlockPosition().)
        HAStar local = new HAStar(false);
        local.run(from, to);
        return local.getPath();
    }

    public void clear() {

    }
}
