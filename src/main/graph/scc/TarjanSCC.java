package scc;

import util.Metrics;
import java.util.*;

public class TarjanSCC {
    private final int n;
    private final List<int[]>[] adj;
    private final Metrics metrics;

    public TarjanSCC(int n, List<int[]>[] adj, Metrics metrics) {
        this.n = n;
        this.adj = adj;
        this.metrics = metrics;
    }

    public List<List<Integer>> run() {
        int[] index = new int[n];
        int[] lowlink = new int[n];
        boolean[] onStack = new boolean[n];
        Arrays.fill(index, -1);
        Stack<Integer> stack = new Stack<>();
        List<List<Integer>> comps = new ArrayList<>();
        int[] idx = {0};
        for (int v = 0; v < n; v++) if (index[v] == -1)
            strongconnect(v, index, lowlink, onStack, stack, comps, idx);
        return comps;
    }

    private void strongconnect(int v, int[] index, int[] lowlink, boolean[] onStack,
                               Stack<Integer> stack, List<List<Integer>> comps, int[] idx) {
        metrics.sccDfsVisits.incrementAndGet();
        index[v] = idx[0];
        lowlink[v] = idx[0];
        idx[0]++;
        stack.push(v);
        onStack[v] = true;
        for (int[] e : adj[v]) {
            int w = e[0];
            metrics.sccDfsEdges.incrementAndGet();
            if (index[w] == -1) {
                strongconnect(w, index, lowlink, onStack, stack, comps, idx);
                lowlink[v] = Math.min(lowlink[v], lowlink[w]);
            } else if (onStack[w]) lowlink[v] = Math.min(lowlink[v], index[w]);
        }
        if (lowlink[v] == index[v]) {
            List<Integer> comp = new ArrayList<>();
            while (true) {
                int w = stack.pop();
                onStack[w] = false;
                comp.add(w);
                if (w == v) break;
            }
            comps.add(comp);
        }
    }
}
