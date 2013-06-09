package org.terasology.pathfinding.model;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author synopia
 */
public class Floor extends BaseRegion<Floor> {
    private Set<Region> merged = new HashSet<Region>();
    private HeightMap heightMap;

    public Floor(HeightMap heightMap, int id) {
        super(id);
        this.heightMap = heightMap;
    }

    public boolean overlap( Region region ) {
        return map.overlap(region.map);
    }

    public void merge( Region neighbor ) {
        map.merge(neighbor.map);
        neighbor.floor = this;
        merged.add(neighbor);

        for (Region neighborRegion : neighbor.neighborRegions) {
            if( neighborRegion.floor!=null && neighborRegion.floor!=this) {
                neighborRegions.add(neighborRegion.floor);
                neighborRegion.floor.neighborRegions.add(this);
            }
        }

        for (WalkableBlock block : neighbor.contour) {
            for (Region neighborRegion : block.neighborRegions) {
                if( neighborRegion.floor==null || neighborRegion.floor==this ) {
                    contour.add(block);
                    break;
                }
            }
        }
    }
}
