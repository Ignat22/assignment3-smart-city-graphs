package connectivity;

import graph.Edge;
import graph.Graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class TopologicalSort {
    private TopologicalSort() {
    }

    public static TopoResult sort(Graph graph) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("Topological ordering needs a directed graph");
        }
        int n = graph.getVertexCount();
        int[] inDegree = new int[n];
        long examined = 0;
        long queueOps = 0;
        for (Edge edge : graph.getEdges()) {
            inDegree[edge.getTo()]++;
            examined++;
        }
        Deque<Integer> queue = new ArrayDeque<>();
        for (int v = 0; v < n; v++) {
            if (inDegree[v] == 0) {
                queue.add(v);
                queueOps++;
            }
        }
        List<Integer> order = new ArrayList<>(n);
        while (!queue.isEmpty()) {
            int u = queue.poll();
            queueOps++;
            order.add(u);
            for (Edge edge : graph.getAdjacent(u)) {
                examined++;
                int v = edge.getTo();
                inDegree[v]--;
                if (inDegree[v] == 0) {
                    queue.add(v);
                    queueOps++;
                }
            }
        }
        if (order.size() < n) {
            List<Integer> blocked = new ArrayList<>();
            for (int v = 0; v < n; v++) {
                if (inDegree[v] > 0) {
                    blocked.add(v);
                }
            }
            return new TopoResult(false, order, blocked, examined, queueOps);
        }
        return new TopoResult(true, order, new ArrayList<>(), examined, queueOps);
    }

    public static boolean isValidOrder(Graph graph, List<Integer> order) {
        int n = graph.getVertexCount();
        if (order == null || order.size() != n) {
            return false;
        }
        int[] position = new int[n];
        boolean[] seen = new boolean[n];
        for (int i = 0; i < order.size(); i++) {
            int v = order.get(i);
            if (v < 0 || v >= n || seen[v]) {
                return false;
            }
            seen[v] = true;
            position[v] = i;
        }
        for (Edge edge : graph.getEdges()) {
            if (position[edge.getFrom()] >= position[edge.getTo()]) {
                return false;
            }
        }
        return true;
    }
}
