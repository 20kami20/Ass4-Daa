package app;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import scc.TarjanSCC;
import scc.Condensation;
import topo.KahnTopo;
import dagsp.DagShortestPaths;
import util.Metrics;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class App {
    public static class EdgeDef { public int u, v; public long w = 1; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Input {
        public int n;
        public List<EdgeDef> edges;
        public Integer source = 0;
        public String weight_model = "edge";
        public boolean directed = true;
    }

    public static void main(String[] args) throws IOException {
        String inPath = args.length > 0 ? args[0] : "src/main/resources/tasks.json";
        ObjectMapper om = new ObjectMapper();
        Input input = om.readValue(new File(inPath), Input.class);
        Metrics metrics = new Metrics();

        @SuppressWarnings("unchecked")
        List<int[]>[] adj = (List<int[]>[]) new List[input.n];
        for (int i = 0; i < input.n; i++) adj[i] = new ArrayList<>();
        for (EdgeDef e : input.edges) adj[e.u].add(new int[]{e.v, (int)e.w});

        long t0 = System.nanoTime();
        TarjanSCC tarjan = new TarjanSCC(input.n, adj, metrics);
        List<List<Integer>> sccs = tarjan.run();
        long t1 = System.nanoTime();
        metrics.setTiming("scc_ns", t1 - t0);
        sccs.sort(Comparator.comparingInt(c -> Collections.min(c)));

        Condensation cond = new Condensation(input.n, sccs, adj);

        long t2 = System.nanoTime();
        KahnTopo kahn = new KahnTopo(cond.compCount, cond.cadj, metrics);
        List<Integer> topo = kahn.topoOrder();
        long t3 = System.nanoTime();
        metrics.setTiming("topo_ns", t3 - t2);

        List<Integer> derived = new ArrayList<>();
        for (int comp : topo) derived.addAll(sccs.get(comp));

        long t4 = System.nanoTime();
        DagShortestPaths dsp = new DagShortestPaths(cond.compCount, cond.cadj, metrics);
        int sourceComp = cond.compId[input.source];
        long[] shortest = dsp.shortestFrom(sourceComp, topo);
        long t5 = System.nanoTime();
        metrics.setTiming("dagsp_shortest_ns", t5 - t4);

        long t6 = System.nanoTime();
        DagShortestPaths.LongestResult longest = dsp.longestPaths(topo);
        long t7 = System.nanoTime();
        metrics.setTiming("dagsp_longest_ns", t7 - t6);

        long best = Long.MIN_VALUE;
        int bestNode = -1;
        for (int i = 0; i < cond.compCount; i++) {
            if (longest.dist[i] > best) {
                best = longest.dist[i];
                bestNode = i;
            }
        }

        List<Integer> criticalCompPath = bestNode == -1 ? Collections.emptyList()
                : DagShortestPaths.reconstructLongest(longest.pred, bestNode);
        List<Integer> criticalVertices = new ArrayList<>();
        for (int comp : criticalCompPath) criticalVertices.addAll(sccs.get(comp));

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("n", input.n);
        out.put("m", input.edges.size());
        out.put("sccs", sccs);
        out.put("scc_sizes", sccs.stream().map(List::size).toList());
        out.put("condensation_nodes", cond.compCount);
        out.put("condensation_edges", Arrays.stream(cond.cadj).mapToInt(List::size).sum());
        out.put("topo_components", topo);
        out.put("derived_tasks_order", derived);
        out.put("source_vertex", input.source);
        out.put("source_component", sourceComp);
        out.put("shortest_component_distances", shortest);
        out.put("critical_path_components", criticalCompPath);
        out.put("critical_path_vertices", criticalVertices);
        out.put("critical_path_length", best == Long.MIN_VALUE ? null : best);
        out.put("metrics", metrics.toMap());
        out.put("weight_model", input.weight_model);
        out.put("directed", input.directed);

        om.writerWithDefaultPrettyPrinter().writeValue(new File("analysis_output.json"), out);
        System.out.println("Analysis written to analysis_output.json");
    }
}

