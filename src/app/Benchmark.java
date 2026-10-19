package app;

import connectivity.KosarajuSCC;
import connectivity.SCCResult;
import connectivity.TopoResult;
import connectivity.TopologicalSort;
import dsu.DisjointSet;
import graph.Graph;
import graph.GraphGenerator;
import graph.GraphUtils;
import mst.Kruskal;
import mst.MSTResult;
import mst.Prim;
import shortestpath.Dijkstra;
import shortestpath.ShortestPathResult;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;

public class Benchmark {
    private static final long SEED = 42;
    private static final int REPEATS = 5;
    private static final int WARMUP_RUNS = 10;
    private static final long MIN_BATCH_NANOS = 2_000_000L;
    private static final int MAX_BATCH = 2000;
    private static final String[] SIZE_NAMES = {"Small", "Medium", "Large"};
    private static final int[][] SIZES = {{10, 30}, {100, 500}, {1000, 5000}};
    private static final int[] SCALING_VERTICES = {1000, 2000, 5000, 10000, 20000, 50000};
    private static final int DENSITY_VERTICES = 1000;
    private static final int[] DENSITY_EDGES = {2000, 5000, 10000, 20000, 50000, 100000, 200000};
    private static final int[] DSU_SIZES = {1000, 5000, 20000};
    private static final String[] ALGORITHMS = {"Kruskal", "Prim", "Topo (DAG)", "Topo (back edge)", "Kosaraju SCC", "Dijkstra"};

    private static final File TABLES = new File("results/tables");
    private static final File PLOTS = new File("results/plots");

    public static void main(String[] args) throws IOException {
        Locale.setDefault(Locale.US);
        TABLES.mkdirs();
        PLOTS.mkdirs();
        writeEnvironment();
        System.out.println("Warming up the JIT...");
        warmUp();
        double[][] requiredTimes = requiredExperiments();
        double[][] scalingTimes = scalingExperiment();
        densityExperiment();
        dsuExperiment();
        requiredPlots(requiredTimes);
        scalingPlot(scalingTimes);
        System.out.println("Done. Tables in results/tables, plots in results/plots.");
    }

    private static void warmUp() {
        Graph u = GraphGenerator.connectedUndirected(1000, 5000, 1);
        Graph d = GraphGenerator.randomDirected(1000, 5000, 1);
        Graph dag = GraphGenerator.randomDag(1000, 5000, 1);
        for (int i = 0; i < 200; i++) {
            Kruskal.findMST(u);
            Prim.findMST(u);
            TopologicalSort.sort(dag);
            TopologicalSort.sort(d);
            KosarajuSCC.findComponents(d);
            Dijkstra.shortestPaths(u, 0);
        }
    }

