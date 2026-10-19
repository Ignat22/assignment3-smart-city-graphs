package shortestpath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShortestPathResult {
    private final int source;
    private final long[] distances;
    private final int[] previous;
    private final long comparisons;
    private final long edgeExaminations;
    private final long relaxations;
    private final long queueOperations;
    private final long staleEntries;

    public ShortestPathResult(int source, long[] distances, int[] previous, long comparisons,
                              long edgeExaminations, long relaxations, long queueOperations, long staleEntries) {
        this.source = source;
        this.distances = distances;
        this.previous = previous;
        this.comparisons = comparisons;
        this.edgeExaminations = edgeExaminations;
        this.relaxations = relaxations;
        this.queueOperations = queueOperations;
        this.staleEntries = staleEntries;
    }

    public int getSource() {
        return source;
    }

    public long getDistance(int vertex) {
        return distances[vertex];
    }

    public boolean isReachable(int vertex) {
        return distances[vertex] != Dijkstra.INFINITY;
    }

    public int getReachableCount() {
        int count = 0;
        for (long d : distances) {
            if (d != Dijkstra.INFINITY) {
                count++;
            }
        }
        return count;
    }

    public List<Integer> getPath(int target) {
        if (!isReachable(target)) {
            return Collections.emptyList();
        }
        List<Integer> path = new ArrayList<>();
        for (int v = target; v != -1; v = previous[v]) {
            path.add(v);
        }
        Collections.reverse(path);
        return path;
    }

    public long getComparisons() {
        return comparisons;
    }

    public long getEdgeExaminations() {
        return edgeExaminations;
    }

    public long getRelaxations() {
        return relaxations;
    }

    public long getQueueOperations() {
        return queueOperations;
    }

    public long getStaleEntries() {
        return staleEntries;
    }

    public long getTotalOperations() {
        return comparisons + edgeExaminations + queueOperations;
    }
}
