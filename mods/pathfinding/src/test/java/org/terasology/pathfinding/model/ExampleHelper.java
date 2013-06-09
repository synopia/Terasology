package org.terasology.pathfinding.model;

import org.junit.Before;
import org.terasology.math.Vector3i;
import org.terasology.world.*;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.family.SymmetricFamily;
import org.terasology.world.block.management.BlockManager;
import org.terasology.world.liquid.LiquidData;

import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author synopia
 */
public class ExampleHelper {
    public Block air;
    public Block ground;
    public HeightMap map;
    public TestWorld world;

    public class TestWorld implements WorldProvider {
        private Map<Vector3i, Block> blocks = new HashMap<Vector3i, Block>();
        @Override
        public boolean isBlockActive(Vector3i pos) {
            return true;
        }

        @Override
        public boolean isBlockActive(Vector3f pos) {
            return true;
        }

        @Override
        public boolean setBlock(Vector3i pos, Block type, Block oldType) {
            blocks.put(pos, type);
            return true;
        }

        @Override
        public boolean setLiquid(Vector3i pos, LiquidData state, LiquidData oldState) {
            return false;
        }

        @Override
        public LiquidData getLiquid(Vector3i blockPos) {
            return null;
        }

        @Override
        public Block getBlock(Vector3f pos) {
            return getBlock(new Vector3i((int) pos.x, (int) pos.y, (int) pos.z));
        }

        @Override
        public Block getBlock(Vector3i pos) {
            return blocks.containsKey(pos) ? blocks.get(pos) : air;
        }

        @Override
        public byte getLight(Vector3f pos) {
            return 0;
        }

        @Override
        public byte getSunlight(Vector3f pos) {
            return 0;
        }

        @Override
        public byte getTotalLight(Vector3f pos) {
            return 0;
        }

        @Override
        public byte getLight(Vector3i pos) {
            return 0;
        }

        @Override
        public byte getSunlight(Vector3i pos) {
            return 0;
        }

        @Override
        public byte getTotalLight(Vector3i pos) {
            return 0;
        }

        @Override
        public String getTitle() {
            return "";
        }

        @Override
        public String getSeed() {
            return "1";
        }

        @Override
        public WorldInfo getWorldInfo() {
            return null;
        }

        @Override
        public WorldBiomeProvider getBiomeProvider() {
            return null;
        }

        @Override
        public WorldView getLocalView(Vector3i chunk) {
            return null;
        }

        @Override
        public WorldView getWorldViewAround(Vector3i chunk) {
            return null;
        }

        @Override
        public boolean isBlockActive(int x, int y, int z) {
            return true;
        }

        @Override
        public boolean setBlocks(BlockUpdate... updates) {
            for (BlockUpdate update : updates) {
                setBlock(update.getPosition(), update.getNewType(), update.getOldType());
            }
            return true;
        }

        @Override
        public boolean setBlocks(Iterable<BlockUpdate> updates) {
            for (BlockUpdate update : updates) {
                setBlock(update.getPosition(), update.getNewType(), update.getOldType());
            }
            return true;
        }

        public void setBlocks( int y, String... data ) {
            for (int z = 0; z < data.length; z++) {
                String line = data[z];
                for (int x = 0; x < line.length(); x++) {
                    char value = line.charAt(x);
                    switch (value) {
                        case 'X': setBlock(x,y,z,ground, null); break;
                        case ' ': setBlock(x,y,z,air,    null); break;
                    }
                }
            }
        }

        @Override
        public boolean setBlock(int x, int y, int z, Block type, Block oldType) {
            setBlock(new Vector3i(x, y, z), type, oldType);
            return true;
        }

        @Override
        public boolean setLiquid(int x, int y, int z, LiquidData newData, LiquidData oldData) {
            return false;
        }

        @Override
        public LiquidData getLiquid(int x, int y, int z) {
            return null;
        }

        @Override
        public Block getBlock(int x, int y, int z) {
            return getBlock(new Vector3i(x,y,z));
        }

        @Override
        public byte getLight(int x, int y, int z) {
            return 0;
        }

        @Override
        public byte getSunlight(int x, int y, int z) {
            return 0;
        }

        @Override
        public byte getTotalLight(int x, int y, int z) {
            return 0;
        }

        @Override
        public long getTime() {
            return 0;
        }

        @Override
        public void setTime(long time) {
        }

        @Override
        public float getTimeInDays() {
            return 0;
        }

        @Override
        public void setTimeInDays(float time) {
        }

        @Override
        public void dispose() {
        }
    }

    public interface Runner {
        public void run( int x, int y, int z, char value );
    }


    public ExampleHelper() {
        air = new Block();
        air.setPenetrable(true);
        BlockManager.getInstance().addBlockFamily(new SymmetricFamily(new BlockUri("air"), air));

        ground = new Block();
        ground.setPenetrable(false);
        BlockManager.getInstance().addBlockFamily(new SymmetricFamily(new BlockUri("ground"), ground));

        createWorld();
    }

    public void createWorld() {
        world = new TestWorld();
        map = new HeightMap(world, new Vector3i(0,0,0));
    }

    public void setBlock(int x, int y, int z) {
        world.setBlock(x,y,z,ground, null);
    }

    public void addBlocks( String ...lines ) {
        String[][] inputs = split(":", lines);
        for (int j = 0; j < inputs.length; j++) {
            int y=j+1;
            String[] input    = inputs[j];

            world.setBlocks(y, input);
        }
    }

    public void run() {
        map.findWalkableBlocks();
        map.findNeighbors();
        map.findRegions();
        map.findRegionNeighbors();
        map.findFloors();
    }

    public static void process( Runner runner, String... lines ) {
        String[][] expected  = split(":", lines);
        for( int j = 0; j < expected.length; j++ ) {
            int y=j+1;

            for (int z = 0; z < expected[j].length; z++) {
                String line = expected[j][z];
                for (int x = 0; x < line.length(); x++) {
                    char c = line.charAt(x);
                    runner.run(x,y,z,c);
                }
            }
        }
    }

    public static String[][] split(String separator, String ...lines) {
        List<List<String>> table = new ArrayList<List<String>>();
        for (String line : lines) {
            if( line==null || line.length()==0 ) {
                continue;
            }
            String[] parts = line.split(separator);
            for (int i = table.size(); i < parts.length; i++) {
                table.add(new ArrayList<String>());
            }
            for (int i = 0; i < parts.length; i++) {
                table.get(i).add(parts[i]);
            }
        }
        String[][] result = new String[table.size()][lines.length];
        for (int i = 0; i < table.size(); i++) {
            List<String> col = table.get(i);
            for (int j = 0; j < col.size(); j++) {
                result[i][j] = col.get(j);
            }
        }
        return result;
    }
}