    private static double[][] requiredExperiments() throws IOException {
        System.out.println();
        System.out.println("=== Required experiments (seed " + SEED + ", " + REPEATS + " repeats) ===");
        double[][] avg = new double[ALGORITHMS.length][SIZES.length];
        List<String> timeRows = new ArrayList<>();
        List<String> mstRows = new ArrayList<>();
        List<String> topoRows = new ArrayList<>();
        List<String> sccRows = new ArrayList<>();
        List<String> dijkstraRows = new ArrayList<>();
        List<String> propertyRows = new ArrayList<>();
        StringBuilder timeMd = mdHeader("Graph", "V", "E", "Algorithm", "Calls per run", "Run 1", "Run 2", "Run 3",
                "Run 4", "Run 5", "Avg (ms)", "Std dev (ms)");
        StringBuilder mstMd = mdHeader("Graph", "V", "E", "Algorithm", "Avg time (ms)", "Comparisons",
                "Edges examined", "DSU find", "DSU union", "PQ ops", "Total ops", "MST weight");
        StringBuilder topoMd = mdHeader("Graph", "V", "E", "Input", "Avg time (ms)", "Edge examinations",
                "Queue ops", "Total ops", "Vertices ordered", "Result");
        StringBuilder sccMd = mdHeader("Graph", "V", "E", "Input", "Avg time (ms)", "Edge examinations",
                "Vertex visits", "Total ops", "SCC count", "Largest SCC");
        StringBuilder dijkstraMd = mdHeader("Graph", "V", "E", "Avg time (ms)", "PQ comparisons",
                "Edge examinations", "Successful relaxations", "PQ ops", "Stale entries", "Total ops", "Reachable");
        StringBuilder propertyMd = mdHeader("Graph", "V", "E", "Undirected connected", "Directed: SCCs",
                "Directed: largest SCC", "Directed has cycle", "DAG check");

        for (int s = 0; s < SIZES.length; s++) {
            String name = SIZE_NAMES[s];
            int v = SIZES[s][0];
            int e = SIZES[s][1];
            Graph undirected = GraphGenerator.connectedUndirected(v, e, SEED);
            Graph directed = GraphGenerator.randomDirected(v, e, SEED);
            Graph dag = GraphGenerator.randomDag(v, e, SEED);
            Graph backEdge = GraphGenerator.dagWithBackEdge(v, e, SEED);

            Measurement<MSTResult> kruskal = measure(() -> Kruskal.findMST(undirected));
            Measurement<MSTResult> prim = measure(() -> Prim.findMST(undirected));
            Measurement<TopoResult> topoDag = measure(() -> TopologicalSort.sort(dag));
            Measurement<TopoResult> topoCyclic = measure(() -> TopologicalSort.sort(directed));
            Measurement<TopoResult> topoBack = measure(() -> TopologicalSort.sort(backEdge));
            Measurement<SCCResult> scc = measure(() -> KosarajuSCC.findComponents(directed));
            Measurement<SCCResult> sccDag = measure(() -> KosarajuSCC.findComponents(dag));
            Measurement<ShortestPathResult> dijkstra = measure(() -> Dijkstra.shortestPaths(undirected, 0));

            Measurement<?>[] all = {kruskal, prim, topoDag, topoBack, scc, dijkstra};
            for (int a = 0; a < all.length; a++) {
                avg[a][s] = all[a].average();
                timeRows.add(csv(name, v, e, ALGORITHMS[a], all[a].batch, all[a].times[0], all[a].times[1], all[a].times[2],
                        all[a].times[3], all[a].times[4], all[a].average(), all[a].stdDev()));
                timeMd.append(mdRow(name, v, e, ALGORITHMS[a], all[a].batch, ms(all[a].times[0]), ms(all[a].times[1]),
                        ms(all[a].times[2]), ms(all[a].times[3]), ms(all[a].times[4]), ms(all[a].average()),
                        ms(all[a].stdDev())));
            }

            for (Measurement<MSTResult> m : List.of(kruskal, prim)) {
                MSTResult r = m.result;
                mstRows.add(csv(name, v, e, r.getAlgorithm(), m.average(), r.getComparisons(), r.getEdgeExaminations(),
                        r.getDsuFinds(), r.getDsuUnions(), r.getQueueOperations(), r.getTotalOperations(),
                        r.getTotalWeight()));
                mstMd.append(mdRow(name, v, e, r.getAlgorithm(), ms(m.average()), r.getComparisons(),
                        r.getEdgeExaminations(), r.getDsuFinds(), r.getDsuUnions(), r.getQueueOperations(),
                        r.getTotalOperations(), r.getTotalWeight()));
            }

            addTopoRow(topoRows, topoMd, name, v, e, "random DAG", topoDag);
            addTopoRow(topoRows, topoMd, name, v, e, "DAG + 1 back edge", topoBack);
            addTopoRow(topoRows, topoMd, name, v, e, "random directed (cyclic)", topoCyclic);

            addSccRow(sccRows, sccMd, name, v, e, "random directed", scc);
            addSccRow(sccRows, sccMd, name, v, e, "random DAG", sccDag);

            ShortestPathResult d = dijkstra.result;
            dijkstraRows.add(csv(name, v, e, dijkstra.average(), d.getComparisons(), d.getEdgeExaminations(),
                    d.getRelaxations(), d.getQueueOperations(), d.getStaleEntries(), d.getTotalOperations(),
                    d.getReachableCount()));
            dijkstraMd.append(mdRow(name, v, e, ms(dijkstra.average()), d.getComparisons(), d.getEdgeExaminations(),
                    d.getRelaxations(), d.getQueueOperations(), d.getStaleEntries(), d.getTotalOperations(),
                    d.getReachableCount() + "/" + v));

            boolean hasCycle = scc.result.getComponentCount() < v;
            propertyRows.add(csv(name, v, e, GraphUtils.isConnected(undirected), scc.result.getComponentCount(),
                    scc.result.getLargestComponentSize(), hasCycle, topoDag.result.isDag()));
            propertyMd.append(mdRow(name, v, e, GraphUtils.isConnected(undirected) ? "yes" : "no",
                    scc.result.getComponentCount(), scc.result.getLargestComponentSize(), hasCycle ? "yes" : "no",
                    topoDag.result.isDag() ? "acyclic" : "cyclic"));

            System.out.printf("%-6s V=%-5d E=%-5d Kruskal=%s Prim=%s TopoDAG=%s TopoBack=%s SCC=%s Dijkstra=%s ms%n",
                    name, v, e, ms(kruskal.average()), ms(prim.average()), ms(topoDag.average()),
                    ms(topoBack.average()), ms(scc.average()), ms(dijkstra.average()));
        }

        writeCsv("required_times.csv", "graph,V,E,algorithm,calls_per_run,run1_ms,run2_ms,run3_ms,run4_ms,run5_ms,avg_ms,std_ms", timeRows);
        writeCsv("mst.csv", "graph,V,E,algorithm,avg_ms,comparisons,edge_examinations,dsu_find,dsu_union,pq_ops,total_ops,mst_weight", mstRows);
        writeCsv("topological_sort.csv", "graph,V,E,input,avg_ms,edge_examinations,queue_ops,total_ops,ordered,is_dag", topoRows);
        writeCsv("scc.csv", "graph,V,E,input,avg_ms,edge_examinations,vertex_visits,total_ops,scc_count,largest_scc", sccRows);
        writeCsv("dijkstra.csv", "graph,V,E,avg_ms,pq_comparisons,edge_examinations,relaxations,pq_ops,stale_entries,total_ops,reachable", dijkstraRows);
        writeCsv("graph_properties.csv", "graph,V,E,undirected_connected,directed_scc_count,directed_largest_scc,directed_has_cycle,dag_is_acyclic", propertyRows);
        writeText("required_times.md", timeMd.toString());
        writeText("mst.md", mstMd.toString());
        writeText("topological_sort.md", topoMd.toString());
        writeText("scc.md", sccMd.toString());
        writeText("dijkstra.md", dijkstraMd.toString());
        writeText("graph_properties.md", propertyMd.toString());
        return avg;
    }

