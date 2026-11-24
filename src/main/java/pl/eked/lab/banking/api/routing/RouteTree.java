package pl.eked.lab.banking.api.routing;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import pl.eked.lab.banking.api.model.HttpRoute;

final class RouteTree {
    private static final String DYNAMIC_SEGMENT_REGEX = "\\{[a-zA-Z][a-zA-Z0-9]*}";
    private final RouteNode root;

    public RouteTree(List<HttpRoute> routes) {
        this.root = new RouteNode();
        routes.forEach(this::registerRoute);
        this.root.freeze();
    }

    private void registerRoute(HttpRoute route) {
        var currentNode = this.root;
        var segments = splitPath(route.path());
        var dynamicSegments = new HashSet<String>();
        for (var segment : segments) {
            if (isDynamicSegment(segment)) {
                if (!dynamicSegments.add(segment)) {
                    throw new IllegalArgumentException("Duplicate path parameter '"
                        + segment + "' in route '" + route.path() + "'");
                }
                currentNode = currentNode.getOrCreateDynamicChild();
            } else {
                currentNode = currentNode.getOrCreateStaticChild(segment);
            }
        }
        currentNode.registerRoute(route);
    }

    private static List<String> splitPath(String path) {
        var isValidPath = path != null
            && path.startsWith("/")
            && !path.contains("//");
        if (!isValidPath) {
            throw new IllegalArgumentException("Invalid path: " + path);
        }

        return Arrays.stream(path.split("/", -1))
            .filter(it -> !it.isEmpty())
            .toList();
    }

    private static boolean isDynamicSegment(String segment) {
        var isStatic = !segment.contains("{") && !segment.contains("}");
        if (isStatic) {
            return false;
        }
        var isDynamic = segment.matches(DYNAMIC_SEGMENT_REGEX);
        if (isDynamic) {
            return true;
        }
        throw new IllegalArgumentException("Invalid segment: " + segment);
    }
}
