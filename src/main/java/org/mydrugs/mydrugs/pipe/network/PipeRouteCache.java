package org.mydrugs.mydrugs.pipe.network;

import java.util.ArrayList;
import java.util.List;

public final class PipeRouteCache {
    private final List<PipeRoute> itemRoutes = new ArrayList<>();

    public void clear() {
        this.itemRoutes.clear();
    }

    public void addItemRoute(PipeRoute route) {
        this.itemRoutes.add(route);
    }

    public List<PipeRoute> itemRoutes() {
        return List.copyOf(this.itemRoutes);
    }

    public List<PipeRoute> itemRoutesFrom(PipeEndpoint source) {
        return this.itemRoutes.stream()
                .filter(route -> route.source().equals(source))
                .toList();
    }
}