    private static void addTopoRow(List<String> rows, StringBuilder md, String name, int v, int e, String input,
                                   Measurement<TopoResult> m) {
        TopoResult r = m.result;
        int ordered = r.getPartialOrder().size();
        rows.add(csv(name, v, e, input, m.average(), r.getEdgeExaminations(), r.getQueueOperations(),
                r.getTotalOperations(), ordered, r.isDag()));
        md.append(mdRow(name, v, e, input, ms(m.average()), r.getEdgeExaminations(), r.getQueueOperations(),
                r.getTotalOperations(), ordered + "/" + v, r.isDag() ? "valid order" : "cycle detected"));
    }

    private static void addSccRow(List<String> rows, StringBuilder md, String name, int v, int e, String input,
                                  Measurement<SCCResult> m) {
        SCCResult r = m.result;
        rows.add(csv(name, v, e, input, m.average(), r.getEdgeExaminations(), r.getVertexVisits(),
                r.getTotalOperations(), r.getComponentCount(), r.getLargestComponentSize()));
        md.append(mdRow(name, v, e, input, ms(m.average()), r.getEdgeExaminations(), r.getVertexVisits(),
                r.getTotalOperations(), r.getComponentCount(), r.getLargestComponentSize()));
    }

    private static double[][] scalingExperiment() throws IOException {
        System.out.println();
        System.out.println("=== Scaling experiment (E = 5V) ===");
        double[][] avg = new double[ALGORITHMS.length][SCALING_VERTICES.length];
        long[][] ops = new long[ALGORITHMS.length][SCALING_VERTICES.length];
        List<String> rows = new ArrayList<>();
        StringBuilder md = mdHeader("V", "E", "Kruskal (ms)", "Prim (ms)", "Topo DAG (ms)", "Topo back edge (ms)",
                "SCC (ms)", "Dijkstra (ms)");
        StringBuilder opsMd = mdHeader("V", "E", "Kruskal ops", "Prim ops", "Topo DAG ops", "Topo back edge ops",
                "SCC ops", "Dijkstra ops");
        for (int s = 0; s < SCALING_VERTICES.length; s++) {
            int v = SCALING_VERTICES[s];
            int e = 5 * v;
            Graph undirected = GraphGenerator.connectedUndirected(v, e, SEED);
            Graph directed = GraphGenerator.randomDirected(v, e, SEED);
            Graph dag = GraphGenerator.randomDag(v, e, SEED);
            Graph backEdge = GraphGenerator.dagWithBackEdge(v, e, SEED);
            Measurement<MSTResult> kruskal = measure(() -> Kruskal.findMST(undirected));
            Measurement<MSTResult> prim = measure(() -> Prim.findMST(undirected));
            Measurement<TopoResult> topoDag = measure(() -> TopologicalSort.sort(dag));
            Measurement<TopoResult> topoCyclic = measure(() -> TopologicalSort.sort(backEdge));
            Measurement<SCCResult> scc = measure(() -> KosarajuSCC.findComponents(directed));
            Measurement<ShortestPathResult> dijkstra = measure(() -> Dijkstra.shortestPaths(undirected, 0));
            Measurement<?>[] all = {kruskal, prim, topoDag, topoCyclic, scc, dijkstra};
            long[] counts = {kruskal.result.getTotalOperations(), prim.result.getTotalOperations(),
                    topoDag.result.getTotalOperations(), topoCyclic.result.getTotalOperations(),
                    scc.result.getTotalOperations(), dijkstra.result.getTotalOperations()};
            Object[] mdTimes = new Object[ALGORITHMS.length + 2];
            Object[] mdOps = new Object[ALGORITHMS.length + 2];
            mdTimes[0] = v;
            mdTimes[1] = e;
            mdOps[0] = v;
            mdOps[1] = e;
            for (int a = 0; a < all.length; a++) {
                avg[a][s] = all[a].average();
                ops[a][s] = counts[a];
                rows.add(csv(v, e, ALGORITHMS[a], all[a].average(), all[a].stdDev(), counts[a]));
                mdTimes[a + 2] = ms(all[a].average());
                mdOps[a + 2] = counts[a];
            }
            md.append(mdRow(mdTimes));
            opsMd.append(mdRow(mdOps));
            System.out.printf("V=%-6d E=%-6d Kruskal=%s Prim=%s SCC=%s Dijkstra=%s ms%n", v, e,
                    ms(kruskal.average()), ms(prim.average()), ms(scc.average()), ms(dijkstra.average()));
        }
        writeCsv("scaling.csv", "V,E,algorithm,avg_ms,std_ms,total_ops", rows);
        writeText("scaling_times.md", md.toString());
        writeText("scaling_operations.md", opsMd.toString());

        double[] edges = new double[SCALING_VERTICES.length];
        for (int s = 0; s < edges.length; s++) {
            edges[s] = 5.0 * SCALING_VERTICES[s];
        }
        LineChart opsChart = new LineChart("Operation count vs number of edges (E = 5V)",
                "Counted primitive operations, seed 42", "Edges (E)", "Total counted operations", false, false);
        for (int a = 0; a < ALGORITHMS.length; a++) {
            double[] y = new double[ops[a].length];
            for (int i = 0; i < y.length; i++) {
                y[i] = ops[a][i];
            }
            opsChart.addSeries(ALGORITHMS[a], edges, y);
        }
        opsChart.save(new File(PLOTS, "operations_vs_edges.png"));
        return avg;
    }

