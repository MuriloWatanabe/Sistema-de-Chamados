package br.com.helpdesk.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class HealthControllerTest {

    private HealthController controller;

    @BeforeEach
    void setUp() {
        controller = new HealthController();
    }

    @Test
    void health_returnsStatusUp() {
        Map<String, String> result = controller.health();

        assertThat(result).containsEntry("status", "UP");
    }

    @Test
    void health_returnsMapWithOneEntry() {
        Map<String, String> result = controller.health();

        assertThat(result).hasSize(1);
    }

    @Test
    void health_doesNotReturnNull() {
        Map<String, String> result = controller.health();

        assertThat(result).isNotNull();
    }
}
