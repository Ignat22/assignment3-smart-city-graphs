package mst;

import graph.Edge;

import java.util.Collections;
import java.util.List;

public class MSTResult {
    private final String algorithm;
    private final List<Edge> edges;
    private final long totalWeight;
    private final int components;
    private final long comparisons;
    private final long edgeExaminations;
    private final long dsuFinds;
    private final long dsuUnions;
    private final long queueOperations;

    public MSTResult(String algorithm, List<Edge> edges, long totalWeight, int components,
                     long comparisons, long edgeExaminations, long dsuFinds, long dsuUnions,
                     long queueOperations) {
        this.algorithm = algorithm;
        this.edges = edges;
        this.totalWeight = totalWeight;
        this.components = components;
        this.comparisons = comparisons;
        this.edgeExaminations = edgeExaminations;
        this.dsuFinds = dsuFinds;
        this.dsuUnions = dsuUnions;
        this.queueOperations = queueOperations;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public List<Edge> getEdges() {
        return Collections.unmodifiableList(edges);
    }

    public long getTotalWeight() {
        return totalWeight;
    }

    public int getComponents() {
        return components;
    }

    public boolean isConnected() {
        return components <= 1;
    }

    public long getComparisons() {
        return comparisons;
    }

    public long getEdgeExaminations() {
        return edgeExaminations;
    }

    public long getDsuFinds() {
        return dsuFinds;
    }

    public long getDsuUnions() {
        return dsuUnions;
    }

    public long getQueueOperations() {
        return queueOperations;
    }

    public long getTotalOperations() {
        return comparisons + edgeExaminations + dsuFinds + dsuUnions + queueOperations;
    }

    @Override
    public String toString() {
        String kind = isConnected() ? "MST" : "minimum spanning forest (" + components + " components)";
        return algorithm + ": " + kind + ", weight=" + totalWeight + ", edges=" + edges.size();
    }
}
