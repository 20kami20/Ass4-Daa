package dagsp;

import util.Metrics;
import java.util.*;

public class DagShortestPaths {
    private final int n;
    private final List<int[]>[] adj;
    private final Metrics metrics;

    public DagShortestPaths(int n, List<int[]>[] adj, Metrics metrics) {
        this.n = n;
        this.adj = adj;
        this.metrics = metrics;
    }

    public static final long INF = Long.MAX_VALUE / 4;

    public long[] shortestFrom(int source, List<Integer> topoOrder) {
        long[] dist = new long[n];
        Arrays.fill(dist, INF);
        int[] pred = new int[n];
        Arrays.fill(pred, -1);
        dist[source] = 0;
        for (int u : topoOrder) {
            if (dist[u] == INF) continue;
            for (int[] e : adj[u]) {
                int v = e[0], w = e[1];
                metrics.dagspRelaxations.incrementAndGet();
                if (dist[v] > dist[u] + w) {
                    dist[v] = dist[u] + w;
                    pred[v] = u;
                }
            }
        }
        return dist;
    }

    public static class LongestResult {
        public final long[] dist;
        public final int[] pred;
        public LongestResult(long[] dist, int[] pred) { this.dist = dist; this.pred = pred; }
    }

    public LongestResult longestPaths(List<Integer> topoOrder) {
        final long NEG_INF = Long.MIN_VALUE / 4;
        long[] dist = new long[n];
        Arrays.fill(dist, NEG_INF);
        int[] pred = new int[n];
        Arrays.fill(pred, -1);
        int[] indeg = new int[n];
        for (int u = 0; u < n; u++) for (int[] e : adj[u]) indeg[e[0]]++;
        for (int i = 0; i < n; i++) if (indeg[i] == 0) dist[i] = 0;
        for (int u : topoOrder) {
            if (dist[u] == NEG_INF) continue;
            for (int[] e : adj[u]) {
                int v = e[0], w = e[1];
                if (dist[v] < dist[u] + w) {
                    dist[v] = dist[u] + w;
                    pred[v] = u;
                }
            }
        }
        return new LongestResult(dist, pred);
    }

    public static List<Integer> reconstructLongest(int[] pred, int target) {
        LinkedList<Integer> path = new LinkedList<>();
        int cur = target;
        while (cur != -1) {
            path.addFirst(cur);
            cur = pred[cur];
        }
        return path;
    }
}