    private static void densityExperiment() throws IOException {
        System.out.println();
        System.out.println("=== Kruskal vs Prim, V = " + DENSITY_VERTICES + ", growing E ===");
        List<String> rows = new ArrayList<>();
        StringBuilder md = mdHeader("V", "E", "E/V", "Kruskal (ms)", "Prim (ms)", "Kruskal ops", "Prim ops",
                "Kruskal edges examined", "Same weight");
        double[] x = new double[DENSITY_EDGES.length];
        double[] kt = new double[DENSITY_EDGES.length];
        double[] pt = new double[DENSITY_EDGES.length];
        for (int i = 0; i < DENSITY_EDGES.length; i++) {
            int e = DENSITY_EDGES[i];
            Graph g = GraphGenerator.connectedUndirected(DENSITY_VERTICES, e, SEED);
            Measurement<MSTResult> k = measure(() -> Kruskal.findMST(g));
            Measurement<MSTResult> p = measure(() -> Prim.findMST(g));
            boolean same = k.result.getTotalWeight() == p.result.getTotalWeight();
            x[i] = e;
            kt[i] = k.average();
            pt[i] = p.average();
            rows.add(csv(DENSITY_VERTICES, e, k.average(), p.average(), k.result.getTotalOperations(),
                    p.result.getTotalOperations(), k.result.getEdgeExaminations(), same));
            md.append(mdRow(DENSITY_VERTICES, e, e / DENSITY_VERTICES, ms(k.average()), ms(p.average()),
                    k.result.getTotalOperations(), p.result.getTotalOperations(), k.result.getEdgeExaminations(),
                    same ? "yes" : "no"));
            System.out.printf("E=%-7d Kruskal=%s Prim=%s ms%n", e, ms(k.average()), ms(p.average()));
        }
        writeCsv("kruskal_vs_prim_density.csv", "V,E,kruskal_ms,prim_ms,kruskal_ops,prim_ops,kruskal_edges_examined,same_weight", rows);
        writeText("kruskal_vs_prim_density.md", md.toString());
        new LineChart("Kruskal vs Prim as the graph gets denser",
                "V = 1000 districts, average of 5 runs, seed 42", "Edges (E)", "Average time (ms)", false, false)
                .addSeries("Kruskal", x, kt)
                .addSeries("Prim", x, pt)
                .save(new File(PLOTS, "kruskal_vs_prim_density.png"));
    }

