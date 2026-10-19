package graph;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class GraphGenerator {
    public static final int MIN_WEIGHT = 1;
    public static final int MAX_WEIGHT = 100;

    private GraphGenerator() {
    }

    public static Graph connectedUndirected(int vertices, int edges, long seed) {
        long maxEdges = (long) vertices * (vertices - 1) / 2;
        if (vertices > 0 && edges < vertices - 1) {
            throw new IllegalArgumentException("A connected graph needs at least V-1 edges");
        }
        if (edges > maxEdges) {
            throw new IllegalArgumentException("Too many edges for a simple undirected graph");
        }
        Random random = new Random(seed);
        Graph graph = new Graph(vertices, false);
        Set<Long> used = new HashSet<>();
        int[] order = shuffledVertices(vertices, random);
        for (int i = 1; i < vertices; i++) {
            int u = order[i];
            int v = order[random.nextInt(i)];
            used.add(key(u, v, false));
            graph.addEdge(u, v, randomWeight(random));
        }
        while (graph.getEdgeCount() < edges) {
            int u = random.nextInt(vertices);
            int v = random.nextInt(vertices);
            if (u == v || !used.add(key(u, v, false))) {
                continue;
            }
            graph.addEdge(u, v, randomWeight(random));
        }
        return graph;
    }

    public static Graph randomDirected(int vertices, int edges, long seed) {
        long maxEdges = (long) vertices * (vertices - 1);
        if (edges > maxEdges) {
            throw new IllegalArgumentException("Too many edges for a simple directed graph");
        }
        Random random = new Random(seed);
        Graph graph = new Graph(vertices, true);
        Set<Long> used = new HashSet<>();
        while (graph.getEdgeCount() < edges) {
            int u = random.nextInt(vertices);
            int v = random.nextInt(vertices);
            if (u == v || !used.add(key(u, v, true))) {
                continue;
            }
            graph.addEdge(u, v, randomWeight(random));
        }
        return graph;
    }

    public static Graph randomDag(int vertices, int edges, long seed) {
        long maxEdges = (long) vertices * (vertices - 1) / 2;
        if (edges > maxEdges) {
            throw new IllegalArgumentException("Too many edges for a DAG");
        }
        Random random = new Random(seed);
        int[] rankToVertex = shuffledVertices(vertices, random);
        Graph graph = new Graph(vertices, true);
        Set<Long> used = new HashSet<>();
        while (graph.getEdgeCount() < edges) {
            int a = random.nextInt(vertices);
            int b = random.nextInt(vertices);
            if (a == b) {
                continue;
            }
            int low = Math.min(a, b);
            int high = Math.max(a, b);
            int u = rankToVertex[low];
            int v = rankToVertex[high];
            if (!used.add(key(u, v, true))) {
                continue;
            }
            graph.addEdge(u, v, randomWeight(random));
        }
        return graph;
    }

    public static Graph dagWithBackEdge(int vertices, int edges, long seed) {
        if (vertices < 4 || edges < 2) {
            throw new IllegalArgumentException("Need at least 4 vertices and 2 edges");
        }
        Random random = new Random(seed);
        int[] rankToVertex = shuffledVertices(vertices, random);
        int middle = rankToVertex[vertices / 2];
        int late = rankToVertex[3 * vertices / 4];
        Graph graph = new Graph(vertices, true);
        Set<Long> used = new HashSet<>();
        used.add(key(middle, late, true));
        graph.addEdge(middle, late, randomWeight(random));
        while (graph.getEdgeCount() < edges - 1) {
            int a = random.nextInt(vertices);
            int b = random.nextInt(vertices);
            if (a == b) {
                continue;
            }
            int u = rankToVertex[Math.min(a, b)];
            int v = rankToVertex[Math.max(a, b)];
            if (!used.add(key(u, v, true))) {
                continue;
            }
            graph.addEdge(u, v, randomWeight(random));
        }
        graph.addEdge(late, middle, randomWeight(random));
        return graph;
    }

    private static int[] shuffledVertices(int vertices, Random random) {
        int[] order = new int[vertices];
        for (int i = 0; i < vertices; i++) {
            order[i] = i;
        }
        for (int i = vertices - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int tmp = order[i];
            order[i] = order[j];
            order[j] = tmp;
        }
        return order;
    }

    private static int randomWeight(Random random) {
        return MIN_WEIGHT + random.nextInt(MAX_WEIGHT - MIN_WEIGHT + 1);
    }

    private static long key(int u, int v, boolean directed) {
        if (!directed && u > v) {
            int tmp = u;
            u = v;
            v = tmp;
        }
        return ((long) u << 32) | (v & 0xffffffffL);
    }
}
