package com.gepardec.mega.application.filter;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.security.SecurityScheme;

public class MegaCronSecuritySchemaOASFilter implements OASFilter {

    @Override
    public SecurityScheme filterSecurityScheme(SecurityScheme securityScheme) {
        if (securityScheme.getType() == SecurityScheme.Type.OAUTH2) {
            securityScheme.getFlows().getClientCredentials().tokenUrl(ConfigProvider.getConfig().getConfigValue("mega.oauth.issuer").getValue() + "/protocol/openid-connect/token");
            return OASFilter.super.filterSecurityScheme(securityScheme);
        } else {
            return null;
        }
    }
}
