package connectivity;

import graph.Edge;
import graph.Graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class KosarajuSCC {
    private KosarajuSCC() {
    }

    public static SCCResult findComponents(Graph graph) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("SCC needs a directed graph");
        }
        int n = graph.getVertexCount();
        long[] counters = new long[2];

        boolean[] visited = new boolean[n];
        int[] finishOrder = new int[n];
        int finished = 0;
        int[] nextEdge = new int[n];
        Deque<Integer> stack = new ArrayDeque<>();
        for (int start = 0; start < n; start++) {
            if (visited[start]) {
                continue;
            }
            visited[start] = true;
            counters[1]++;
            stack.push(start);
            while (!stack.isEmpty()) {
                int u = stack.peek();
                List<Edge> adjacent = graph.getAdjacent(u);
                if (nextEdge[u] < adjacent.size()) {
                    int v = adjacent.get(nextEdge[u]++).getTo();
                    counters[0]++;
                    if (!visited[v]) {
                        visited[v] = true;
                        counters[1]++;
                        stack.push(v);
                    }
                } else {
                    stack.pop();
                    finishOrder[finished++] = u;
                }
            }
        }

        Graph reversed = graph.reversed();
        int[] componentOf = new int[n];
        Arrays.fill(componentOf, -1);
        List<List<Integer>> components = new ArrayList<>();
        for (int i = n - 1; i >= 0; i--) {
            int start = finishOrder[i];
            if (componentOf[start] != -1) {
                continue;
            }
            int id = components.size();
            List<Integer> members = new ArrayList<>();
            componentOf[start] = id;
            counters[1]++;
            stack.push(start);
            while (!stack.isEmpty()) {
                int u = stack.pop();
                members.add(u);
                for (Edge edge : reversed.getAdjacent(u)) {
                    counters[0]++;
                    int v = edge.getTo();
                    if (componentOf[v] == -1) {
                        componentOf[v] = id;
                        counters[1]++;
                        stack.push(v);
                    }
                }
            }
            components.add(members);
        }
        return new SCCResult(components, componentOf, counters[0], counters[1]);
    }

    public static Graph condensation(Graph graph, SCCResult scc) {
        int count = scc.getComponentCount();
        String[] names = new String[count];
        for (int i = 0; i < count; i++) {
            names[i] = "C" + i;
        }
        Graph dag = new Graph(names, true);
        Set<Long> added = new HashSet<>();
        for (Edge edge : graph.getEdges()) {
            int a = scc.getComponentOf(edge.getFrom());
            int b = scc.getComponentOf(edge.getTo());
            if (a != b && added.add(((long) a << 32) | b)) {
                dag.addEdge(a, b, edge.getWeight());
            }
        }
        return dag;
    }
}
