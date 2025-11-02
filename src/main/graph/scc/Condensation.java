package scc;

import java.util.*;

public class Condensation {
    public final int compCount;
    public final List<int[]>[] cadj;
    public final int[] compId;
    public final List<List<Integer>> comps;

    @SuppressWarnings("unchecked")
    public Condensation(int n, List<List<Integer>> comps, List<int[]>[] adj) {
        this.comps = comps;
        this.compCount = comps.size();
        compId = new int[n];
        for (int i = 0; i < comps.size(); i++)
            for (int v : comps.get(i)) compId[v] = i;
        cadj = (List<int[]>[]) new List[compCount];
        for (int i = 0; i < compCount; i++) cadj[i] = new ArrayList<>();
        Map<Long, Integer> edgeMap = new HashMap<>();
        for (int u = 0; u < n; u++) {
            for (int[] e : adj[u]) {
                int v = e[0], w = e[1];
                int cu = compId[u], cv = compId[v];
                if (cu == cv) continue;
                long key = (((long)cu) << 32) | (cv & 0xffffffffL);
                if (!edgeMap.containsKey(key) || edgeMap.get(key) > w)
                    edgeMap.put(key, w);
            }
        }
        for (Map.Entry<Long,Integer> en : edgeMap.entrySet()) {
            long k = en.getKey();
            int cu = (int)(k >>> 32);
            int cv = (int)k;
            int w = en.getValue();
            cadj[cu].add(new int[]{cv, w});
        }
    }
}
