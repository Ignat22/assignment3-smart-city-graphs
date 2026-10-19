package shortestpath;

import graph.Edge;
import graph.Graph;

import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

public class Dijkstra {
    public static final long INFINITY = Long.MAX_VALUE;

    private Dijkstra() {
    }

    public static ShortestPathResult shortestPaths(Graph graph, int source) {
        if (graph.hasNegativeWeight()) {
            throw new IllegalArgumentException("Dijkstra's algorithm requires non-negative edge weights");
        }
        return run(graph, source);
    }

    public static ShortestPathResult shortestPathsWithoutCheck(Graph graph, int source) {
        return run(graph, source);
    }

    private static ShortestPathResult run(Graph graph, int source) {
        int n = graph.getVertexCount();
        if (source < 0 || source >= n) {
            throw new IllegalArgumentException("Source " + source + " is out of range");
        }
        long[] dist = new long[n];
        int[] previous = new int[n];
        boolean[] settled = new boolean[n];
        Arrays.fill(dist, INFINITY);
        Arrays.fill(previous, -1);
        dist[source] = 0;

        long[] comparisons = new long[1];
        Comparator<long[]> byDistance = (a, b) -> {
            comparisons[0]++;
            return Long.compare(a[1], b[1]);
        };
        PriorityQueue<long[]> queue = new PriorityQueue<>(byDistance);
        queue.add(new long[]{source, 0});
        long queueOps = 1;
        long edgeChecks = 0;
        long relaxations = 0;
        long staleEntries = 0;

        while (!queue.isEmpty()) {
            long[] top = queue.poll();
            queueOps++;
            int u = (int) top[0];
            if (settled[u] || top[1] > dist[u]) {
                staleEntries++;
                continue;
            }
            settled[u] = true;
            for (Edge edge : graph.getAdjacent(u)) {
                edgeChecks++;
                int v = graph.neighbor(edge, u);
                long candidate = dist[u] + edge.getWeight();
                if (candidate < dist[v]) {
                    dist[v] = candidate;
                    previous[v] = u;
                    relaxations++;
                    queue.add(new long[]{v, candidate});
                    queueOps++;
                }
            }
        }
        return new ShortestPathResult(source, dist, previous, comparisons[0], edgeChecks, relaxations,
                queueOps, staleEntries);
    }
}
