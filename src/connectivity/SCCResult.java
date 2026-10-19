package connectivity;

import java.util.Collections;
import java.util.List;

public class SCCResult {
    private final List<List<Integer>> components;
    private final int[] componentOf;
    private final long edgeExaminations;
    private final long vertexVisits;

    public SCCResult(List<List<Integer>> components, int[] componentOf, long edgeExaminations, long vertexVisits) {
        this.components = components;
        this.componentOf = componentOf;
        this.edgeExaminations = edgeExaminations;
        this.vertexVisits = vertexVisits;
    }

    public List<List<Integer>> getComponents() {
        return Collections.unmodifiableList(components);
    }

    public int getComponentCount() {
        return components.size();
    }

    public int getComponentOf(int vertex) {
        return componentOf[vertex];
    }

    public int getLargestComponentSize() {
        int max = 0;
        for (List<Integer> component : components) {
            max = Math.max(max, component.size());
        }
        return max;
    }

    public long getEdgeExaminations() {
        return edgeExaminations;
    }

    public long getVertexVisits() {
        return vertexVisits;
    }

    public long getTotalOperations() {
        return edgeExaminations + vertexVisits;
    }
}