    private static void dsuExperiment() throws IOException {
        System.out.println();
        System.out.println("=== DSU optimisations ===");
        String[] variants = {"No optimisation", "Union by rank", "Path compression", "Rank + compression"};
        boolean[][] flags = {{false, false}, {false, true}, {true, false}, {true, true}};
        List<String> rows = new ArrayList<>();
        StringBuilder md = mdHeader("Workload", "n", "Variant", "Avg time (ms)", "find calls", "Pointer steps",
                "Steps per find", "Max tree height");
        double[] x = new double[DSU_SIZES.length];
        double[][] steps = new double[variants.length][DSU_SIZES.length];
        for (String workload : new String[]{"chain", "random"}) {
            for (int i = 0; i < DSU_SIZES.length; i++) {
                int n = DSU_SIZES[i];
                x[i] = n;
                for (int k = 0; k < variants.length; k++) {
                    boolean compression = flags[k][0];
                    boolean rank = flags[k][1];
                    Measurement<DisjointSet> m = measure(() -> dsuWorkload(workload, n, compression, rank));
                    DisjointSet dsu = m.result;
                    double perFind = (double) dsu.getPointerSteps() / dsu.getFindCalls();
                    if (workload.equals("random")) {
                        steps[k][i] = dsu.getPointerSteps();
                    }
                    rows.add(csv(workload, n, variants[k], m.average(), dsu.getFindCalls(), dsu.getPointerSteps(),
                            perFind, dsu.height()));
                    md.append(mdRow(workload, n, variants[k], ms(m.average()), dsu.getFindCalls(),
                            dsu.getPointerSteps(), String.format("%.2f", perFind), dsu.height()));
                }
                System.out.printf("%-6s n=%-6d done%n", workload, n);
            }
        }
        writeCsv("dsu.csv", "workload,n,variant,avg_ms,find_calls,pointer_steps,steps_per_find,max_height", rows);
        writeText("dsu.md", md.toString());
        LineChart chart = new LineChart("DSU pointer steps: n random unions + n random finds",
                "Lower is better, log-log scale, seed 42", "Number of elements (n)", "Total parent-pointer steps",
                true, true);
        for (int k = 0; k < variants.length; k++) {
            double[] y = new double[DSU_SIZES.length];
            for (int i = 0; i < y.length; i++) {
                y[i] = Math.max(1, steps[k][i]);
            }
            chart.addSeries(variants[k], x, y);
        }
        chart.save(new File(PLOTS, "dsu_pointer_steps.png"));
    }

