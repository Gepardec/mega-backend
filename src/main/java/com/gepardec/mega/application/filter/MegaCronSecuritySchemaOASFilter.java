package com.gepardec.mega.application.filter;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.security.OAuthFlow;
import org.eclipse.microprofile.openapi.models.security.OAuthFlows;
import org.eclipse.microprofile.openapi.models.security.SecurityScheme;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class MegaCronSecuritySchemaOASFilter implements OASFilter {

    private static final String TOKEN_ENDPOINT_PATH = "/protocol/openid-connect/token";
    private static final String AUTHORIZATION_ENDPOINT_PATH = "/protocol/openid-connect/auth";

    @Override
    public SecurityScheme filterSecurityScheme(SecurityScheme securityScheme) {
        String issuer = ConfigProvider.getConfig().getConfigValue("mega.oauth.issuer").getValue();
        String scopeConfig = ConfigProvider.getConfig().getConfigValue("mega.oauth.scope").getValue();

        if (securityScheme.getType() == SecurityScheme.Type.OAUTH2
                && securityScheme.getFlows() != null
                && securityScheme.getFlows().getClientCredentials() != null) {
            securityScheme.getFlows()
                    .getClientCredentials()
                    .tokenUrl(issuer + TOKEN_ENDPOINT_PATH);
        }

        if (securityScheme.getType() == SecurityScheme.Type.OAUTH2
                && securityScheme.getFlows() != null
                && securityScheme.getFlows().getAuthorizationCode() != null) {
            OAuthFlow authorizationCodeFlow = OASFactory.createOAuthFlow()
                    .authorizationUrl(issuer + AUTHORIZATION_ENDPOINT_PATH)
                    .tokenUrl(issuer + TOKEN_ENDPOINT_PATH)
                    .scopes(scopesFromConfig(scopeConfig));
            OAuthFlows flows = OASFactory.createOAuthFlows().authorizationCode(authorizationCodeFlow);
            securityScheme.flows(flows);
        }

        if (securityScheme.getType() == SecurityScheme.Type.OPENIDCONNECT) {
            return null;
        }

        return OASFilter.super.filterSecurityScheme(securityScheme);
    }

    private Map<String, String> scopesFromConfig(String scopeConfig) {
        Map<String, String> scopes = new LinkedHashMap<>();

        Arrays.stream(scopeConfig.split("\\s+"))
                .map(String::trim)
                .filter(scope -> !scope.isEmpty())
                .forEach(scope -> scopes.put(scope, "Configured OAuth scope: " + scope));

        return scopes;
    }
}
