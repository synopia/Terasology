package org.terasology.pathfinding.model;

import org.terasology.math.Vector3i;

import java.util.*;

/**
 * @author synopia
 */
public class HAStar {

    private Path localPath;
    private PathCache cache;

    private class Node {
        int id;
        float g;
        float f;
        Node p;
        WalkableBlock block;
        Path path;
    }

    private List<Node> nodes = new ArrayList<Node>();
    private Map<WalkableBlock, Integer> nodeMap = new HashMap<WalkableBlock, Integer>();
    private int start;
    private int end;

    private BinaryHeap openList;
    private List<Integer> closedList = new ArrayList<Integer>();

    private boolean useContour;

    public HAStar() {
        this(true);
    }
    public HAStar(boolean useContour) {
        this.useContour = useContour;
        openList = new BinaryHeap(new Comparator<Integer>() {
            @Override
            public int compare(Integer a, Integer b) {
                float fA = nodes.get(a).f;
                float fB = nodes.get(b).f;
                return -(fA < fB ? -1 : ( fA > fB ? 1 : 0 ));
            }
        }, 1024, 1024);
        cache = new PathCache();
    }

    public void reset() {
        cache.clear();
        nodes.clear();
        nodeMap.clear();
        closedList.clear();
        openList.clear();
    }

    private int create(WalkableBlock block) {
        Integer id = nodeMap.get(block);
        if(id!=null) {
            return id;
        }
        Node node = new Node();
        node.block = block;
        node.id = nodes.size();
        nodes.add(node);
        nodeMap.put(block, node.id);
        return node.id;
    }

    public boolean run( WalkableBlock start, WalkableBlock end ) {
        reset();
        this.start = create(start);
        this.end = create(end);

        openList.insert(this.start);

        int maxSize = 0;
        int current = -1;
        while (!openList.isEmpty()) {
            current = openList.removeMin();
            if( current==this.end ) {
                break;
            }
            expand( current );
            closedList.add(current);
            if( openList.getSize()>maxSize ) {
                maxSize = openList.getSize();
            }
        }
        return current==this.end;
    }

    public Path getPath() {
        Path path = new Path();
        Node start = nodes.get(this.start);
        Node current = nodes.get(end);
        while (current!=start && current!=null) {
            if( current.path!=null ) {
                path.addAll(current.path);
            } else {
                path.add(current.block);
            }
            current = current.p;
        }
        if( path.get(path.size()-1)!=start.block ) {
            path.add(start.block);
        }
        return path;
    }

    protected void expand( int current ) {
        Node currentNode = nodes.get(current);
        Floor currentFloor = currentNode.block.region.floor;
        Set<WalkableBlock> neighbors = new HashSet<WalkableBlock>();

        for (WalkableBlock neighbor : currentNode.block.neighbors) {
            if( neighbor==null ) {
                continue;
            }
            neighbors.add(neighbor);
        }
        if( useContour ) {
            for (WalkableBlock contour : currentFloor.getContour()) {
                if( contour==null || contour==currentNode.block ) {
                    continue;
                }
                neighbors.add(contour);
            }
        }
        for (WalkableBlock neighbor : neighbors) {
            expandNeighbor(current, currentNode, neighbor);
        }
    }

    private void expandNeighbor(int current, Node currentNode, WalkableBlock neighbor) {
        int successor = create(neighbor);
        if( closedList.contains(successor) ) {
            return;
        }
        float tentativeG = currentNode.g + c(current, successor);
        Node successorNode = nodes.get(successor);
        if( openList.contains(successor) && tentativeG>=successorNode.g) {
            return;
        }
        successorNode.path = localPath;
        successorNode.p = currentNode;
        successorNode.g = tentativeG;
        successorNode.f = tentativeG + h(successor);

        if( openList.contains(successor) ) {
            openList.update(successor);
        } else {
            openList.insert(successor);
        }
    }

    protected float c( int from, int to ) {
        localPath = null;
        Node fromNode = nodes.get(from);
        Node toNode = nodes.get(to);
        Vector3i fromPos = fromNode.block.getBlockPosition();
        Vector3i toPos = toNode.block.getBlockPosition();
        int diffX = Math.abs(fromPos.x- toPos.x);
        int diffZ = Math.abs(fromPos.z- toPos.z);
        if( (diffX==1 && diffZ==0) || (diffX==0 && diffZ==1) ) {
            if( toNode.block.hasNeighbor(fromNode.block) ) {
                return 1;
            }
        }
        if( diffX==1 && diffZ==1) {
            return BitMap.SQRT_2;
        }

        localPath = cache.findPath(fromNode.block, toNode.block);
        return localPath.size();
    }

    protected float h( int current ) {
        Node fromNode = nodes.get(current);
        Node toNode = nodes.get(end);
        Vector3i fromPos = fromNode.block.getBlockPosition();
        Vector3i toPos = toNode.block.getBlockPosition();
        return (float) Math.abs(fromPos.x-toPos.x) + Math.abs(fromPos.y-toPos.y) + Math.abs(fromPos.z-toPos.z);
    }
}
