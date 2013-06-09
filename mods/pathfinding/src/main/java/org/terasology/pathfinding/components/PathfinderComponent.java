package org.terasology.pathfinding.components;

import org.terasology.entitySystem.Component;
import org.terasology.pathfinding.rendering.InfoGrid;
import org.terasology.rendering.world.BlockGrid;

/**
 * @author synopia
 */
public class PathfinderComponent implements Component {
    public InfoGrid grid = new InfoGrid();
}
