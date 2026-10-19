package graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Graph {
    private final int vertexCount;
    private final boolean directed;
    private final List<List<Edge>> adjacency;
    private final List<Edge> edges;
    private final String[] names;

    public Graph(int vertexCount, boolean directed) {
        if (vertexCount < 0) {
            throw new IllegalArgumentException("Vertex count must be non-negative");
        }
        this.vertexCount = vertexCount;
        this.directed = directed;
        this.adjacency = new ArrayList<>(vertexCount);
        this.edges = new ArrayList<>();
        this.names = new String[vertexCount];
        for (int i = 0; i < vertexCount; i++) {
            adjacency.add(new ArrayList<>());
            names[i] = "V" + i;
        }
    }

    public Graph(String[] districtNames, boolean directed) {
        this(districtNames.length, directed);
        for (int i = 0; i < districtNames.length; i++) {
            names[i] = districtNames[i];
        }
    }

    public void addEdge(int from, int to, int weight) {
        checkVertex(from);
        checkVertex(to);
        Edge edge = new Edge(from, to, weight);
        edges.add(edge);
        adjacency.get(from).add(edge);
        if (!directed && from != to) {
            adjacency.get(to).add(edge);
        }
    }

    public void addEdge(int from, int to) {
        addEdge(from, to, 1);
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public int getEdgeCount() {
        return edges.size();
    }

    public boolean isDirected() {
        return directed;
    }

    public List<Edge> getAdjacent(int vertex) {
        checkVertex(vertex);
        return Collections.unmodifiableList(adjacency.get(vertex));
    }

    public List<Edge> getEdges() {
        return Collections.unmodifiableList(edges);
    }

    public String getName(int vertex) {
        checkVertex(vertex);
        return names[vertex];
    }

    public int neighbor(Edge edge, int vertex) {
        if (directed) {
            return edge.getTo();
        }
        return edge.other(vertex);
    }

    public Graph reversed() {
        Graph result = new Graph(names, directed);
        for (Edge edge : edges) {
            result.addEdge(edge.getTo(), edge.getFrom(), edge.getWeight());
        }
        return result;
    }

    public boolean hasNegativeWeight() {
        for (Edge edge : edges) {
            if (edge.getWeight() < 0) {
                return true;
            }
        }
        return false;
    }

    private void checkVertex(int vertex) {
        if (vertex < 0 || vertex >= vertexCount) {
            throw new IllegalArgumentException("Vertex " + vertex + " is out of range [0, " + vertexCount + ")");
        }
    }

    @Override
    public String toString() {
        return (directed ? "Directed" : "Undirected") + " graph: V=" + vertexCount + ", E=" + edges.size();
    }
}
