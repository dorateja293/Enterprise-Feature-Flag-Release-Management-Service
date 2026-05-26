package com.teja.featureflagservice.controller;

import com.teja.featureflagservice.repository.FeatureRepository;
import com.teja.featureflagservice.repository.ReleaseHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FeatureControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FeatureRepository featureRepository;

    @Autowired
    private ReleaseHistoryRepository releaseHistoryRepository;

    @BeforeEach
    void cleanDatabase() {
        releaseHistoryRepository.deleteAll();
        featureRepository.deleteAll();
    }

    @Test
    void shouldCreateListToggleAndAuditFeatureFlag() throws Exception {
        mockMvc.perform(post("/api/feature")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "featureName": "dark-mode",
                                  "environment": "DEV",
                                  "enabled": false
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.featureName").value("dark-mode"))
                .andExpect(jsonPath("$.environment").value("DEV"))
                .andExpect(jsonPath("$.enabled").value(false));

        mockMvc.perform(get("/api/features/DEV"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].featureName").value("dark-mode"));

        mockMvc.perform(put("/api/toggle-feature")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "featureName": "dark-mode",
                                  "environment": "DEV",
                                  "enabled": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true));

        mockMvc.perform(get("/api/release-history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].featureName").value("dark-mode"))
                .andExpect(jsonPath("$[0].oldStatus").value(false))
                .andExpect(jsonPath("$[0].newStatus").value(true))
                .andExpect(jsonPath("$[0].environment").value("DEV"));
    }

    @Test
    void shouldReturnNotFoundWhenTogglingUnknownFeature() throws Exception {
        mockMvc.perform(put("/api/toggle-feature")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "featureName": "unknown-feature",
                                  "environment": "QA",
                                  "enabled": true
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
