package org.terasology.pathfinding.model;

import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.chunks.Chunk;

import java.util.*;

/**
* @author synopia
*/
public class HeightMap {
    public static final int SIZE_X = Chunk.SIZE_X;
    public static final int SIZE_Z = Chunk.SIZE_Z;
    public static final int DIR_LEFT = 0;
    public static final int DIR_UP = 2;
    public static final int DIR_RIGHT = 4;
    public static final int DIR_DOWN = 6;
    public static final int[][] DIRECTIONS = new int[][] {
            {-1,0}, {-1,-1}, {0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 1}
    };

    public HeightMapCell[] cells = new HeightMapCell[SIZE_X*SIZE_Z];
    public final List<WalkableBlock> walkableBlocks = new ArrayList<WalkableBlock>();
    private List<Region> regions;
    private Map<Region,Integer> count;
    private List<Sweep> sweeps;
    private WorldProvider world;
    public Vector3i worldPos;
    private List<Floor> floors;
    public final Set<WalkableBlock> borderBlocks = new HashSet<WalkableBlock>();

    public HeightMap(WorldProvider world, Vector3i chunkPos) {
        this.world = world;
        this.worldPos = new Vector3i(chunkPos);
        worldPos.mult(Chunk.SIZE_X, Chunk.SIZE_Y, Chunk.SIZE_Z);
        for (int i = 0; i < cells.length; i++) {
            cells[i] = new HeightMapCell();
        }
    }

    public void update() {
        findWalkableBlocks();
        findNeighbors();
        findRegions();
        findRegionNeighbors();
        findFloors();
    }

    public void findWalkableBlocks() {
        int airMap[] = new int[Chunk.SIZE_X*Chunk.SIZE_Z];
        Vector3i blockPos = new Vector3i();
        regions = new ArrayList<Region>();
        walkableBlocks.clear();
        for (int y = Chunk.SIZE_Y-1; y >= 0; y--) {
            for (int z = 0; z < Chunk.SIZE_Z; z++) {
                for (int x = 0; x < Chunk.SIZE_X; x++) {
                    blockPos.set(x+worldPos.x, y+worldPos.y, z+worldPos.z);
                    Block block = world.getBlock(blockPos);
                    int offset = x + z * Chunk.SIZE_Z;
                    if( block.isPenetrable() ) {
                        airMap[offset]++;
                    } else {
                        if( airMap[offset]>=2 ) {
                            WalkableBlock walkableBlock = new WalkableBlock(blockPos.x, blockPos.z, blockPos.y);
                            cells[offset].addBlock(walkableBlock);
                            walkableBlocks.add(walkableBlock);
                        }
                        airMap[offset] = 0;
                    }
                }
            }
        }
    }

    public void findNeighbors() {
        borderBlocks.clear();
        for (int z = 0; z < Chunk.SIZE_Z; z++) {
            for (int x = 0; x < Chunk.SIZE_X; x++) {
                int offset = x + z * Chunk.SIZE_Z;
                HeightMapCell cell = cells[offset];
                findNeighbor(cell, x, z);
            }
        }
    }

    private void findNeighbor(HeightMapCell cell, int x, int z) {
        for (WalkableBlock block : cell.blocks) {
            for (int i = 0; i < DIRECTIONS.length; i++) {
                int nx = x + DIRECTIONS[i][0];
                int nz = z + DIRECTIONS[i][1];
                if( nx<0 || nz<0 || nx>= HeightMap.SIZE_X || nz>=HeightMap.SIZE_Z ) {
                    borderBlocks.add(block);
                    continue;
                }
                HeightMapCell neighbor = cells[nx + nz * HeightMap.SIZE_Z];
                for (WalkableBlock neighborBlock : neighbor.blocks) {
                    if( Math.abs( block.height-neighborBlock.height )<2 ) {
                        block.neighbors[i] = neighborBlock;
                    }
                }
            }
        }
    }

    public void connectNeighborMaps( HeightMap left, HeightMap up, HeightMap right, HeightMap down ) {
        for (WalkableBlock block : borderBlocks) {
            int x = TeraMath.calcBlockPosX(block.getBlockPosition().x);
            int z = TeraMath.calcBlockPosZ(block.getBlockPosition().z);
            if( left!=null && x==0 ) {
                connectToNeighbor(block, HeightMap.SIZE_X-1, z, left, DIR_LEFT);
            }
            if( right!=null && x==HeightMap.SIZE_X-1 ) {
                connectToNeighbor(block, 0, z, right, DIR_RIGHT);
            }
            if( up!=null && z==0 ) {
                connectToNeighbor(block, x, HeightMap.SIZE_Z-1, up, DIR_UP);
            }
            if( down!=null && z==HeightMap.SIZE_Z-1 ) {
                connectToNeighbor(block, x, 0, down, DIR_DOWN);
            }
        }
    }

