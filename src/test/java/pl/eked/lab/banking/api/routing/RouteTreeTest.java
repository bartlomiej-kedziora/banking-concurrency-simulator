package pl.eked.lab.banking.api.routing;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import pl.eked.lab.banking.api.model.HttpMethod;
import pl.eked.lab.banking.api.model.HttpResponse;
import pl.eked.lab.banking.api.model.HttpRoute;

class RouteTreeTest {

    @Nested
    class RouteRegistration {
        @Test
        void shouldRegisterStaticRoute() {
            //given
            var route = new HttpRoute(
                HttpMethod.GET,
                "/accounts",
                _ -> HttpResponse.ok("OK")
            );
            var givenRoutes = List.of(route);

            //when & then
            assertDoesNotThrow(() -> new RouteTree(givenRoutes));
        }

        @Test
        void shouldRegisterDynamicRoute() {
            //given
            var route = new HttpRoute(
                HttpMethod.GET,
                "/accounts/{id}",
                _ -> HttpResponse.ok("OK")
            );
            var givenRoutes = List.of(route);

            //when & then
            assertDoesNotThrow(() -> new RouteTree(givenRoutes));
        }

        @Test
        void shouldRegisterRootRoute() {
            //given
            var route = new HttpRoute(
                HttpMethod.GET,
                "/",
                _ -> HttpResponse.ok("OK")
            );
            var givenRoutes = List.of(route);

            //when & then
            assertDoesNotThrow(() -> new RouteTree(givenRoutes));
        }

        @Test
        void shouldRegisterEmptyRouteTree() {
            //given
            List<HttpRoute> givenRoutes = emptyList();

            //when & then
            assertDoesNotThrow(() -> new RouteTree(givenRoutes));
        }

        @Test
        void shouldRegisterRouteWithMultipleParameters() {
            //given
            var route = new HttpRoute(
                HttpMethod.GET,
                "/accounts/{accountId}/transactions/{transactionId}",
                _ -> HttpResponse.ok("OK")
            );
            var givenRoutes = List.of(route);

            //when & then
            assertDoesNotThrow(() -> new RouteTree(givenRoutes));
        }

        @Test
        void shouldRegisterMultipleRoutes() {
            //given
            var route1 = new HttpRoute(
                HttpMethod.GET,
                "/accounts1",
                _ -> HttpResponse.ok("OK")
            );
            var route2 = new HttpRoute(
                HttpMethod.GET,
                "/accounts2",
                _ -> HttpResponse.ok("OK")
            );
            var givenRoutes = List.of(route1, route2);

            //when & then
            assertDoesNotThrow(() -> new RouteTree(givenRoutes));
        }

        @Test
        void shouldRegisterRoutesWithSharedPrefix() {
            //given
            var route1 = new HttpRoute(
                HttpMethod.GET,
                "/accounts",
                _ -> HttpResponse.ok("OK")
            );
            var route2 = new HttpRoute(
                HttpMethod.GET,
                "/accounts/summary",
                _ -> HttpResponse.ok("OK")
            );
            var givenRoutes = List.of(route1, route2);

            //when & then
            assertDoesNotThrow(() -> new RouteTree(givenRoutes));
        }

        @Test
        void shouldRegisterStaticAndDynamicRoutes() {
            //given
            var route1 = new HttpRoute(
                HttpMethod.GET,
                "/accounts/summary",
                _ -> HttpResponse.ok("OK")
            );
            var route2 = new HttpRoute(
                HttpMethod.GET,
                "/accounts/{accountId}",
                _ -> HttpResponse.ok("OK")
            );
            var givenRoutes = List.of(route1, route2);

            //when & then
            assertDoesNotThrow(() -> new RouteTree(givenRoutes));
        }

        @Test
        void shouldAllowDifferentMethodsForSamePath() {
            //given
            var route1 = new HttpRoute(
                HttpMethod.GET,
                "/accounts",
                _ -> HttpResponse.ok("OK")
            );
            var route2 = new HttpRoute(
                HttpMethod.POST,
                "/accounts",
                _ -> HttpResponse.ok("OK")
            );
            var givenRoutes = List.of(route1, route2);

            //when & then
            assertDoesNotThrow(() -> new RouteTree(givenRoutes));
        }

        @Test
        void shouldAllowDifferentMethodsForSameDynamicPath() {
            //given
            var route1 = new HttpRoute(
                HttpMethod.GET,
                "/accounts/{id}",
                _ -> HttpResponse.ok("OK")
            );
            var route2 = new HttpRoute(
                HttpMethod.POST,
                "/accounts/{accountId}",
                _ -> HttpResponse.ok("OK")
            );
            var givenRoutes = List.of(route1, route2);

            //when & then
            assertDoesNotThrow(() -> new RouteTree(givenRoutes));
        }

