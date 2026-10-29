package uk.gov.companieshouse.transactions.web;

import io.opentelemetry.api.GlobalOpenTelemetry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Unit tests for {@link OpenTelemetryAppenderInitializer}.
 *
 * <p>These tests verify that appender installation is safe under typical and boundary
 * initialisation scenarios.
 */
class OpenTelemetryAppenderInitializerTest {

    /**
     * Verifies that initialisation succeeds when a valid OpenTelemetry instance is provided.
     */
    @Test
    void afterPropertiesSetInstallsAppenderWhenOpenTelemetryProvided() {
        OpenTelemetryAppenderInitializer initializer =
                new OpenTelemetryAppenderInitializer(GlobalOpenTelemetry.get());

        assertDoesNotThrow(initializer::afterPropertiesSet);
    }

    /**
     * Verifies that initialisation remains safe when the initialiser is constructed with null.
     */
    @Test
    void afterPropertiesSetDoesNotThrowWhenOpenTelemetryIsNull() {
        OpenTelemetryAppenderInitializer initializer =
                new OpenTelemetryAppenderInitializer(null);

        assertDoesNotThrow(initializer::afterPropertiesSet);
    }

    /**
     * Verifies that repeated initialisation calls are idempotent and do not fail.
     */
    @Test
    void afterPropertiesSetCanBeCalledMultipleTimesWithoutError() {
        OpenTelemetryAppenderInitializer initializer =
                new OpenTelemetryAppenderInitializer(GlobalOpenTelemetry.get());

        assertDoesNotThrow(initializer::afterPropertiesSet);
        assertDoesNotThrow(initializer::afterPropertiesSet);
    }
}
