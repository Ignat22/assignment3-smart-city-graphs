package dsu;

public class DisjointSet {
    private final int[] parent;
    private final int[] rank;
    private final boolean pathCompression;
    private final boolean unionByRank;
    private int setCount;
    private long findCalls;
    private long unionCalls;
    private long successfulUnions;
    private long pointerSteps;

    public DisjointSet(int size) {
        this(size, true, true);
    }

    public DisjointSet(int size, boolean pathCompression, boolean unionByRank) {
        if (size < 0) {
            throw new IllegalArgumentException("Size must be non-negative");
        }
        this.parent = new int[size];
        this.rank = new int[size];
        this.pathCompression = pathCompression;
        this.unionByRank = unionByRank;
        this.setCount = size;
        for (int i = 0; i < size; i++) {
            parent[i] = i;
        }
    }

    public int find(int x) {
        checkElement(x);
        findCalls++;
        int root = x;
        while (parent[root] != root) {
            root = parent[root];
            pointerSteps++;
        }
        if (pathCompression) {
            int current = x;
            while (parent[current] != root) {
                int next = parent[current];
                parent[current] = root;
                current = next;
            }
        }
        return root;
    }

    public boolean union(int a, int b) {
        unionCalls++;
        int rootA = find(a);
        int rootB = find(b);
        if (rootA == rootB) {
            return false;
        }
        if (unionByRank) {
            if (rank[rootA] < rank[rootB]) {
                parent[rootA] = rootB;
            } else if (rank[rootA] > rank[rootB]) {
                parent[rootB] = rootA;
            } else {
                parent[rootB] = rootA;
                rank[rootA]++;
            }
        } else {
            parent[rootA] = rootB;
        }
        setCount--;
        successfulUnions++;
        return true;
    }

    public boolean connected(int a, int b) {
        return find(a) == find(b);
    }

    public int getSetCount() {
        return setCount;
    }

    public int size() {
        return parent.length;
    }

    public long getFindCalls() {
        return findCalls;
    }

    public long getUnionCalls() {
        return unionCalls;
    }

    public long getSuccessfulUnions() {
        return successfulUnions;
    }

    public long getPointerSteps() {
        return pointerSteps;
    }

    public int height() {
        int max = 0;
        for (int i = 0; i < parent.length; i++) {
            int depth = 0;
            int current = i;
            while (parent[current] != current) {
                current = parent[current];
                depth++;
            }
            max = Math.max(max, depth);
        }
        return max;
    }

    private void checkElement(int x) {
        if (x < 0 || x >= parent.length) {
            throw new IllegalArgumentException("Element " + x + " is out of range");
        }
    }
}
