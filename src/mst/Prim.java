package mst;

import graph.Edge;
import graph.Graph;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class Prim {
    private Prim() {
    }

    public static MSTResult findMST(Graph graph) {
        if (graph.isDirected()) {
            throw new IllegalArgumentException("Prim's algorithm needs an undirected graph");
        }
        int n = graph.getVertexCount();
        long[] comparisons = new long[1];
        Comparator<Edge> byWeight = (a, b) -> {
            comparisons[0]++;
            return Integer.compare(a.getWeight(), b.getWeight());
        };
        PriorityQueue<Edge> queue = new PriorityQueue<>(byWeight);
        boolean[] inTree = new boolean[n];
        List<Edge> tree = new ArrayList<>();
        long weight = 0;
        long examined = 0;
        long queueOps = 0;
        int components = 0;

        for (int start = 0; start < n; start++) {
            if (inTree[start]) {
                continue;
            }
            components++;
            inTree[start] = true;
            for (Edge edge : graph.getAdjacent(start)) {
                examined++;
                if (!inTree[edge.other(start)]) {
                    queue.add(edge);
                    queueOps++;
                }
            }
            while (!queue.isEmpty()) {
                Edge edge = queue.poll();
                queueOps++;
                int u = edge.getFrom();
                int v = edge.getTo();
                if (inTree[u] && inTree[v]) {
                    continue;
                }
                int next = inTree[u] ? v : u;
                inTree[next] = true;
                tree.add(edge);
                weight += edge.getWeight();
                for (Edge adjacent : graph.getAdjacent(next)) {
                    examined++;
                    if (!inTree[adjacent.other(next)]) {
                        queue.add(adjacent);
                        queueOps++;
                    }
                }
            }
        }
        return new MSTResult("Prim", tree, weight, components, comparisons[0], examined, 0, 0, queueOps);
    }
}
