package com.example.healthcheck;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/** The app exposes no controllers of its own, so Actuator's health surface is all there is. */
@SpringBootTest
@AutoConfigureMockMvc
class HealthCheckApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("the application context starts")
    void contextLoads() {
        assertThat(this.mockMvc).isNotNull();
    }

    @Test
    @DisplayName("actuator reports UP on a clean start")
    void healthIsUp() throws Exception {
        this.mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components.ping.status").value("UP"))
                .andExpect(jsonPath("$.components.diskSpace.status").value("UP"));
    }

    @Test
    @DisplayName("component detail is visible to scrapers")
    void componentDetailIsVisible() throws Exception {
        this.mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.diskSpace.details.total").isNumber());
    }

    @Test
    @DisplayName("a single component is addressable by name")
    void singleComponentByName() throws Exception {
        this.mockMvc.perform(get("/actuator/health/diskSpace"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("the liveness and readiness groups resolve against their contributors")
    void healthGroupsResolve() throws Exception {
        this.mockMvc.perform(get("/actuator/health/liveness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components.livenessState.status").value("UP"));
        this.mockMvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components.readinessState.status").value("UP"));
    }

    @Test
    @DisplayName("the custom health API is gone")
    void customApiIsGone() throws Exception {
        this.mockMvc.perform(get("/api/v1/health")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("only health and info are exposed over HTTP")
    void exposureIsRestricted() throws Exception {
        this.mockMvc.perform(get("/actuator/env")).andExpect(status().isNotFound());
        this.mockMvc.perform(get("/actuator/beans")).andExpect(status().isNotFound());
        this.mockMvc.perform(get("/actuator/info")).andExpect(status().isOk());
    }

}
