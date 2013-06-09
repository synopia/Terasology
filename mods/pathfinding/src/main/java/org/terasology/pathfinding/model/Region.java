package org.terasology.pathfinding.model;

import java.util.*;

/**
* @author synopia
*/
public class Region extends BaseRegion<Region> {
    public Region(int id) {
        super(id);
    }

    public void addNeighborBlock( WalkableBlock current, WalkableBlock neighbor ) {
        contour.add(current);
        current.neighborRegions.add(neighbor.region);
        neighborRegions.add( neighbor.region);
    }

}