    private void connectToNeighbor( WalkableBlock block, int dx, int dz, HeightMap neighbor, int neighborId ) {
        HeightMapCell neighborCell = neighbor.getCell(dx, dz);

        for (WalkableBlock candidate : neighborCell.blocks) {
            if( Math.abs(candidate.height-block.height)<2 ) {
                block.neighbors[neighborId] = candidate;
                block.region.floor.neighborRegions.add(candidate.region.floor);
                candidate.neighbors[(neighborId+4)%8] = block;
                candidate.region.floor.neighborRegions.add(block.region.floor);
            }
        }
    }

    public void findRegions() {
        regions.clear();
        for (int z = 0; z < Chunk.SIZE_Z; z++) {
            sweeps = new ArrayList<Sweep>();
            count = new HashMap<Region, Integer>();
            // find sweeps
            for (int x = 0; x < Chunk.SIZE_X; x++) {
                int offset = x + z * Chunk.SIZE_Z;
                HeightMapCell cell = cells[offset];

                findSweeps(cell);
            }
            // map sweeps to regions
            for (Sweep sweep : sweeps) {
                if( sweep.neighbor!=null && sweep.neighborCount== count.get(sweep.neighbor) ) {
                    sweep.region = sweep.neighbor;
                } else {
                    sweep.region = new Region(regions.size());
                    regions.add(sweep.region);
                }
            }
            for (int x = 0; x < Chunk.SIZE_X; x++) {
                int offset = x + z * Chunk.SIZE_Z;
                HeightMapCell cell = cells[offset];

                for (WalkableBlock block : cell.blocks) {
                    block.region = block.sweep.region;
                    block.sweep = null;
                    block.region.setPassable(block.x-worldPos.x, block.z-worldPos.z);
                }
            }
        }
    }

    private void findSweeps(HeightMapCell cell) {
        for (WalkableBlock block : cell.blocks) {
            Sweep sweep;
            WalkableBlock leftNeighbor = block.neighbors[DIR_LEFT];
            if( leftNeighbor !=null ) {
                sweep = leftNeighbor.sweep;
            } else {
                sweep = new Sweep();
                sweeps.add(sweep);
            }
            WalkableBlock upNeighbor = block.neighbors[DIR_UP];
            if( upNeighbor !=null ) {
                if( upNeighbor.region!=null) {
                    if( sweep.neighborCount==0 ) {
                        sweep.neighbor = upNeighbor.region;
                    }
                    if( sweep.neighbor==upNeighbor.region ) {
                        sweep.neighborCount++;
                        int c = count.containsKey(upNeighbor.region) ? count.get(upNeighbor.region) : 0;
                        c ++;
                        count.put(upNeighbor.region, c);
                    } else {
                        sweep.neighbor = null;
                    }
                }
            }

            block.sweep = sweep;
        }
    }

    public void findRegionNeighbors() {
        for (int z = 0; z < Chunk.SIZE_Z; z++) {
            for (int x = 0; x < Chunk.SIZE_X; x++) {
                int offset = x + z * Chunk.SIZE_Z;
                HeightMapCell cell = cells[offset];
                findRegionNeighbors(cell);
            }
        }
    }

    private void findRegionNeighbors(HeightMapCell cell) {
        for (WalkableBlock block : cell.blocks) {
            Region region = block.region;
            if( region==null ) {
                continue;
            }

            for (WalkableBlock neighbor : block.neighbors) {
                if( neighbor!=null && neighbor.region!=null && neighbor.region!=region ) {
                    region.addNeighborBlock(block, neighbor);
                }
            }
        }
    }

    public void findFloors() {
        floors = new ArrayList<Floor>();
        for (Region region : regions) {
            if( region.floor!=null ) {
                continue;
            }
            Floor floor = new Floor(this, floors.size());
            floors.add(floor);

            LinkedList<Region> stack = new LinkedList<Region>();
            stack.push(region);

            while (!stack.isEmpty()) {
                Collections.sort(stack, new Comparator<Region>() {
                    @Override
                    public int compare(Region o1, Region o2) {
                        return o1.id<o2.id ? -1 : o1.id>o2.id ? 1 : 0;
                    }
                });
                Region current = stack.poll();
                if( current.floor!=null ) {
                    continue;
                }
                if( !floor.overlap(current) ) {
                    floor.merge(current);

                    Set<Region> neighborRegions = current.getNeighborRegions();
                    for (Region neighborRegion : neighborRegions) {
                        if( neighborRegion.floor==null ) {
                            stack.add(neighborRegion);
                        }
                    }
                }
            }
        }
    }

    public HeightMapCell getCell(int x, int z) {
        return cells[x + z * Chunk.SIZE_Z];
    }

    public WalkableBlock getBlock( int x, int y, int z ) {
        return getCell(x-worldPos.x, z-worldPos.z).getBlock(y-worldPos.y);
    }

    public List<Floor> getFloors() {
        return floors;
    }

    public Floor getFloor( int id ) {
        for (Floor floor : floors) {
            if( floor.id==id ){
                return floor;
            }
        }
        return null;
    }

    public Region getRegion( int id ) {
        for (Region region : regions) {
            if( region.id==id ) {
                return region;
            }
        }
        return null;
    }

    public void printInfo() {
        System.out.println("Walkable blocks: "+walkableBlocks.size());
        System.out.println("Regions: "+regions.size());
    }
}
