package tests;

import connectivity.KosarajuSCC;
import connectivity.SCCResult;
import connectivity.TopoResult;
import connectivity.TopologicalSort;
import dsu.DisjointSet;
import graph.Edge;
import graph.Graph;
import graph.GraphGenerator;
import graph.GraphUtils;
import mst.Kruskal;
import mst.MSTResult;
import mst.Prim;
import shortestpath.BellmanFord;
import shortestpath.Dijkstra;
import shortestpath.ShortestPathResult;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class AllTests {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        testDsuBasics();
        testDsuVariantsAgree();
        testDsuRankKeepsTreesShallow();

        testMstSmallKnownGraph();
        testMstSingleVertex();
        testMstEmptyGraph();
        testMstDisconnected();
        testMstMultipleValidTrees();
        testMstDuplicateWeights();
        testMstAgainstBruteForce();
        testKruskalEqualsPrimOnRandomGraphs();
        testMstEdgeCountAndConnectivity();

        testTopoSmallDag();
        testTopoSingleVertex();
        testTopoDetectsCycle();
        testTopoSelfLoop();
        testTopoRandomDags();
        testTopoRandomCyclicGraphs();

        testSccKnownGraph();
        testSccSingleVertex();
        testSccDagHasSingletonComponents();
        testSccEveryVertexOnce();
        testSccAgainstReachability();
        testCondensationIsDag();

        testDijkstraSmallGraph();
        testDijkstraSingleVertex();
        testDijkstraUnreachable();
        testDijkstraDuplicateWeights();
        testDijkstraPathsAreConsistent();
        testDijkstraAgainstBellmanFord();
        testDijkstraRejectsNegativeWeights();
        testDijkstraNegativeCounterExample();

        testGeneratorReproducible();
        testGeneratorConnectedAndSimple();

        System.out.println();
        System.out.println("Passed: " + passed + ", failed: " + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void testDsuBasics() {
        DisjointSet dsu = new DisjointSet(6);
        check("DSU starts with n sets", dsu.getSetCount() == 6);
        check("union of different sets returns true", dsu.union(0, 1));
        check("union of same set returns false", !dsu.union(1, 0));
        dsu.union(2, 3);
        dsu.union(1, 3);
        check("0 and 2 connected after unions", dsu.connected(0, 2));
        check("4 and 5 still separate", !dsu.connected(4, 5));
        check("set count after 3 successful unions", dsu.getSetCount() == 3);
    }

    private static void testDsuVariantsAgree() {
        Random random = new Random(7);
        int n = 300;
        DisjointSet[] variants = {
                new DisjointSet(n, false, false),
                new DisjointSet(n, false, true),
                new DisjointSet(n, true, false),
                new DisjointSet(n, true, true)
        };
        boolean same = true;
        for (int i = 0; i < 400; i++) {
            int a = random.nextInt(n);
            int b = random.nextInt(n);
            boolean first = variants[0].union(a, b);
            for (int k = 1; k < variants.length; k++) {
                same &= variants[k].union(a, b) == first;
            }
        }
        for (int i = 0; i < 1000; i++) {
            int a = random.nextInt(n);
            int b = random.nextInt(n);
            boolean first = variants[0].connected(a, b);
            for (int k = 1; k < variants.length; k++) {
                same &= variants[k].connected(a, b) == first;
            }
        }
        check("all DSU variants give the same partition", same);
    }

    private static void testDsuRankKeepsTreesShallow() {
        int n = 1024;
        DisjointSet naive = new DisjointSet(n, false, false);
        DisjointSet byRank = new DisjointSet(n, false, true);
        for (int i = 0; i < n - 1; i++) {
            naive.union(i, i + 1);
            byRank.union(i, i + 1);
        }
        check("naive union builds a chain of height n-1", naive.height() == n - 1);
        check("union by rank height <= log2(n)", byRank.height() <= 10);
        DisjointSet compressed = new DisjointSet(n, true, false);
        for (int i = 0; i < n - 1; i++) {
            compressed.union(i, i + 1);
        }
        compressed.find(0);
        check("path compression flattens the path of a found element", compressed.find(0) == compressed.find(n - 1)
                && compressed.height() < n - 1);
    }

    private static void testMstSmallKnownGraph() {
        Graph g = new Graph(5, false);
        g.addEdge(0, 1, 2);
        g.addEdge(0, 3, 6);
        g.addEdge(1, 2, 3);
        g.addEdge(1, 3, 8);
        g.addEdge(1, 4, 5);
        g.addEdge(2, 4, 7);
        g.addEdge(3, 4, 9);
        MSTResult k = Kruskal.findMST(g);
        MSTResult p = Prim.findMST(g);
        check("Kruskal weight on known graph = 16", k.getTotalWeight() == 16);
        check("Prim weight on known graph = 16", p.getTotalWeight() == 16);
        check("MST has V-1 edges", k.getEdges().size() == 4 && p.getEdges().size() == 4);
        check("known graph is connected", k.isConnected() && p.isConnected());
    }

    private static void testMstSingleVertex() {
        Graph g = new Graph(1, false);
        MSTResult k = Kruskal.findMST(g);
        MSTResult p = Prim.findMST(g);
        check("single vertex MST has weight 0", k.getTotalWeight() == 0 && p.getTotalWeight() == 0);
        check("single vertex MST has no edges", k.getEdges().isEmpty() && p.getEdges().isEmpty());
        check("single vertex graph is connected", k.isConnected() && p.isConnected());
    }

    private static void testMstEmptyGraph() {
        Graph g = new Graph(0, false);
        check("empty graph MST weight 0", Kruskal.findMST(g).getTotalWeight() == 0
                && Prim.findMST(g).getTotalWeight() == 0);
    }

    private static void testMstDisconnected() {
        Graph g = new Graph(6, false);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 2, 2);
        g.addEdge(0, 2, 3);
        g.addEdge(3, 4, 4);
        MSTResult k = Kruskal.findMST(g);
        MSTResult p = Prim.findMST(g);
        check("Kruskal detects disconnected graph", !k.isConnected());
        check("Prim detects disconnected graph", !p.isConnected());
        check("disconnected graph has 3 components", k.getComponents() == 3 && p.getComponents() == 3);
        check("spanning forest weight = 7", k.getTotalWeight() == 7 && p.getTotalWeight() == 7);
        check("spanning forest has V - components edges", k.getEdges().size() == 3 && p.getEdges().size() == 3);
        check("BFS agrees the graph is disconnected", !GraphUtils.isConnected(g));
    }

    private static void testMstMultipleValidTrees() {
        Graph g = new Graph(4, false);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 2, 1);
        g.addEdge(2, 3, 1);
        g.addEdge(3, 0, 1);
        g.addEdge(0, 2, 5);
        MSTResult k = Kruskal.findMST(g);
        MSTResult p = Prim.findMST(g);
        check("square with equal sides has MST weight 3", k.getTotalWeight() == 3 && p.getTotalWeight() == 3);
        check("both trees avoid the heavy diagonal", !containsWeight(k, 5) && !containsWeight(p, 5));
        check("both results are spanning trees", isSpanningTree(g, k.getEdges()) && isSpanningTree(g, p.getEdges()));
    }

    private static void testMstDuplicateWeights() {
        Graph g = new Graph(6, false);
        int[][] edges = {{0, 1}, {0, 2}, {1, 2}, {1, 3}, {2, 4}, {3, 4}, {3, 5}, {4, 5}};
        for (int[] e : edges) {
            g.addEdge(e[0], e[1], 10);
        }
        g.addEdge(0, 5, 10);
        MSTResult k = Kruskal.findMST(g);
        MSTResult p = Prim.findMST(g);
        check("all weights equal: MST weight = 10*(V-1)", k.getTotalWeight() == 50 && p.getTotalWeight() == 50);
        check("duplicate weights still give spanning trees", isSpanningTree(g, k.getEdges())
                && isSpanningTree(g, p.getEdges()));
    }

    private static void testMstAgainstBruteForce() {
        Random random = new Random(11);
        boolean ok = true;
        for (int trial = 0; trial < 40; trial++) {
            int v = 3 + random.nextInt(4);
            int maxE = v * (v - 1) / 2;
            int e = Math.min(maxE, v - 1 + random.nextInt(maxE - v + 2));
            Graph g = GraphGenerator.connectedUndirected(v, e, 1000 + trial);
            long best = bruteForceMst(g);
            ok &= Kruskal.findMST(g).getTotalWeight() == best;
            ok &= Prim.findMST(g).getTotalWeight() == best;
        }
        check("Kruskal and Prim match brute-force MST on 40 small graphs", ok);
    }

    private static void testKruskalEqualsPrimOnRandomGraphs() {
        boolean ok = true;
        int[][] sizes = {{10, 30}, {50, 200}, {100, 500}, {1000, 5000}};
        for (int[] size : sizes) {
            for (int seed = 0; seed < 5; seed++) {
                Graph g = GraphGenerator.connectedUndirected(size[0], size[1], seed);
                ok &= Kruskal.findMST(g).getTotalWeight() == Prim.findMST(g).getTotalWeight();
            }
        }
        check("Kruskal and Prim weights equal on 20 random connected graphs", ok);
    }

    private static void testMstEdgeCountAndConnectivity() {
        Graph g = GraphGenerator.connectedUndirected(200, 800, 3);
        MSTResult k = Kruskal.findMST(g);
        MSTResult p = Prim.findMST(g);
        check("random MST is a spanning tree (Kruskal)", isSpanningTree(g, k.getEdges()));
        check("random MST is a spanning tree (Prim)", isSpanningTree(g, p.getEdges()));
    }

    private static void testTopoSmallDag() {
        Graph g = new Graph(6, true);
        g.addEdge(5, 2);
        g.addEdge(5, 0);
        g.addEdge(4, 0);
        g.addEdge(4, 1);
        g.addEdge(2, 3);
        g.addEdge(3, 1);
        TopoResult r = TopologicalSort.sort(g);
        check("small DAG recognised", r.isDag());
        check("small DAG order is valid", TopologicalSort.isValidOrder(g, r.getOrder()));
    }

    private static void testTopoSingleVertex() {
        Graph g = new Graph(1, true);
        TopoResult r = TopologicalSort.sort(g);
        check("single vertex is a DAG with order [0]", r.isDag() && r.getOrder().size() == 1);
    }

    private static void testTopoDetectsCycle() {
        Graph g = new Graph(5, true);
        g.addEdge(0, 1);
        g.addEdge(1, 2);
        g.addEdge(2, 3);
        g.addEdge(3, 1);
        g.addEdge(3, 4);
        TopoResult r = TopologicalSort.sort(g);
        check("cycle 1->2->3->1 detected", !r.isDag());
        check("order is null for cyclic graph", r.getOrder() == null);
        check("cycle vertices reported as blocked", r.getVerticesOnOrAfterCycle().containsAll(List.of(1, 2, 3)));
    }

    private static void testTopoSelfLoop() {
        Graph g = new Graph(2, true);
        g.addEdge(0, 1);
        g.addEdge(1, 1);
        check("self loop detected as cycle", !TopologicalSort.sort(g).isDag());
    }

    private static void testTopoRandomDags() {
        boolean ok = true;
        int[][] sizes = {{10, 30}, {100, 500}, {1000, 5000}};
        for (int[] size : sizes) {
            for (int seed = 0; seed < 5; seed++) {
                Graph g = GraphGenerator.randomDag(size[0], size[1], seed);
                TopoResult r = TopologicalSort.sort(g);
                ok &= r.isDag() && TopologicalSort.isValidOrder(g, r.getOrder());
            }
        }
        check("topological order valid for 15 random DAGs", ok);
    }

    private static void testTopoRandomCyclicGraphs() {
        boolean ok = true;
        int[][] sizes = {{10, 30}, {100, 500}, {1000, 5000}};
        for (int[] size : sizes) {
            for (int seed = 0; seed < 5; seed++) {
                Graph g = GraphGenerator.randomDirected(size[0], size[1], seed);
                boolean hasCycle = KosarajuSCC.findComponents(g).getComponentCount() < g.getVertexCount();
                ok &= TopologicalSort.sort(g).isDag() != hasCycle;
            }
        }
        check("cycle detection agrees with SCC on 15 random directed graphs", ok);
        boolean backEdgeOk = true;
        for (int[] size : sizes) {
            Graph g = GraphGenerator.dagWithBackEdge(size[0], size[1], 42);
            TopoResult r = TopologicalSort.sort(g);
            backEdgeOk &= !r.isDag() && g.getEdgeCount() == size[1] && r.getPartialOrder().size() >= size[0] / 2;
        }
        check("DAG with one back edge is detected as cyclic after ordering at least half the vertices", backEdgeOk);
    }

    private static void testSccKnownGraph() {
        Graph g = new Graph(8, true);
        g.addEdge(0, 1);
        g.addEdge(1, 2);
        g.addEdge(2, 0);
        g.addEdge(2, 3);
        g.addEdge(3, 4);
        g.addEdge(4, 5);
        g.addEdge(5, 3);
        g.addEdge(6, 5);
        g.addEdge(6, 7);
        g.addEdge(7, 6);
        SCCResult r = KosarajuSCC.findComponents(g);
        check("known graph has 3 SCCs", r.getComponentCount() == 3);
        check("0,1,2 in same SCC", r.getComponentOf(0) == r.getComponentOf(1) && r.getComponentOf(1) == r.getComponentOf(2));
        check("3,4,5 in same SCC", r.getComponentOf(3) == r.getComponentOf(4) && r.getComponentOf(4) == r.getComponentOf(5));
        check("6,7 in same SCC", r.getComponentOf(6) == r.getComponentOf(7));
        check("0 and 3 in different SCCs", r.getComponentOf(0) != r.getComponentOf(3));
    }

    private static void testSccSingleVertex() {
        SCCResult r = KosarajuSCC.findComponents(new Graph(1, true));
        check("single vertex forms one SCC", r.getComponentCount() == 1 && r.getComponents().get(0).equals(List.of(0)));
    }

    private static void testSccDagHasSingletonComponents() {
        Graph g = GraphGenerator.randomDag(100, 500, 9);
        check("every SCC of a DAG is a single vertex", KosarajuSCC.findComponents(g).getComponentCount() == 100);
    }

    private static void testSccEveryVertexOnce() {
        boolean ok = true;
        int[][] sizes = {{10, 30}, {100, 500}, {1000, 5000}, {1000, 1200}};
        for (int[] size : sizes) {
            Graph g = GraphGenerator.randomDirected(size[0], size[1], 21);
            SCCResult r = KosarajuSCC.findComponents(g);
            int[] seen = new int[size[0]];
            for (List<Integer> component : r.getComponents()) {
                for (int v : component) {
                    seen[v]++;
                }
            }
            for (int count : seen) {
                ok &= count == 1;
            }
        }
        check("every vertex belongs to exactly one SCC", ok);
    }

    private static void testSccAgainstReachability() {
        Random random = new Random(5);
        boolean ok = true;
        for (int trial = 0; trial < 30; trial++) {
            int n = 2 + random.nextInt(15);
            int e = random.nextInt(Math.min(n * (n - 1), 3 * n) + 1);
            Graph g = GraphGenerator.randomDirected(n, e, 500 + trial);
            boolean[][] reach = transitiveClosure(g);
            SCCResult r = KosarajuSCC.findComponents(g);
            for (int a = 0; a < n; a++) {
                for (int b = 0; b < n; b++) {
                    boolean mutual = reach[a][b] && reach[b][a];
                    ok &= mutual == (r.getComponentOf(a) == r.getComponentOf(b));
                }
            }
        }
        check("SCCs match mutual reachability on 30 random graphs", ok);
    }

    private static void testCondensationIsDag() {
        Graph g = GraphGenerator.randomDirected(1000, 1500, 4);
        SCCResult r = KosarajuSCC.findComponents(g);
        Graph dag = KosarajuSCC.condensation(g, r);
        TopoResult t = TopologicalSort.sort(dag);
        check("condensation graph is always a DAG", t.isDag() && TopologicalSort.isValidOrder(dag, t.getOrder()));
    }

    private static void testDijkstraSmallGraph() {
        Graph g = new Graph(5, true);
        g.addEdge(0, 1, 10);
        g.addEdge(0, 2, 3);
        g.addEdge(2, 1, 4);
        g.addEdge(1, 3, 2);
        g.addEdge(2, 3, 8);
        g.addEdge(3, 4, 7);
        g.addEdge(2, 4, 2);
        ShortestPathResult r = Dijkstra.shortestPaths(g, 0);
        check("dist to 1 = 7 via 2", r.getDistance(1) == 7);
        check("dist to 3 = 9", r.getDistance(3) == 9);
        check("dist to 4 = 5", r.getDistance(4) == 5);
        check("path to 3 is 0-2-1-3", r.getPath(3).equals(List.of(0, 2, 1, 3)));
    }

    private static void testDijkstraSingleVertex() {
        ShortestPathResult r = Dijkstra.shortestPaths(new Graph(1, false), 0);
        check("single vertex distance 0 and path [0]", r.getDistance(0) == 0 && r.getPath(0).equals(List.of(0)));
    }

    private static void testDijkstraUnreachable() {
        Graph g = new Graph(4, true);
        g.addEdge(0, 1, 5);
        g.addEdge(2, 3, 1);
        g.addEdge(3, 0, 1);
        ShortestPathResult r = Dijkstra.shortestPaths(g, 0);
        check("vertex 1 reachable", r.isReachable(1) && r.getDistance(1) == 5);
        check("vertices 2 and 3 unreachable", !r.isReachable(2) && !r.isReachable(3));
        check("unreachable path is empty", r.getPath(3).isEmpty());
        check("unreachable distance is infinity", r.getDistance(2) == Dijkstra.INFINITY);
        check("reachable count = 2", r.getReachableCount() == 2);
    }

    private static void testDijkstraDuplicateWeights() {
        Graph g = new Graph(4, false);
        g.addEdge(0, 1, 1);
        g.addEdge(0, 2, 1);
        g.addEdge(1, 3, 1);
        g.addEdge(2, 3, 1);
        ShortestPathResult r = Dijkstra.shortestPaths(g, 0);
        List<Integer> path = r.getPath(3);
        check("equal-cost paths give distance 2", r.getDistance(3) == 2);
        check("chosen path is one of the shortest", path.equals(List.of(0, 1, 3)) || path.equals(List.of(0, 2, 3)));
    }

    private static void testDijkstraPathsAreConsistent() {
        Graph g = GraphGenerator.connectedUndirected(300, 1500, 8);
        ShortestPathResult r = Dijkstra.shortestPaths(g, 0);
        boolean ok = true;
        for (int v = 0; v < g.getVertexCount(); v++) {
            ok &= pathLength(g, r.getPath(v)) == r.getDistance(v);
        }
        check("path weights equal reported distances", ok);
    }

    private static void testDijkstraAgainstBellmanFord() {
        boolean ok = true;
        int[][] sizes = {{10, 30}, {100, 500}, {1000, 5000}};
        for (int[] size : sizes) {
            for (int seed = 0; seed < 5; seed++) {
                Graph undirected = GraphGenerator.connectedUndirected(size[0], size[1], seed);
                Graph directed = GraphGenerator.randomDirected(size[0], size[1], seed);
                ok &= sameDistances(undirected, seed % size[0]);
                ok &= sameDistances(directed, seed % size[0]);
            }
        }
        check("Dijkstra matches Bellman-Ford on 30 random graphs", ok);
    }

    private static void testDijkstraRejectsNegativeWeights() {
        Graph g = new Graph(2, true);
        g.addEdge(0, 1, -1);
        boolean thrown = false;
        try {
            Dijkstra.shortestPaths(g, 0);
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        check("Dijkstra rejects negative weights", thrown);
    }

    private static void testDijkstraNegativeCounterExample() {
        Graph g = new Graph(4, true);
        g.addEdge(0, 1, 2);
        g.addEdge(0, 2, 5);
        g.addEdge(2, 1, -4);
        g.addEdge(1, 3, 1);
        long wrong = Dijkstra.shortestPathsWithoutCheck(g, 0).getDistance(3);
        long correct = BellmanFord.distances(g, 0)[3];
        check("counter-example: Dijkstra gives 3 but correct answer is 2", wrong == 3 && correct == 2);
    }

    private static void testGeneratorReproducible() {
        Graph a = GraphGenerator.connectedUndirected(100, 500, 42);
        Graph b = GraphGenerator.connectedUndirected(100, 500, 42);
        boolean same = a.getEdgeCount() == b.getEdgeCount();
        for (int i = 0; same && i < a.getEdgeCount(); i++) {
            Edge x = a.getEdges().get(i);
            Edge y = b.getEdges().get(i);
            same = x.getFrom() == y.getFrom() && x.getTo() == y.getTo() && x.getWeight() == y.getWeight();
        }
        check("same seed gives the same graph", same);
    }

    private static void testGeneratorConnectedAndSimple() {
        boolean ok = true;
        int[][] sizes = {{10, 30}, {100, 500}, {1000, 5000}};
        for (int[] size : sizes) {
            Graph g = GraphGenerator.connectedUndirected(size[0], size[1], 42);
            ok &= g.getVertexCount() == size[0] && g.getEdgeCount() == size[1] && GraphUtils.isConnected(g);
            Set<Long> keys = new HashSet<>();
            for (Edge e : g.getEdges()) {
                int u = Math.min(e.getFrom(), e.getTo());
                int v = Math.max(e.getFrom(), e.getTo());
                ok &= u != v && keys.add(((long) u << 32) | v);
            }
        }
        check("generated undirected graphs are connected, simple and have exact V/E", ok);
    }

    private static boolean sameDistances(Graph g, int source) {
        ShortestPathResult r = Dijkstra.shortestPaths(g, source);
        long[] expected = BellmanFord.distances(g, source);
        for (int v = 0; v < g.getVertexCount(); v++) {
            if (r.getDistance(v) != expected[v]) {
                return false;
            }
        }
        return true;
    }

    private static long pathLength(Graph g, List<Integer> path) {
        long total = 0;
        for (int i = 0; i + 1 < path.size(); i++) {
            int u = path.get(i);
            int v = path.get(i + 1);
            long best = Long.MAX_VALUE;
            for (Edge e : g.getAdjacent(u)) {
                if (g.neighbor(e, u) == v) {
                    best = Math.min(best, e.getWeight());
                }
            }
            total += best;
        }
        return total;
    }

    private static boolean[][] transitiveClosure(Graph g) {
        int n = g.getVertexCount();
        boolean[][] reach = new boolean[n][n];
        for (int i = 0; i < n; i++) {
            reach[i][i] = true;
        }
        for (Edge e : g.getEdges()) {
            reach[e.getFrom()][e.getTo()] = true;
        }
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    reach[i][j] |= reach[i][k] && reach[k][j];
                }
            }
        }
        return reach;
    }

    private static long bruteForceMst(Graph g) {
        int n = g.getVertexCount();
        List<Edge> edges = new ArrayList<>(g.getEdges());
        int m = edges.size();
        long best = Long.MAX_VALUE;
        for (int mask = 0; mask < (1 << m); mask++) {
            if (Integer.bitCount(mask) != n - 1) {
                continue;
            }
            DisjointSet dsu = new DisjointSet(n);
            long weight = 0;
            boolean acyclic = true;
            for (int i = 0; i < m && acyclic; i++) {
                if ((mask & (1 << i)) != 0) {
                    acyclic = dsu.union(edges.get(i).getFrom(), edges.get(i).getTo());
                    weight += edges.get(i).getWeight();
                }
            }
            if (acyclic) {
                best = Math.min(best, weight);
            }
        }
        return best;
    }

    private static boolean isSpanningTree(Graph g, List<Edge> tree) {
        int n = g.getVertexCount();
        if (tree.size() != n - 1) {
            return false;
        }
        DisjointSet dsu = new DisjointSet(n);
        for (Edge e : tree) {
            if (!dsu.union(e.getFrom(), e.getTo())) {
                return false;
            }
        }
        return dsu.getSetCount() == 1;
    }

    private static boolean containsWeight(MSTResult result, int weight) {
        for (Edge e : result.getEdges()) {
            if (e.getWeight() == weight) {
                return true;
            }
        }
        return false;
    }

    private static void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("[PASS] " + name);
        } else {
            failed++;
            System.out.println("[FAIL] " + name);
        }
    }
}
