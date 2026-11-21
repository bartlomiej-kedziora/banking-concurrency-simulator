package pl.eked.lab.banking.api.routing;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import pl.eked.lab.banking.api.controller.RestController;
import pl.eked.lab.banking.api.model.HttpMethod;
import pl.eked.lab.banking.api.model.HttpRoute;

class HttpRouterTest {

    @Nested
    class RouteRegistration {
        @Test
        void shouldRegisterSingleRoute() {
            //given
            var route = new HttpRoute(
                HttpMethod.GET,
                "/api/v1/path",
                mock()
            );
            var controller = Fixtures.givenController(route);
            var controllers = List.of(controller);

            //when / then
            assertDoesNotThrow(() -> new HttpRouter(controllers));
        }

        @Test
        void shouldRegisterMultipleRoutesForSamePath() {
            //given
            var route1 = new HttpRoute(
                HttpMethod.GET,
                "/api/v1/path",
                mock()
            );
            var route2 = new HttpRoute(
                HttpMethod.POST,
                "/api/v1/path",
                mock()
            );
            var routes = List.of(route1, route2);
            var controller = Fixtures.givenController(routes);
            var controllers = List.of(controller);

            //when / then
            assertDoesNotThrow(() -> new HttpRouter(controllers));
        }

        @Test
        void shouldRegisterRoutesFromMultipleControllers() {
            //given
            var route1 = new HttpRoute(
                HttpMethod.GET,
                "/api/v1/path",
                mock()
            );
            var route2 = new HttpRoute(
                HttpMethod.POST,
                "/api/v1/path",
                mock()
            );
            var controller1 = Fixtures.givenController(route1);
            var controller2 = Fixtures.givenController(route2);
            var controllers = List.of(controller1, controller2);

            //when / then
            assertDoesNotThrow(() -> new HttpRouter(controllers));
        }

        @Test
        void shouldRejectDuplicateRoutes() {
            //given
            var route1 = new HttpRoute(
                HttpMethod.GET,
                "/api/v1/path",
                mock()
            );
            var route2 = new HttpRoute(
                HttpMethod.GET,
                "/api/v1/path",
                mock()
            );
            var routes = List.of(route1, route2);
            var controller = Fixtures.givenController(routes);
            var controllers = List.of(controller);
            var givenExceptionMsg = "Duplicate route found for GET /api/v1/path";

            //when
            var exception = assertThrows(
                IllegalStateException.class,
                () -> new HttpRouter(controllers)
            );

            //then
            assertEquals(givenExceptionMsg, exception.getMessage());
        }

        @Test
        void shouldAllowSameMethodForDifferentPaths() {
            //given
            var route1 = new HttpRoute(
                HttpMethod.GET,
                "/api/v1/path1",
                mock()
            );
            var route2 = new HttpRoute(
                HttpMethod.GET,
                "/api/v1/path2",
                mock()
            );
            var routes = List.of(route1, route2);
            var controller = Fixtures.givenController(routes);
            var controllers = List.of(controller);

            //when / then
            assertDoesNotThrow(() -> new HttpRouter(controllers));
        }
    }

    private static class Fixtures {
        private static RestController givenController(HttpRoute route) {
            return () -> List.of(route);
        }

        private static RestController givenController(List<HttpRoute> routes) {
            return () -> routes;
        }
    }
}