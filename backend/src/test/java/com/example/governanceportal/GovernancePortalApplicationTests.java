package com.example.governanceportal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GovernancePortalApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
    }

    @Test
    void actuatorHealthIsUpWithoutRedis() throws Exception {
        mockMvc.perform(get("/actuator/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void catalogApisReturnSampleData() throws Exception {
        mockMvc.perform(get("/api/catalog/metadata"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].datasetName").value("심평원 요양기관 개설 현황 원천 CSV"));

        mockMvc.perform(get("/api/catalog/lineage"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].processName").value("파일 다운로드"));

        mockMvc.perform(get("/api/catalog/source-sample"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].institutionName").exists());
    }
}
