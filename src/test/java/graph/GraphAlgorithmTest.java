package graph;

import graph.scc.TarjanSCC;
import graph.scc.Condensation;
import graph.topo.KahnTopo;
import graph.dagsp.DagShortestPaths;
import util.Metrics;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class GraphAlgorithmTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testSingleSCC() {
        int n = 3;
        List<int[]>[] adj = (List<int[]>[]) new List[n];
        for (int i = 0; i < n; i++) adj[i] = new ArrayList<>();
        adj[0].add(new int[]{1,1});
        adj[1].add(new int[]{2,1});
        adj[2].add(new int[]{0,1});
        Metrics m = new Metrics();
        TarjanSCC t = new TarjanSCC(n, adj, m);
        List<List<Integer>> sccs = t.run();
        assertEquals(1, sccs.size());
        assertEquals(3, sccs.get(0).size());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testTopoAndPaths() {
        int n = 4;
        List<int[]>[] adj = (List<int[]>[]) new List[n];
        for (int i = 0; i < n; i++) adj[i] = new ArrayList<>();
        adj[0].add(new int[]{1,1});
        adj[0].add(new int[]{2,2});
        adj[1].add(new int[]{3,3});
        adj[2].add(new int[]{3,1});
        Metrics m = new Metrics();
        TarjanSCC t = new TarjanSCC(n, adj, m);
        List<List<Integer>> sccs = t.run();
        Condensation c = new Condensation(n, sccs, adj);
        KahnTopo topo = new KahnTopo(c.compCount, c.cadj, m);
        List<Integer> order = topo.topoOrder();
        DagShortestPaths dsp = new DagShortestPaths(c.compCount, c.cadj, m);
        int sourceComp = c.compId[0];
        long[] dist = dsp.shortestFrom(sourceComp, order);
        assertEquals(3, dist[c.compId[3]]);
        DagShortestPaths.LongestResult lr = dsp.longestPaths(order);
        assertEquals(4, lr.dist[c.compId[3]]);
    }
}
