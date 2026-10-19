package app;

import connectivity.KosarajuSCC;
import connectivity.SCCResult;
import connectivity.TopoResult;
import connectivity.TopologicalSort;
import graph.Edge;
import graph.Graph;
import mst.Kruskal;
import mst.MSTResult;
import mst.Prim;
import shortestpath.BellmanFord;
import shortestpath.Dijkstra;
import shortestpath.ShortestPathResult;

import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final String[] DISTRICTS = {
            "Central", "Old Town", "University", "Tech Park", "Riverside",
            "Airport", "Industrial", "Harbor", "Green Hills"
    };

    public static void main(String[] args) {
        System.out.println("=== Smart City Infrastructure and Route Planning ===");
        infrastructurePlanning();
        oneWayRoads();
        serviceDependencies();
        routePlanning();
        negativeWeightExample();
    }

    private static void infrastructurePlanning() {
        printHeader("1. Infrastructure planning (MST, cost in million KZT)");
        Graph roads = cityRoads();
        System.out.println("Candidate roads: " + roads.getEdgeCount() + ", districts: " + roads.getVertexCount());
        MSTResult kruskal = Kruskal.findMST(roads);
        MSTResult prim = Prim.findMST(roads);
        printMst(roads, kruskal);
        printMst(roads, prim);
        System.out.println("Same total weight: " + (kruskal.getTotalWeight() == prim.getTotalWeight()));

        Graph split = new Graph(new String[]{"Central", "Old Town", "University", "Island A", "Island B"}, false);
        split.addEdge(0, 1, 4);
        split.addEdge(1, 2, 3);
        split.addEdge(0, 2, 6);
        split.addEdge(3, 4, 5);
        MSTResult forest = Kruskal.findMST(split);
        System.out.println();
        System.out.println("Disconnected plan: connected=" + forest.isConnected()
                + ", components=" + forest.getComponents() + ", forest weight=" + forest.getTotalWeight());
    }

    private static void oneWayRoads() {
        printHeader("2. One-way roads (strongly connected components)");
        String[] names = {"Central", "Old Town", "University", "Tech Park", "Riverside", "Airport", "Harbor"};
        Graph oneWay = new Graph(names, true);
        oneWay.addEdge(0, 1);
        oneWay.addEdge(1, 2);
        oneWay.addEdge(2, 0);
        oneWay.addEdge(2, 3);
        oneWay.addEdge(3, 4);
        oneWay.addEdge(4, 3);
        oneWay.addEdge(4, 5);
        oneWay.addEdge(6, 5);
        SCCResult scc = KosarajuSCC.findComponents(oneWay);
        System.out.println("Strongly connected components: " + scc.getComponentCount());
        for (int i = 0; i < scc.getComponentCount(); i++) {
            System.out.println("  SCC " + i + ": " + names(oneWay, scc.getComponents().get(i)));
        }
        Graph condensed = KosarajuSCC.condensation(oneWay, scc);
        TopoResult order = TopologicalSort.sort(condensed);
        System.out.println("Condensation graph is a DAG: " + order.isDag() + ", component order: " + order.getOrder());
    }

    private static void serviceDependencies() {
        printHeader("3. Service dependencies (topological ordering)");
        String[] services = {"Power grid", "Water supply", "Fiber network", "Traffic lights",
                "Metro", "Smart parking", "City app"};
        Graph deps = new Graph(services, true);
        deps.addEdge(0, 1);
        deps.addEdge(0, 2);
        deps.addEdge(0, 3);
        deps.addEdge(2, 3);
        deps.addEdge(0, 4);
        deps.addEdge(3, 4);
        deps.addEdge(2, 5);
        deps.addEdge(5, 6);
        deps.addEdge(4, 6);
        TopoResult result = TopologicalSort.sort(deps);
        System.out.println("Is DAG: " + result.isDag());
        System.out.println("Build order: " + names(deps, result.getOrder()));
        System.out.println("Order is valid: " + TopologicalSort.isValidOrder(deps, result.getOrder()));

        deps.addEdge(6, 2);
        TopoResult cyclic = TopologicalSort.sort(deps);
        System.out.println("After adding 'City app -> Fiber network':");
        System.out.println("Is DAG: " + cyclic.isDag() + " (topological ordering impossible, cycle detected)");
        System.out.println("Vertices on or after the cycle: " + names(deps, cyclic.getVerticesOnOrAfterCycle()));
    }

    private static void routePlanning() {
        printHeader("4. Route planning (Dijkstra, travel distance in km)");
        Graph roads = cityRoads();
        Graph withIsolated = new Graph(append(DISTRICTS, "New Suburb"), false);
        for (Edge edge : roads.getEdges()) {
            withIsolated.addEdge(edge.getFrom(), edge.getTo(), edge.getWeight());
        }
        int source = 0;
        ShortestPathResult result = Dijkstra.shortestPaths(withIsolated, source);
        System.out.println("Source: " + withIsolated.getName(source));
        for (int v = 0; v < withIsolated.getVertexCount(); v++) {
            String name = withIsolated.getName(v);
            if (result.isReachable(v)) {
                System.out.printf("  %-12s dist=%3d  path=%s%n", name, result.getDistance(v),
                        names(withIsolated, result.getPath(v)));
            } else {
                System.out.printf("  %-12s unreachable%n", name);
            }
        }
    }

    private static void negativeWeightExample() {
        printHeader("5. Why Dijkstra fails with negative weights");
        Graph g = new Graph(new String[]{"A", "B", "C", "D"}, true);
        g.addEdge(0, 1, 2);
        g.addEdge(0, 2, 5);
        g.addEdge(2, 1, -4);
        g.addEdge(1, 3, 1);
        try {
            Dijkstra.shortestPaths(g, 0);
        } catch (IllegalArgumentException e) {
            System.out.println("Checked version refuses: " + e.getMessage());
        }
        ShortestPathResult wrong = Dijkstra.shortestPathsWithoutCheck(g, 0);
        long[] correct = BellmanFord.distances(g, 0);
        System.out.println("dist(A->D) Dijkstra without check = " + wrong.getDistance(3)
                + ", Bellman-Ford = " + correct[3]);
        System.out.println("B was settled with distance 2 before the cheaper path A->C->B (1) was found,");
        System.out.println("so the edge B->D was never relaxed again.");
    }

    private static Graph cityRoads() {
        Graph g = new Graph(DISTRICTS, false);
        g.addEdge(0, 1, 4);
        g.addEdge(0, 2, 7);
        g.addEdge(0, 3, 9);
        g.addEdge(1, 2, 3);
        g.addEdge(1, 4, 6);
        g.addEdge(2, 3, 4);
        g.addEdge(2, 4, 5);
        g.addEdge(3, 5, 12);
        g.addEdge(3, 6, 8);
        g.addEdge(4, 6, 8);
        g.addEdge(4, 7, 10);
        g.addEdge(5, 6, 11);
        g.addEdge(6, 7, 6);
        g.addEdge(6, 8, 7);
        g.addEdge(7, 8, 9);
        g.addEdge(5, 8, 14);
        return g;
    }

    private static void printMst(Graph graph, MSTResult result) {
        System.out.println(result);
        for (Edge edge : result.getEdges()) {
            System.out.printf("  %-11s - %-11s %3d%n", graph.getName(edge.getFrom()), graph.getName(edge.getTo()),
                    edge.getWeight());
        }
    }

    private static List<String> names(Graph graph, List<Integer> vertices) {
        List<String> result = new ArrayList<>();
        for (int v : vertices) {
            result.add(graph.getName(v));
        }
        return result;
    }

    private static String[] append(String[] array, String value) {
        String[] result = new String[array.length + 1];
        System.arraycopy(array, 0, result, 0, array.length);
        result[array.length] = value;
        return result;
    }

    private static void printHeader(String title) {
        System.out.println();
        System.out.println("--- " + title + " ---");
    }
}
