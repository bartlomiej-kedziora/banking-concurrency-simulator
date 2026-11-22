package pl.eked.lab.banking.api.routing;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
            var route3 = new HttpRoute(
                HttpMethod.GET,
                "/api/v1/anotherpath",
                mock()
            );
            var route4 = new HttpRoute(
                HttpMethod.POST,
                "/api/v1/anotherpath",
                mock()
            );

            var routes1 = List.of(route1, route2);
            var routes2 = List.of(route3, route4);

            var controller1 = Fixtures.givenController(routes1);
            var controller2 = Fixtures.givenController(routes2);
            var controllers = List.of(controller1, controller2);

            //when / then
            assertDoesNotThrow(() -> new HttpRouter(controllers));
        }

        @ParameterizedTest
        @CsvSource(textBlock = """
                /api/v1/path, /api/v1/path, /api/v1/anotherpath, /api/v1/anotherpath, /api/v1/path
                /api/v1/patha, /api/v1/patb, /api/v1/pathc, /api/v1/patha, /api/v1/patha
                """)
        void shouldRejectDuplicateRoutes(
            String firstPath,
            String secondPath,
            String thirdPath,
            String fourthPath,
            String duplicatePath
        ) {
            //given
            var route1 = new HttpRoute(
                HttpMethod.GET,
                firstPath,
                mock()
            );
            var route2 = new HttpRoute(
                HttpMethod.GET,
                secondPath,
                mock()
            );
            var route3 = new HttpRoute(
                HttpMethod.GET,
                thirdPath,
                mock()
            );
            var route4 = new HttpRoute(
                HttpMethod.GET,
                fourthPath,
                mock()
            );

            var routes1 = List.of(route1, route2);
            var routes2 = List.of(route3, route4);

            var controller1 = Fixtures.givenController(routes1);
            var controller2 = Fixtures.givenController(routes2);
            var controllers = List.of(controller1, controller2);
            var expectedExceptionMessage = "Duplicate route found for GET " + duplicatePath;

            //when
            var exception = assertThrows(
                IllegalStateException.class,
                () -> new HttpRouter(controllers)
            );

            //then
            assertEquals(expectedExceptionMessage, exception.getMessage());
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