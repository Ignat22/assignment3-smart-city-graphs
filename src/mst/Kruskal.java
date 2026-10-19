package mst;

import dsu.DisjointSet;
import graph.Edge;
import graph.Graph;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Kruskal {
    private Kruskal() {
    }

    public static MSTResult findMST(Graph graph) {
        if (graph.isDirected()) {
            throw new IllegalArgumentException("Kruskal's algorithm needs an undirected graph");
        }
        int n = graph.getVertexCount();
        long[] comparisons = new long[1];
        List<Edge> sorted = new ArrayList<>(graph.getEdges());
        Comparator<Edge> byWeight = (a, b) -> {
            comparisons[0]++;
            return Integer.compare(a.getWeight(), b.getWeight());
        };
        sorted.sort(byWeight);

        DisjointSet dsu = new DisjointSet(n);
        List<Edge> tree = new ArrayList<>();
        long weight = 0;
        long examined = 0;
        for (Edge edge : sorted) {
            if (tree.size() == n - 1) {
                break;
            }
            examined++;
            if (dsu.union(edge.getFrom(), edge.getTo())) {
                tree.add(edge);
                weight += edge.getWeight();
            }
        }
        return new MSTResult("Kruskal", tree, weight, dsu.getSetCount(), comparisons[0], examined,
                dsu.getFindCalls(), dsu.getUnionCalls(), 0);
    }
}
