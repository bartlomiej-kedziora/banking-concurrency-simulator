package pl.eked.lab.banking.api.routing;

import java.util.HashMap;
import java.util.Map;
import pl.eked.lab.banking.api.model.HttpMethod;
import pl.eked.lab.banking.api.model.HttpRoute;

final class RouteNode {
    private Map<String, RouteNode> staticChildren = new HashMap<>();
    private RouteNode dynamicChild;
    private Map<HttpMethod, HttpRoute> routes = new HashMap<>();
    private boolean frozen;

    RouteNode getOrCreateStaticChild(String segment) {
        this.ensureNotFrozen();
        return staticChildren.computeIfAbsent(segment, _ -> new RouteNode());
    }

    RouteNode getOrCreateDynamicChild() {
        this.ensureNotFrozen();
        if (dynamicChild == null) {
            dynamicChild = new RouteNode();
        }

        return dynamicChild;
    }

    void registerRoute(HttpRoute route) {
        this.ensureNotFrozen();
        var registeredRoute = routes.putIfAbsent(route.method(), route);
        if (registeredRoute != null) {
            throw new IllegalStateException("Route registration conflict for HTTP method "
                + route.method().name()
                + ": '"
                + route.path()
                + "' conflicts with existing route '"
                + registeredRoute.path()
                + "'"
            );
        }
    }

    void freeze() {
        if (frozen) {
            return;
        }

        staticChildren.values().forEach(RouteNode::freeze);
        if (dynamicChild != null) {
            dynamicChild.freeze();
        }
        staticChildren = Map.copyOf(staticChildren);
        routes = Map.copyOf(routes);
        frozen = true;
    }

    private void ensureNotFrozen() {
        if (frozen) {
            throw new IllegalStateException("Route node has been frozen");
        }
    }
}
