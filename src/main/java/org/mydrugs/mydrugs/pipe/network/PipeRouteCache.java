package org.mydrugs.mydrugs.pipe.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PipeRouteCache {
    private final List<PipeRoute> itemRoutes = new ArrayList<>();
    private final Map<PipeEndpoint, List<PipeRoute>> itemRoutesBySource = new HashMap<>();

    public void clear() {
        this.itemRoutes.clear();
        this.itemRoutesBySource.clear();
    }

    public void addItemRoute(PipeRoute route) {
        this.itemRoutes.add(route);
        this.itemRoutesBySource.computeIfAbsent(route.source(), ignored -> new ArrayList<>()).add(route);
    }

    public List<PipeRoute> itemRoutes() {
        return List.copyOf(this.itemRoutes);
    }

    public List<PipeRoute> itemRoutesFrom(PipeEndpoint source) {
        List<PipeRoute> routes = this.itemRoutesBySource.get(source);
        return routes == null ? List.of() : routes;
    }
}
