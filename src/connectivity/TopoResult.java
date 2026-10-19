package connectivity;

import java.util.Collections;
import java.util.List;

public class TopoResult {
    private final boolean dag;
    private final List<Integer> order;
    private final List<Integer> verticesOnOrAfterCycle;
    private final long edgeExaminations;
    private final long queueOperations;

    public TopoResult(boolean dag, List<Integer> order, List<Integer> verticesOnOrAfterCycle,
                      long edgeExaminations, long queueOperations) {
        this.dag = dag;
        this.order = order;
        this.verticesOnOrAfterCycle = verticesOnOrAfterCycle;
        this.edgeExaminations = edgeExaminations;
        this.queueOperations = queueOperations;
    }

    public boolean isDag() {
        return dag;
    }

    public List<Integer> getOrder() {
        if (!dag) {
            return null;
        }
        return Collections.unmodifiableList(order);
    }

    public List<Integer> getPartialOrder() {
        return Collections.unmodifiableList(order);
    }

    public List<Integer> getVerticesOnOrAfterCycle() {
        return Collections.unmodifiableList(verticesOnOrAfterCycle);
    }

    public long getEdgeExaminations() {
        return edgeExaminations;
    }

    public long getQueueOperations() {
        return queueOperations;
    }

    public long getTotalOperations() {
        return edgeExaminations + queueOperations;
    }
}
