package com.example.governanceportal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    void featureApisReturnSampleData() throws Exception {
        mockMvc.perform(get("/api/metadata"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].datasetName").value("심평원 요양기관 개설 현황 원천 CSV"));

        mockMvc.perform(get("/api/lineage"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].processName").value("파일 다운로드"));

        mockMvc.perform(get("/api/source-sample"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].institutionName").exists());
    }

    @Test
    void sourceSampleSearchReturnsPagedListResponse() throws Exception {
        mockMvc.perform(post("/api/source-sample/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "pageNo": 1,
                      "pageSize": 10,
                      "sort": [{"field": "sampleId", "direction": "asc"}],
                      "filters": {"region": "", "keyword": ""}
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.rows.length()").value(10))
            .andExpect(jsonPath("$.totalCount").value(1000))
            .andExpect(jsonPath("$.pageNo").value(1))
            .andExpect(jsonPath("$.pageSize").value(10));
    }

    @Test
    void metadataSearchReturnsPagedListResponse() throws Exception {
        mockMvc.perform(post("/api/metadata/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "pageNo": 1,
                      "pageSize": 10,
                      "sort": [{"field": "metadataId", "direction": "asc"}],
                      "filters": {"datasetType": "원천 파일", "keyword": ""}
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.rows.length()").value(1))
            .andExpect(jsonPath("$.rows[0].datasetType").value("원천 파일"))
            .andExpect(jsonPath("$.totalCount").value(1))
            .andExpect(jsonPath("$.pageNo").value(1))
            .andExpect(jsonPath("$.pageSize").value(10));
    }
}