    private static DisjointSet dsuWorkload(String workload, int n, boolean compression, boolean rank) {
        DisjointSet dsu = new DisjointSet(n, compression, rank);
        Random random = new Random(SEED);
        if (workload.equals("chain")) {
            for (int i = 0; i < n - 1; i++) {
                dsu.union(i, i + 1);
            }
        } else {
            for (int i = 0; i < n; i++) {
                dsu.union(random.nextInt(n), random.nextInt(n));
            }
        }
        for (int i = 0; i < n; i++) {
            dsu.find(random.nextInt(n));
        }
        return dsu;
    }

    private static void requiredPlots(double[][] avg) throws IOException {
        double[] v = new double[SIZES.length];
        double[] e = new double[SIZES.length];
        for (int s = 0; s < SIZES.length; s++) {
            v[s] = SIZES[s][0];
            e[s] = SIZES[s][1];
        }
        LineChart byVertices = new LineChart("Execution time vs number of vertices",
                "Small (10/30), Medium (100/500), Large (1000/5000); average of 5 runs; log-log scale",
                "Vertices (V)", "Average time (ms)", true, true);
        LineChart byEdges = new LineChart("Execution time vs number of edges",
                "Small (10/30), Medium (100/500), Large (1000/5000); average of 5 runs; log-log scale",
                "Edges (E)", "Average time (ms)", true, true);
        for (int a = 0; a < ALGORITHMS.length; a++) {
            byVertices.addSeries(ALGORITHMS[a], v, avg[a]);
            byEdges.addSeries(ALGORITHMS[a], e, avg[a]);
        }
        byVertices.save(new File(PLOTS, "time_vs_vertices.png"));
        byEdges.save(new File(PLOTS, "time_vs_edges.png"));
    }

    private static void scalingPlot(double[][] avg) throws IOException {
        double[] e = new double[SCALING_VERTICES.length];
        double[] v = new double[SCALING_VERTICES.length];
        for (int s = 0; s < e.length; s++) {
            v[s] = SCALING_VERTICES[s];
            e[s] = 5.0 * SCALING_VERTICES[s];
        }
        LineChart byEdges = new LineChart("Scaling: execution time vs number of edges (E = 5V)",
                "V from 1,000 to 50,000; average of 5 runs; linear scale", "Edges (E)", "Average time (ms)",
                false, false);
        LineChart byVertices = new LineChart("Scaling: execution time vs number of vertices (E = 5V)",
                "V from 1,000 to 50,000; average of 5 runs; linear scale", "Vertices (V)", "Average time (ms)",
                false, false);
        for (int a = 0; a < ALGORITHMS.length; a++) {
            byEdges.addSeries(ALGORITHMS[a], e, avg[a]);
            byVertices.addSeries(ALGORITHMS[a], v, avg[a]);
        }
        byEdges.save(new File(PLOTS, "scaling_time_vs_edges.png"));
        byVertices.save(new File(PLOTS, "scaling_time_vs_vertices.png"));
    }

