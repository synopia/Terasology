package org.terasology.pathfinding.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.terasology.math.Vector3i;

import java.util.ArrayList;
import java.util.List;

/**
 * @author synopia
 */
public class HAStarTest {
    public HeightMap map;
    public ExampleHelper.TestWorld world;
    public ExampleHelper helper;
    private WalkableBlock start;
    private WalkableBlock end;

    @Before
    public void setup() {
        helper = new ExampleHelper();
        world = helper.world;
        map = helper.map;
    }

    @Test
    public void simple() {
        executeExample(

                "XXX : X :  *:      ",
                "X*X :   :   :XXX*  ",
                "XSX :   :   :XXXE  "

//                "XXXXX",
//                "XS*EX",
//                "XXXXX"
        );
    }

    private void executeExample( String ...lines) {
        helper.createWorld();
        world = helper.world;
        map = helper.map;
        helper.addBlocks(lines);

        ExampleHelper.process(new ExampleHelper.Runner() {
            @Override
            public void run(int x, int y, int z, char value) {
                switch (value) {
                    case 'S':case 'E':case '*':
                    helper.setBlock( x, y, z );
                    break;
                }
            }
        }, lines);
        helper.run();
        final List<Vector3i> expected = new ArrayList<Vector3i>();
        ExampleHelper.process(new ExampleHelper.Runner() {
            @Override
            public void run(int x, int y, int z, char value) {
                switch (value) {
                    case 'S':
                        start = map.getBlock(x, y, z);
                        expected.add(start.getBlockPosition());
                        break;
                    case 'E':
                        end = map.getBlock(x, y, z);
                        expected.add(end.getBlockPosition());
                        break;
                    case '*':
                        expected.add(map.getBlock(x,y,z).getBlockPosition());
                        break;
                }
            }
        }, lines);

        HAStar haStar = new HAStar();

        haStar.run(start, end);
        List<WalkableBlock> path = haStar.getPath();

        for (WalkableBlock block : path) {
            if( !expected.remove(block.getBlockPosition()) ) {
                Assert.fail(block.toString());
            }
        }
        Assert.assertEquals(0, expected.size());
    }

}
