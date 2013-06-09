package org.terasology.pathfinding.model;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.terasology.math.Vector3i;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author synopia
 */
public class HeightMapTest {

    public HeightMap map;
    public ExampleHelper.TestWorld world;
    private ExampleHelper helper;

    @Before
    public void setup() {
        helper = new ExampleHelper();
        world = helper.world;
        map = helper.map;
    }

    private void executeExampleRegion(String ...lines) {
        helper.createWorld();
        String[][] split = ExampleHelper.split("\\|", lines);
        String[] blocks = split[0];
        String[] region = split[1];
        helper.addBlocks(blocks);
        helper.run();
        ExampleHelper.process(new ExampleHelper.Runner() {
            @Override
            public void run(int x, int y, int z, char value) {
                if( value==' ') {
                    Assert.assertNull(map.getBlock(x,y,z));
                } else {
                    int id = Integer.parseInt(value + "");
                    Assert.assertEquals("x=" + x + ", y=" + y + ", z=" + z, id, helper.map.getBlock(x, y, z).region.id);
                }
            }
        }, region);
    }
    private void executeExampleFloor(String...lines) {
        helper.createWorld();
        String[][] split = ExampleHelper.split("\\|", lines);
        String[] blocks = split[0];
        String[] floor  = split[1];
        helper.addBlocks(blocks);
        helper.run();
        ExampleHelper.process(new ExampleHelper.Runner() {
            @Override
            public void run(int x, int y, int z, char value) {
                if( value==' ') {
                    Assert.assertNull(map.getBlock(x,y,z));
                } else {
                    int id = Integer.parseInt(value + "");
                    Assert.assertEquals("x=" + x + ", y=" + y + ", z=" + z, id, helper.map.getBlock(x, y, z).region.floor.id);
                }
            }
        }, floor);
    }

//    private void executeExample( Runner runner, String...lines ) {
/*
        if( neighbors!=null ) {
            for (int j = 0; j < neighbors.length; j++) {
                String neighbor = neighbors[j][0];
                if( neighbor.length()==0 ) {
                    continue;
                }
                Region region = runner.getRegion(j);
                Set<Integer> neighborRegions = new HashSet<Integer>();
                for (Region neighborRegion : region.getNeighborRegions()) {
                    neighborRegions.add(neighborRegion.id);
                }
                String[] parts = neighbor.split(",");
                for (String part : parts) {
                    int neighborId = Integer.parseInt(part.trim());
                    if (!neighborRegions.remove(neighborId)) {
                        Assert.fail(neighborId + " not found");
                    }
                }
                Assert.assertEquals(Arrays.toString(neighborRegions.toArray()), 0, neighborRegions.size());
            }
        }
*/

//    }

    @Test
    public void testFindRegions() {
        executeExampleRegion("X |0 ");
        executeExampleRegion(
                "XX |00",
                "XX |00"
        );
        executeExampleRegion(
                "XXX    |000",
                "XX     |00",
                "XXX    |000"
        );
        executeExampleRegion(
                "XXX    |000",
                " XX    | 00",
                "XXX    |000"
        );
        executeExampleRegion(
                "XXX    |000 | 1,2:0,3:0,3:1,2",
                "X X    |1 2",
                "XXX    |333"
        );
        executeExampleRegion(
                "X X X  |0 1 2 | 3:3:3:0,1,2,4,5,6:3:3:3",
                "XXXXX  |33333",
                "X X X  |4 5 6"
        );
        executeExampleRegion(
                "XXX : X :  X:   X  |0    : 0 :  0 :   0 | 1,2 : 0 : 0",
                "XXX :   :   :XXXX  |111  :   :    :2222",
                "XXX :   :   :XXXX  |111  :   :    :2222"
        );
    }
    @Test
    public void testFindFloor() {
        executeExampleFloor("X |0 ");
        executeExampleFloor(
                "XX |00",
                "XX |00"
        );
        executeExampleFloor(
                "XXX    |000",
                "XX     |00",
                "XXX    |000"
        );
        executeExampleFloor(
                "XXX    |000",
                " XX    | 00",
                "XXX    |000"
        );
        executeExampleFloor(
                "XXX    |000",
                "X X    |0 0",
                "XXX    |000"
        );
        executeExampleFloor(
                "X X X  |0 0 0",
                "XXXXX  |00000",
                "X X X  |0 0 0"
        );
        executeExampleFloor(
                "XXXXXXXXXX  |0000000000",
                "X    XXXXX  |0    00000",
                "X XX XXXXX  |0 11 00000",
                "X XX XXXXX  |0 11 00000",
                "X    XXXXX  |0    00000",
                "XXXXXXXXXX  |0000000000"
        );
        executeExampleFloor(
                "XXXXXXXXXX  |0000000000",
                "X    XXXXX  |0    00000",
                "X XXXXXXXX  |0 00000000",
                "X XX XXXXX  |0 00 00000",
                "X    XXXXX  |0    00000",
                "XXXXXXXXXX  |0000000000"
        );
        executeExampleFloor(
                "XXX : X :  X:   X  |0    : 0 :  0 :   0 | 1 : 0 ",
                "XXX :   :   :XXXX  |000  :   :    :1111",
                "XXX :   :   :XXXX  |000  :   :    :1111"
        );
    }


