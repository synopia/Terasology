package org.terasology.pathfinding.componentSystem;

import org.terasology.entitySystem.*;
import org.terasology.events.ActivateEvent;
import org.terasology.math.Vector3i;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.pathfinding.components.PathfinderComponent;
import org.terasology.pathfinding.model.*;
import org.terasology.pathfinding.rendering.InfoGrid;
import org.terasology.world.BlockChangedEvent;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.chunks.Chunk;


/**
 * @author synopia
 */
@RegisterComponentSystem
public class AddPathfinderBlockAction implements EventHandlerSystem {
    @In
    private WorldProvider worldProvider;
    private Pathfinder pathfinder;

    private WalkableBlock start;
    private WalkableBlock end;
    private InfoGrid grid;

    @Override
    public void initialise() {
        pathfinder = new Pathfinder(worldProvider);
    }

    @Override
    public void shutdown() {

    }

    @ReceiveEvent(components = BlockComponent.class)
    public void blockChanged(BlockChangedEvent event, EntityRef entity) {
        updateMap(event.getBlockPosition(), grid);
        if( start!=null && end!=null ) {
            start = pathfinder.getBlock(start.getBlockPosition());
            end   = pathfinder.getBlock(end.getBlockPosition());
            findPath(grid);
        }
    }


    @ReceiveEvent(components = PathfinderComponent.class)
    public void onActivate(ActivateEvent event, EntityRef entity ) {
        PathfinderComponent comp = entity.getComponent(PathfinderComponent.class);
        Vector3i targetPos = new Vector3i(event.getTargetLocation());

        grid = comp.grid;
        pathfinder.init(targetPos);

        if( start==null ) {
            start = pathfinder.getBlock(targetPos);
            if( start!=null ) {
                grid.addInfo(targetPos, "start", "start");
            }
            entity.saveComponent(comp);
        } else {
            grid.removeInfo("end");
            if( end==null ) {
                this.end = pathfinder.getBlock(targetPos);
                if( end!=null ){
                    grid.addInfo(targetPos, "end", "end");
                }
                findPath(grid);
            } else {
                start = null;
                end = null;
                grid.removeInfo("start");
            }
            entity.saveComponent(comp);
        }

    }

    private void findPath(InfoGrid grid) {
        PerformanceMonitor.startActivity("find path");
        grid.removeInfo("path");
        Path path = pathfinder.run(start, end);
        if(path!=null) {
            for (int i = 0; i < path.size(); i++) {
                WalkableBlock block = path.get(i);
                grid.addInfo(block.getBlockPosition(), "path", "step " + i);
            }
        }
        PerformanceMonitor.endActivity();
    }

    private void updateMap(Vector3i targetPos, InfoGrid grid) {
        PerformanceMonitor.startActivity("update map");
        grid.removeInfo("floor");
        pathfinder.update(targetPos);
//        for (WalkableBlock block : map.walkableBlocks) {
//            grid.addInfo(block.getBlockPosition(), "floor", "floor " + block.getFloor().id);
//        }
        PerformanceMonitor.endActivity();
    }
}
