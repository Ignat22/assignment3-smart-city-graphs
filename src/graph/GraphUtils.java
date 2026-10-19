package graph;

import java.util.ArrayDeque;
import java.util.Deque;

public class GraphUtils {
    private GraphUtils() {
    }

    public static int countComponents(Graph graph) {
        int n = graph.getVertexCount();
        boolean[] visited = new boolean[n];
        int components = 0;
        Deque<Integer> stack = new ArrayDeque<>();
        for (int start = 0; start < n; start++) {
            if (visited[start]) {
                continue;
            }
            components++;
            visited[start] = true;
            stack.push(start);
            while (!stack.isEmpty()) {
                int u = stack.pop();
                for (Edge edge : graph.getAdjacent(u)) {
                    int v = graph.neighbor(edge, u);
                    if (!visited[v]) {
                        visited[v] = true;
                        stack.push(v);
                    }
                }
            }
        }
        return components;
    }

    public static boolean isConnected(Graph graph) {
        return graph.getVertexCount() <= 1 || countComponents(graph) == 1;
    }
}
