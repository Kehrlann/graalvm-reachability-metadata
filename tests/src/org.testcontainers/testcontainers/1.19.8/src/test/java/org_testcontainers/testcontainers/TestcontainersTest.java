/*
 * Copyright and related rights waived via CC0
 *
 * You should have received a copy of the CC0 legalcode along with this
 * work. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */
package org_testcontainers.testcontainers;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

// This test does not use testcontainers/ryuk image for removing containers, but uses the Java
// AutoCloseable to bring down containers after the test succeeds.
// The Ryuk image cannot be used because it contains many CVEs, and new versions of Ryuk are not
// compatible with Testcontainers v1.19.8 .
// ISSUE: https://github.com/oracle/graalvm-reachability-metadata/issues/250
class TestcontainersTest {
    private static final boolean DEBUG = false;

    @BeforeAll
    static void beforeAll() {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", DEBUG ? "debug" : "warn");
        // Ensure we are not running Ryuk
        String ryukDisabled = System.getenv().get("TESTCONTAINERS_RYUK_DISABLED");
        assertThat(ryukDisabled)
                .withFailMessage("Expected Ryuk to be disabled through the environment variable TESTCONTAINERS_RYUK_DISABLED=true, but found TESTCONTAINERS_RYUK_DISABLED=%s", ryukDisabled)
                .isEqualTo("true");
    }

    @Test
    void test() throws Exception {
        try (GenericContainer<?> nginx = new GenericContainer<>("nginx:1-alpine-slim")) {
            nginx.withExposedPorts(80).start();
            HttpClient httpClient = HttpClient.newBuilder().build();
            String url = String.format("http://%s:%d", nginx.getHost(), nginx.getFirstMappedPort());
            HttpResponse<String> response = httpClient.send(HttpRequest.newBuilder().GET().uri(URI.create(url)).build(), HttpResponse.BodyHandlers.ofString());
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.body()).contains("<h1>Welcome to nginx!</h1>");
        }
    }
}
