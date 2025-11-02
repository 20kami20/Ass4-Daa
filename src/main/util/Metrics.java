package util;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class Metrics {
    public final AtomicLong sccDfsVisits = new AtomicLong(0);
    public final AtomicLong sccDfsEdges = new AtomicLong(0);
    public final AtomicLong kahnPushes = new AtomicLong(0);
    public final AtomicLong kahnPops = new AtomicLong(0);
    public final AtomicLong dagspRelaxations = new AtomicLong(0);
    private final Map<String, Long> timingsNs = new HashMap<>();

    public void setTiming(String key, long ns) { timingsNs.put(key, ns); }
    public Map<String, Long> getTimings() { return timingsNs; }

    public Map<String, Long> toMap() {
        Map<String, Long> m = new HashMap<>();
        m.put("scc_dfs_visits", sccDfsVisits.get());
        m.put("scc_dfs_edges", sccDfsEdges.get());
        m.put("kahn_pushes", kahnPushes.get());
        m.put("kahn_pops", kahnPops.get());
        m.put("dagsp_relaxations", dagspRelaxations.get());
        m.putAll(timingsNs);
        return m;
    }
}
