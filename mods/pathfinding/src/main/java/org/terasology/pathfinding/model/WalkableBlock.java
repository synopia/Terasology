package org.terasology.pathfinding.model;

import org.terasology.math.Vector3i;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
* @author synopia
*/
public class WalkableBlock {
    public int x, z;
    public int height;
    public WalkableBlock[] neighbors = new WalkableBlock[8];
    public Sweep sweep;
    public Region region;
    public Set<Region> neighborRegions = new HashSet<Region>();

    public WalkableBlock(int x, int z, int height) {
        this.x = x;
        this.z = z;
        this.height = height;
    }

    public Vector3i getBlockPosition() {
        return new Vector3i(x, height, z);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if( region!=null ) {
            sb.append(region.toString()).append("\n");
        } else {
            sb.append("no region\n");
        }
        sb.append(x).append(",").append(z).append(",").append(height).append("\n");
        if( neighborRegions.size()>0 ){
            sb.append(" -> ");
            for (Region neighborRegion : neighborRegions) {
                sb.append(neighborRegion.id);
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    public boolean hasNeighbor(WalkableBlock block) {
        for (WalkableBlock neighbor : neighbors) {
            if( neighbor==block ) {
                return true;
            }
        }
        return false;
    }

    public Floor getFloor() {
        return region.floor;
    }
}
