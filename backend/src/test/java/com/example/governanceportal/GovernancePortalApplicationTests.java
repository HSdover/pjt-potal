package com.example.governanceportal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
    void sampleCrudWorks() throws Exception {
        Integer id = mockMvc.perform(post("/api/samples")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "CRUD Sample",
                      "description": "Created from test"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id", greaterThan(0)))
            .andExpect(jsonPath("$.name").value("CRUD Sample"))
            .andReturn()
            .getResponse()
            .getContentAsString()
            .replaceAll(".*\\\"id\\\":(\\d+).*", "$1")
            .transform(Integer::valueOf);

        mockMvc.perform(put("/api/samples/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "CRUD Sample Updated",
                      "description": "Updated from test"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id))
            .andExpect(jsonPath("$.name").value("CRUD Sample Updated"));

        mockMvc.perform(post("/api/samples/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "pageNo": 1,
                      "pageSize": 10,
                      "sort": [{"field": "id", "direction": "asc"}],
                      "filters": {"keyword": "CRUD Sample Updated"}
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalCount").value(1))
            .andExpect(jsonPath("$.rows[0].id", is(id)));

        mockMvc.perform(delete("/api/samples/{id}", id))
            .andExpect(status().isNoContent());
    }

    @Test
    void sampleJpaCrudWorks() throws Exception {
        Integer id = mockMvc.perform(post("/api/samples-jpa")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "JPA CRUD Sample",
                      "description": "Created from JPA test"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id", greaterThan(0)))
            .andExpect(jsonPath("$.name").value("JPA CRUD Sample"))
            .andReturn()
            .getResponse()
            .getContentAsString()
            .replaceAll(".*\\\"id\\\":(\\d+).*", "$1")
            .transform(Integer::valueOf);

        mockMvc.perform(put("/api/samples-jpa/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "JPA CRUD Sample Updated",
                      "description": "Updated from JPA test"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id))
            .andExpect(jsonPath("$.name").value("JPA CRUD Sample Updated"));

        mockMvc.perform(post("/api/samples-jpa/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "pageNo": 1,
                      "pageSize": 10,
                      "sort": [{"field": "id", "direction": "asc"}],
                      "filters": {"keyword": "JPA CRUD Sample Updated"}
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalCount").value(1))
            .andExpect(jsonPath("$.rows[0].id", is(id)));

        mockMvc.perform(delete("/api/samples-jpa/{id}", id))
            .andExpect(status().isNoContent());
    }

    @Test
    void sampleJpaSearchSupportsEmptyKeywordAndSort() throws Exception {
        Integer firstId = null;
        Integer secondId = null;

        try {
            firstId = mockMvc.perform(post("/api/samples-jpa")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "name": "QueryDSL Sort A",
                          "description": "Created for empty keyword search"
                        }
                        """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString()
                .replaceAll(".*\\\"id\\\":(\\d+).*", "$1")
                .transform(Integer::valueOf);

            secondId = mockMvc.perform(post("/api/samples-jpa")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "name": "QueryDSL Sort B",
                          "description": "Created for empty keyword search"
                        }
                        """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString()
                .replaceAll(".*\\\"id\\\":(\\d+).*", "$1")
                .transform(Integer::valueOf);

            mockMvc.perform(post("/api/samples-jpa/search")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "pageNo": 1,
                          "pageSize": 2,
                          "sort": [{"field": "name", "direction": "desc"}],
                          "filters": {"keyword": ""}
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rows.length()").value(2))
                .andExpect(jsonPath("$.rows[0].name").value("QueryDSL Sort B"))
                .andExpect(jsonPath("$.rows[1].name").value("QueryDSL Sort A"))
                .andExpect(jsonPath("$.totalCount", greaterThan(0)))
                .andExpect(jsonPath("$.pageNo").value(1))
                .andExpect(jsonPath("$.pageSize").value(2));
        } finally {
            if (secondId != null) {
                mockMvc.perform(delete("/api/samples-jpa/{id}", secondId));
            }
            if (firstId != null) {
                mockMvc.perform(delete("/api/samples-jpa/{id}", firstId));
            }
        }
    }
}
