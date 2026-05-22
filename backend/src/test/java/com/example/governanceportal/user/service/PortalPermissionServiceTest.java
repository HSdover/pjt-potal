package com.example.governanceportal.user.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.saml2.provider.service.authentication.DefaultSaml2AuthenticatedPrincipal;

import com.example.governanceportal.user.config.SamlPermissionMappingProperties;

class PortalPermissionServiceTest {

    @Test
    void mapsSamlGroupToInternalPermissions() {
        SamlPermissionMappingProperties properties = new SamlPermissionMappingProperties();
        properties.setGroupPermissionMappings(Map.of(
            "KNOX_DG_PORTAL_ADMIN",
            List.of("DASHBOARD_READ", "BATCH_ADMIN")
        ));
        PortalPermissionService service = new PortalPermissionService(properties);
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(
            samlPrincipal(Map.of("groups", List.of("KNOX_DG_PORTAL_ADMIN"))),
            null,
            "ROLE_USER"
        );

        List<String> permissions = service.permissions(authentication);

        assertThat(permissions)
            .containsExactly("DASHBOARD_READ", "BATCH_ADMIN")
            .doesNotContain("KNOX_DG_PORTAL_ADMIN", "ROLE_USER");
        assertThat(service.hasPermission(authentication, "BATCH_ADMIN")).isTrue();
    }

    @Test
    void mapsMemberOfDistinguishedNameByCommonName() {
        SamlPermissionMappingProperties properties = new SamlPermissionMappingProperties();
        properties.setGroupPermissionMappings(Map.of(
            "KNOX_DG_PORTAL_VIEWER",
            List.of("DASHBOARD_READ")
        ));
        PortalPermissionService service = new PortalPermissionService(properties);
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(
            samlPrincipal(Map.of("memberOf", List.of("CN=KNOX_DG_PORTAL_VIEWER,OU=Groups,DC=example,DC=com"))),
            null
        );

        assertThat(service.permissions(authentication)).containsExactly("DASHBOARD_READ");
    }

    @Test
    void keepsOnlyDirectPermissionAttributesWhenNoGroupMappingExists() {
        SamlPermissionMappingProperties properties = new SamlPermissionMappingProperties();
        properties.setDirectPermissionAttributes(List.of("permissions"));
        PortalPermissionService service = new PortalPermissionService(properties);
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(
            samlPrincipal(Map.of(
                "permissions", List.of("DASHBOARD_READ"),
                "groups", List.of("KNOX_DG_UNMAPPED")
            )),
            null
        );

        assertThat(service.permissions(authentication))
            .containsExactly("DASHBOARD_READ")
            .doesNotContain("KNOX_DG_UNMAPPED");
    }

    @Test
    void doesNotExposeRawPermissionAttributesByDefault() {
        PortalPermissionService service = new PortalPermissionService(new SamlPermissionMappingProperties());
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(
            samlPrincipal(Map.of("permissions", List.of("KNOX_DG_PORTAL_ADMIN"))),
            null
        );

        assertThat(service.permissions(authentication)).isEmpty();
    }

    private DefaultSaml2AuthenticatedPrincipal samlPrincipal(Map<String, List<String>> attributes) {
        Map<String, List<Object>> samlAttributes = new LinkedHashMap<>();
        attributes.forEach((name, values) -> samlAttributes.put(name, new ArrayList<>(values)));

        return new DefaultSaml2AuthenticatedPrincipal("user1", samlAttributes);
    }
}