        @Test
        void shouldRejectRepeatedParameterNames() {
            //given
            var route = new HttpRoute(
                HttpMethod.GET,
                "/accounts/{id}/transactions/{id}",
                _ -> HttpResponse.ok("OK")
            );
            var givenRoutes = List.of(route);

            //when
            var exception = assertThrows(
                IllegalArgumentException.class,
                () -> new RouteTree(givenRoutes)
            );

            //then
            assertEquals("Duplicate path parameter '{id}' in route '/accounts/{id}/transactions/{id}'",  exception.getMessage());
        }

        @Test
        void shouldRejectDuplicateStaticRoutes() {
            //given
            var route1 = new HttpRoute(
                HttpMethod.GET,
                "/accounts",
                _ -> HttpResponse.ok("OK")
            );
            var route2 = new HttpRoute(
                HttpMethod.GET,
                "/accounts",
                _ -> HttpResponse.ok("OK")
            );
            var givenRoutes = List.of(route1, route2);

            //when
            var exception = assertThrows(
                IllegalStateException.class,
                () -> new RouteTree(givenRoutes)
            );

            //then
            assertEquals("Route registration conflict for HTTP method GET: '/accounts' conflicts with existing route '/accounts'",  exception.getMessage());
        }

        @Test
        void shouldRejectDuplicateDynamicRoutes() {
            //given
            var route1 = new HttpRoute(
                HttpMethod.GET,
                "/accounts/{accountId}",
                _ -> HttpResponse.ok("OK")
            );
            var route2 = new HttpRoute(
                HttpMethod.GET,
                "/accounts/{accountId}",
                _ -> HttpResponse.ok("OK")
            );
            var givenRoutes = List.of(route1, route2);

            //when
            var exception = assertThrows(
                IllegalStateException.class,
                () -> new RouteTree(givenRoutes)
            );

            //then
            assertEquals("Route registration conflict for HTTP method GET: '/accounts/{accountId}' conflicts with existing route '/accounts/{accountId}'",  exception.getMessage());
        }

        @Test
        void shouldRejectAmbiguousDynamicRoutes() {
            //given
            var route1 = new HttpRoute(
                HttpMethod.GET,
                "/accounts/{accountId}",
                _ -> HttpResponse.ok("OK")
            );
            var route2 = new HttpRoute(
                HttpMethod.GET,
                "/accounts/{id}",
                _ -> HttpResponse.ok("OK")
            );
            var givenRoutes = List.of(route1, route2);

            //when
            var exception = assertThrows(
                IllegalStateException.class,
                () -> new RouteTree(givenRoutes)
            );

            //then
            assertEquals("Route registration conflict for HTTP method GET: '/accounts/{id}' conflicts with existing route '/accounts/{accountId}'",  exception.getMessage());
        }

        @Test
        void shouldRejectDuplicateRoutesWithTrailingSlash() {
            //given
            var route1 = new HttpRoute(
                HttpMethod.GET,
                "/accounts",
                _ -> HttpResponse.ok("OK")
            );
            var route2 = new HttpRoute(
                HttpMethod.GET,
                "/accounts/",
                _ -> HttpResponse.ok("OK")
            );
            var givenRoutes = List.of(route1, route2);

            //when
            var exception = assertThrows(
                IllegalStateException.class,
                () -> new RouteTree(givenRoutes)
            );

            //then
            assertEquals("Route registration conflict for HTTP method GET: '/accounts/' conflicts with existing route '/accounts'",  exception.getMessage());
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "", "accounts",
            "api/accounts",
            "//",
            "//accounts",
            "/accounts//",
            "/accounts///",
            "/api//accounts"
        })
        @NullSource
        void shouldRejectInvalidPath(String invalidPath) {
            //given
            var route = new HttpRoute(
                HttpMethod.GET,
                invalidPath,
                _ -> HttpResponse.ok("OK")
            );
            var givenRoutes = List.of(route);

            var expectedErrorMessage = "Invalid path: " + invalidPath;

            //when
            var exception = assertThrows(
                IllegalArgumentException.class,
                () -> new RouteTree(givenRoutes)
            );

            //then
            assertEquals(expectedErrorMessage,  exception.getMessage());
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "/accounts/{}",
            "/accounts/{id",
            "/accounts/id}",
            "/accounts/{{id}}",
            "/accounts/{2id}",
            "/accounts/{account Id}",
            "/accounts/{account-id}",
            "/accounts/{account_id}",
            "/accounts/{id!}",
            "/accounts/{ }",
            "/accounts/prefix{id}",
            "/accounts/{id}suffix",
            "/accounts/{id}{name}"
        })
        void shouldRejectInvalidDynamicSegment(String invalidPath) {
            //given
            var route = new HttpRoute(
                HttpMethod.GET,
                invalidPath,
                _ -> HttpResponse.ok("OK")
            );
            var givenRoutes = List.of(route);

            var expectedErrorMessage = "Invalid segment: .*";

            //when
            var exception = assertThrows(
                IllegalArgumentException.class,
                () -> new RouteTree(givenRoutes)
            );

            //then
            assertTrue(exception.getMessage().matches(expectedErrorMessage));
        }
    }
}