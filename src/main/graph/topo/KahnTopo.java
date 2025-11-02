package topo;

import util.Metrics;
import java.util.*;

public class KahnTopo {
    private final int n;
    private final List<int[]>[] adj;
    private final Metrics metrics;

    public KahnTopo(int n, List<int[]>[] adj, Metrics metrics) {
        this.n = n;
        this.adj = adj;
        this.metrics = metrics;
    }

    public List<Integer> topoOrder() {
        int[] indeg = new int[n];
        for (int u = 0; u < n; u++) for (int[] e : adj[u]) indeg[e[0]]++;
        Deque<Integer> q = new ArrayDeque<>();
        for (int i = 0; i < n; i++) if (indeg[i] == 0) q.add(i);
        List<Integer> order = new ArrayList<>();
        while (!q.isEmpty()) {
            metrics.kahnPops.incrementAndGet();
            int u = q.removeFirst();
            order.add(u);
            for (int[] e : adj[u]) {
                metrics.kahnPushes.incrementAndGet();
                int v = e[0];
                indeg[v]--;
                if (indeg[v] == 0) q.add(v);
            }
        }
        return order;
    }
}
