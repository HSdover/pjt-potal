package com.example.governanceportal;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(
    username = "test-runner",
    authorities = {
        "DASHBOARD_READ",
        "SAMPLE_READ",
        "SAMPLE_CREATE",
        "SAMPLE_UPDATE",
        "SAMPLE_DELETE",
        "SAMPLE_JPA_READ",
        "SAMPLE_JPA_CREATE",
        "SAMPLE_JPA_UPDATE",
        "SAMPLE_JPA_DELETE",
        "SAMPLE_JPA_EXPORT",
        "SAMPLE_JPA_IMPORT",
        "META_VIEW",
        "REF_VIEW",
        "PERMISSION_MANAGE",
        "BATCH_ADMIN"
    }
)
class GovernancePortalApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    @WithAnonymousUser
    void currentUserIsAnonymousBeforeLogin() throws Exception {
        mockMvc.perform(get("/api/me"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value("anonymous"))
            .andExpect(jsonPath("$.authenticated").value(false));
    }

    @Test
    @WithAnonymousUser
    void apiRequiresLoginBeforeInternalScreenAccess() throws Exception {
        mockMvc.perform(post("/api/samples/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "pageNo": 1,
                      "pageSize": 10,
                      "sort": [],
                      "filters": {"keyword": ""}
                    }
                    """))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithAnonymousUser
    void localUserCanLoginWithConfiguredAccount() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "userId": "local-test",
                      "password": "local-test-pass"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value("local-test"))
            .andExpect(jsonPath("$.displayName").value("Local Test User"))
            .andExpect(jsonPath("$.authenticated").value(true))
            .andExpect(jsonPath("$.permissions[?(@ == 'DASHBOARD_READ')]").exists())
            .andExpect(jsonPath("$.permissions[?(@ == 'BATCH_ADMIN')]").exists())
            .andExpect(jsonPath("$.permissions[?(@ == 'META_VIEW')]").exists());
    }

    @Test
    void integratedMetadataCanBeSearchedAndViewed() throws Exception {
        mockMvc.perform(post("/api/metadata/integrated/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "pageNo": 1,
                      "pageSize": 10,
                      "sort": [{"field": "metaName", "direction": "asc"}],
                      "filters": {
                        "metaType": "STRUCTURED",
                        "searchTypes": ["BUSINESS", "OWNER"],
                        "keyword": "고객"
                      }
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalCount").value(1))
            .andExpect(jsonPath("$.rows[0].metaId").value("STM-001"))
            .andExpect(jsonPath("$.rows[0].metaType").value("STRUCTURED"));

        mockMvc.perform(get("/api/metadata/integrated/STM-001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.metaName").value("고객 기본 테이블"))
            .andExpect(jsonPath("$.sections[0].title").value("기본정보"))
            .andExpect(jsonPath("$.columns[0].columnName").value("CUST_ID"));

        mockMvc.perform(get("/api/metadata/integrated/FIM-001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.metaType").value("FILE"))
            .andExpect(jsonPath("$.sections[?(@.title == '보안정보')]").exists())
            .andExpect(jsonPath("$.sections[?(@.title == '파싱정보')]").exists())
            .andExpect(jsonPath("$.sections[?(@.title == '청킹정보')]").exists())
            .andExpect(jsonPath("$.sections[?(@.title == '저장정보')]").exists());
    }

    @Test
    void permissionManagementDataCanBeViewedAndSaved() throws Exception {
        mockMvc.perform(get("/api/system/permissions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.permissions[?(@.permissionCode == 'PERMISSION_MANAGE')]").exists())
            .andExpect(jsonPath("$.subjects[?(@.subjectType == 'IAM_ROLE' && @.subjectId == 'ADMIN')]").exists())
            .andExpect(jsonPath("$.subjects[?(@.subjectType == 'IAM_ROLE' && @.subjectId == 'AI_AGENT_ADMIN')]").exists())
            .andExpect(jsonPath("$.subjects[?(@.subjectType == 'IAM_ROLE' && @.subjectId == 'DATA_ADMIN')]").exists());

        mockMvc.perform(put("/api/system/permissions/assignments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "subjectType": "GROUP",
                      "subjectId": "TEST_PERMISSION_GROUP",
                      "subjectName": "Test Permission Group",
                      "permissionCodes": ["DASHBOARD_READ", "META_VIEW"]
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.subjectType").value("GROUP"))
            .andExpect(jsonPath("$.subjectId").value("TEST_PERMISSION_GROUP"))
            .andExpect(jsonPath("$.permissionCodes[?(@ == 'DASHBOARD_READ')]").exists())
            .andExpect(jsonPath("$.permissionCodes[?(@ == 'META_VIEW')]").exists());

        mockMvc.perform(get("/api/system/permissions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.subjects[?(@.subjectType == 'GROUP' && @.subjectId == 'TEST_PERMISSION_GROUP')]").exists())
            .andExpect(jsonPath("$.assignments[?(@.subjectType == 'GROUP' && @.subjectId == 'TEST_PERMISSION_GROUP')]").exists());
    }

    @Test
    @WithMockUser(username = "viewer", authorities = {"DASHBOARD_READ"})
    void permissionManagementRequiresManagePermission() throws Exception {
        mockMvc.perform(get("/api/system/permissions"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "viewer", authorities = {"SAMPLE_JPA_READ"})
    void sampleJpaWriteAndDownloadRequireDedicatedPermissions() throws Exception {
        mockMvc.perform(post("/api/samples-jpa")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Forbidden Create",
                      "description": "This request should be blocked"
                    }
                    """))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/samples-jpa/excel/download")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "pageNo": 1,
                      "pageSize": 10,
                      "sort": [],
                      "filters": {"keyword": ""}
                    }
                    """))
            .andExpect(status().isForbidden());
    }

    @Test
    void referenceAttachmentDownloadUsesCommonServerModule() throws Exception {
        mockMvc.perform(get("/api/_ref/attachments/sample"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
            .andExpect(header().exists(HttpHeaders.CONTENT_LENGTH))
            .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("attachment")))
            .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("attachment-sample.txt")))
            .andExpect(header().string("X-Content-Type-Options", "nosniff"))
            .andExpect(content().string(containsString("backend common attachment download module")));
    }

    @Test
    void referenceAttachmentCanBeUploadedAndDownloadedAgain() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "uploaded-note.txt",
            MediaType.TEXT_PLAIN_VALUE,
            "uploaded attachment content".getBytes(StandardCharsets.UTF_8)
        );

        String responseBody = mockMvc.perform(multipart("/api/_ref/attachments/sample").file(file))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.attachmentId").exists())
            .andExpect(jsonPath("$.fileName").value("uploaded-note.txt"))
            .andExpect(jsonPath("$.contentType").value(MediaType.TEXT_PLAIN_VALUE))
            .andExpect(jsonPath("$.downloadUrl").exists())
            .andReturn()
            .getResponse()
            .getContentAsString();

        Map<String, Object> response = objectMapper.readValue(responseBody, new TypeReference<>() {
        });
        String downloadUrl = String.valueOf(response.get("downloadUrl"));

        mockMvc.perform(get(downloadUrl))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
            .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("attachment")))
            .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("uploaded-note.txt")))
            .andExpect(header().string("X-Content-Type-Options", "nosniff"))
            .andExpect(content().string("uploaded attachment content"));
    }

    @Test
    void referenceBoardCrudWorks() throws Exception {
        mockMvc.perform(post("/api/reference/boards/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "pageNo": 1,
                      "pageSize": 10,
                      "sort": [{"field": "createdAt", "direction": "desc"}],
                      "filters": {"keyword": "", "category": ""}
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalCount", greaterThan(0)))
            .andExpect(jsonPath("$.rows[0].title").exists());

        Integer id = mockMvc.perform(post("/api/reference/boards")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "title": "Board CRUD Test",
                      "category": "질문",
                      "writerName": "Tester",
                      "content": "Created from board CRUD test"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id", greaterThan(0)))
            .andExpect(jsonPath("$.title").value("Board CRUD Test"))
            .andReturn()
            .getResponse()
            .getContentAsString()
            .replaceAll(".*\\\"id\\\":(\\d+).*", "$1")
            .transform(Integer::valueOf);

        mockMvc.perform(get("/api/reference/boards/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id))
            .andExpect(jsonPath("$.viewCount").value(1));

        mockMvc.perform(put("/api/reference/boards/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "title": "Board CRUD Test Updated",
                      "category": "공지",
                      "writerName": "Tester",
                      "content": "Updated from board CRUD test"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id))
            .andExpect(jsonPath("$.title").value("Board CRUD Test Updated"))
            .andExpect(jsonPath("$.viewCount").value(1));

        mockMvc.perform(post("/api/reference/boards/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "pageNo": 1,
                      "pageSize": 10,
                      "sort": [{"field": "id", "direction": "desc"}],
                      "filters": {"keyword": "Updated", "category": "공지"}
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalCount").value(1))
            .andExpect(jsonPath("$.rows[0].id").value(id));

        mockMvc.perform(delete("/api/reference/boards/{id}", id))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/reference/boards/{id}", id))
            .andExpect(status().isNotFound());
    }

    @Test
    void referenceBoardAttachmentCanBeUploadedSavedAndDownloaded() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "board-attachment.txt",
            MediaType.TEXT_PLAIN_VALUE,
            "board attachment content".getBytes(StandardCharsets.UTF_8)
        );

        String uploadBody = mockMvc.perform(multipart("/api/reference/boards/attachments").file(file))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.attachmentId").exists())
            .andExpect(jsonPath("$.fileName").value("board-attachment.txt"))
            .andExpect(jsonPath("$.downloadUrl").exists())
            .andReturn()
            .getResponse()
            .getContentAsString();

        Map<String, Object> attachment = objectMapper.readValue(uploadBody, new TypeReference<>() {
        });

        String createBody = objectMapper.writeValueAsString(Map.of(
            "title", "Board Attachment Test",
            "category", "자료",
            "writerName", "Tester",
            "content", "<p>Created with attachment</p>",
            "attachment", attachment
        ));

        Integer id = mockMvc.perform(post("/api/reference/boards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.attachment.attachmentId").value(String.valueOf(attachment.get("attachmentId"))))
            .andExpect(jsonPath("$.attachment.fileName").value("board-attachment.txt"))
            .andExpect(jsonPath("$.attachment.downloadUrl").value(String.valueOf(attachment.get("downloadUrl"))))
            .andReturn()
            .getResponse()
            .getContentAsString()
            .replaceAll(".*\\\"id\\\":(\\d+).*", "$1")
            .transform(Integer::valueOf);

        mockMvc.perform(get("/api/reference/boards/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.attachment.fileName").value("board-attachment.txt"));

        mockMvc.perform(get(String.valueOf(attachment.get("downloadUrl"))))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
            .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("attachment")))
            .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("board-attachment.txt")))
            .andExpect(header().string("X-Content-Type-Options", "nosniff"))
            .andExpect(content().string("board attachment content"));
    }

    @Test
    void batchJobCanBeListedAndRunManually() throws Exception {
        mockMvc.perform(get("/api/admin/batch/jobs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("sampleMaintenanceJob"));

        mockMvc.perform(post("/api/admin/batch/jobs/sampleMaintenanceJob/run")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "parameters": {
                        "requestedBy": "test"
                      }
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.jobName").value("sampleMaintenanceJob"))
            .andExpect(jsonPath("$.status").value("COMPLETED"));
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