    private static <T> Measurement<T> measure(Supplier<T> task) {
        System.gc();
        T result = null;
        for (int i = 0; i < WARMUP_RUNS; i++) {
            result = task.get();
        }
        long probeStart = System.nanoTime();
        task.get();
        long probe = Math.max(1, System.nanoTime() - probeStart);
        int batch = (int) Math.min(MAX_BATCH, Math.max(1, MIN_BATCH_NANOS / probe));
        double[] times = new double[REPEATS];
        for (int i = 0; i < REPEATS; i++) {
            long start = System.nanoTime();
            for (int k = 0; k < batch; k++) {
                result = task.get();
            }
            long end = System.nanoTime();
            times[i] = (end - start) / 1_000_000.0 / batch;
        }
        return new Measurement<>(times, result, batch);
    }

    private static class Measurement<T> {
        private final double[] times;
        private final T result;
        private final int batch;

        Measurement(double[] times, T result, int batch) {
            this.times = times;
            this.result = result;
            this.batch = batch;
        }

        double average() {
            double sum = 0;
            for (double t : times) {
                sum += t;
            }
            return sum / times.length;
        }

        double stdDev() {
            double mean = average();
            double sum = 0;
            for (double t : times) {
                sum += (t - mean) * (t - mean);
            }
            return Math.sqrt(sum / times.length);
        }
    }

    private static void writeEnvironment() throws IOException {
        Map<String, String> info = new LinkedHashMap<>();
        info.put("java.version", System.getProperty("java.version"));
        info.put("java.vm.name", System.getProperty("java.vm.name"));
        info.put("os.name", System.getProperty("os.name"));
        info.put("os.arch", System.getProperty("os.arch"));
        info.put("available.processors", String.valueOf(Runtime.getRuntime().availableProcessors()));
        info.put("max.heap.mb", String.valueOf(Runtime.getRuntime().maxMemory() / (1024 * 1024)));
        info.put("seed", String.valueOf(SEED));
        info.put("repeats", String.valueOf(REPEATS));
        info.put("warmup.runs.per.measurement", String.valueOf(WARMUP_RUNS));
        info.put("min.batch.duration.ns", String.valueOf(MIN_BATCH_NANOS));
        info.put("max.calls.per.run", String.valueOf(MAX_BATCH));
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : info.entrySet()) {
            sb.append(entry.getKey()).append('=').append(entry.getValue()).append('\n');
        }
        writeText("environment.txt", sb.toString());
    }

    private static String ms(double value) {
        if (value >= 100) {
            return String.format("%.1f", value);
        }
        if (value >= 1) {
            return String.format("%.3f", value);
        }
        return String.format("%.4f", value);
    }

    private static String csv(Object... values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            Object value = values[i];
            if (value instanceof Double) {
                sb.append(String.format("%.6f", (Double) value));
            } else {
                sb.append(value);
            }
        }
        return sb.toString();
    }

    private static StringBuilder mdHeader(String... columns) {
        StringBuilder sb = new StringBuilder("|");
        StringBuilder line = new StringBuilder("|");
        for (String column : columns) {
            sb.append(' ').append(column).append(" |");
            line.append("---|");
        }
        return sb.append('\n').append(line).append('\n');
    }

    private static String mdRow(Object... values) {
        StringBuilder sb = new StringBuilder("|");
        for (Object value : values) {
            sb.append(' ').append(value).append(" |");
        }
        return sb.append('\n').toString();
    }

    private static void writeCsv(String name, String header, List<String> rows) throws IOException {
        StringBuilder sb = new StringBuilder(header).append('\n');
        for (String row : rows) {
            sb.append(row).append('\n');
        }
        writeText(name, sb.toString());
    }

    private static void writeText(String name, String content) throws IOException {
        try (PrintWriter out = new PrintWriter(new File(TABLES, name), StandardCharsets.UTF_8)) {
            out.print(content);
        }
    }
}