    @Test
    public void testFindWalkableBlocks() {
        world.setBlocks(1, "XXX");
        world.setBlocks(2, "  X");
        world.setBlocks(3, "X ");
        world.setBlocks(4, "XX");
        map.findWalkableBlocks();
        Assert.assertEquals(4, map.walkableBlocks.size());
        for (int i = 0; i < map.walkableBlocks.size(); i++) {
            WalkableBlock block = map.walkableBlocks.get(i);
            switch (i) {
                case 0:
                    Assert.assertEquals(new Vector3i(0,4,0), block.getBlockPosition());
                    break;
                case 1:
                    Assert.assertEquals(new Vector3i(1,4,0), block.getBlockPosition());
                    break;
                case 2:
                    Assert.assertEquals(new Vector3i(2,2,0), block.getBlockPosition());
                    break;
                case 3:
                    Assert.assertEquals(new Vector3i(1,1,0), block.getBlockPosition());
                    break;
            }
        }
        Assert.assertEquals(1, map.getCell(0, 0).blocks.size());
        Assert.assertEquals(2, map.getCell(1, 0).blocks.size());
        Assert.assertEquals(1, map.getCell(2, 0).blocks.size());
    }

    @Test
    public void testFindNeighbors() {
        world.setBlocks(1, "XX", "XX");
        world.setBlocks(4,
                " X ",
                "XXX",
                " X");
        map.findWalkableBlocks();
        Assert.assertEquals(9, map.walkableBlocks.size());
        map.findNeighbors();
        WalkableBlock lu = map.getCell(0, 0).blocks.get(0);
        WalkableBlock ru = map.getCell(1, 0).blocks.get(0);
        WalkableBlock ld = map.getCell(0, 1).blocks.get(0);
        WalkableBlock rd = map.getCell(1, 1).blocks.get(0);
        assertNeighbors(lu, null, null, ru,   ld);
        assertNeighbors(ru, lu,   null, null, rd);
        assertNeighbors(ld, null, lu,   rd,   null);
        assertNeighbors(rd, ld,   ru,   null, null);

        WalkableBlock up = map.getCell(1, 0).blocks.get(1);
        WalkableBlock left = map.getCell(0, 1).blocks.get(1);
        WalkableBlock center = map.getCell(1, 1).blocks.get(1);
        WalkableBlock right = map.getCell(2, 1).blocks.get(0);
        WalkableBlock down = map.getCell(1, 2).blocks.get(0);
        assertNeighbors(up, null, null, null, center);
        assertNeighbors(left, null, null, center, null);
        assertNeighbors(down, null, center, null, null);
        assertNeighbors(right, center, null, null, null);
        assertNeighbors(center, left, up, right, down);
    }

    private void assertNeighbors( WalkableBlock block, WalkableBlock left, WalkableBlock up, WalkableBlock right, WalkableBlock down ) {
        Assert.assertEquals("left", left, block.neighbors[HeightMap.DIR_LEFT]);
        Assert.assertEquals("up", up, block.neighbors[HeightMap.DIR_UP]);
        Assert.assertEquals("right", right, block.neighbors[HeightMap.DIR_RIGHT]);
        Assert.assertEquals("down", down, block.neighbors[HeightMap.DIR_DOWN]);
    }
}
