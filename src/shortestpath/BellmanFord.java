package shortestpath;

import graph.Edge;
import graph.Graph;

import java.util.Arrays;

public class BellmanFord {
    private BellmanFord() {
    }

    public static long[] distances(Graph graph, int source) {
        int n = graph.getVertexCount();
        long[] dist = new long[n];
        Arrays.fill(dist, Dijkstra.INFINITY);
        dist[source] = 0;
        for (int round = 0; round < n - 1; round++) {
            boolean changed = false;
            for (Edge edge : graph.getEdges()) {
                changed |= relax(dist, edge.getFrom(), edge.getTo(), edge.getWeight());
                if (!graph.isDirected()) {
                    changed |= relax(dist, edge.getTo(), edge.getFrom(), edge.getWeight());
                }
            }
            if (!changed) {
                break;
            }
        }
        return dist;
    }

    private static boolean relax(long[] dist, int u, int v, int weight) {
        if (dist[u] != Dijkstra.INFINITY && dist[u] + weight < dist[v]) {
            dist[v] = dist[u] + weight;
            return true;
        }
        return false;
    }
}
