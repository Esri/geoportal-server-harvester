package com.esri.geoportal.base.security;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Configuration holder for config/hrv.properties.
 * Values are injected from property keys (PropertyPlaceholderConfigurer) in config/hrv.properties where env var fallbacks
 * and defaults are defined.
 */
@Component
public class SecurityConfigProperties {

    @Value("${security.issuer}")
    private String issuer;

    @Value("${security.ui.clientId}")
    private String uiClientId;

    @Value("${security.ui.redirectUri}")
    private String uiRedirectUri;

    @Value("${security.api.admin.clientId}")
    private String apiAdminClientId;

    @Value("${security.api.admin.clientSecret}")
    private String apiAdminClientSecret;

    @Value("${security.api.read.clientId}")
    private String apiReadClientId;

    @Value("${security.api.read.clientSecret}")
    private String apiReadClientSecret;

    @Value("${security.public-endpoints:}")
    private String publicEndpoints;

    @Value("${security.arcgis.auth.enabled:false}")
    private boolean arcgisAuthEnabled;

	@Value("${security.arcgis.clientId:}")
    private String arcgisClientId;
	
	@Value("${security.arcgis.clientSecret:}")
    private String arcgisClientSecret;
    
    @Value("${security.arcgis.authorizationURI:}")
    private String arcgisAuthorizationURI;
    
    @Value("${security.arcgis.tokenURI:}")
    private String arcgisTokenURI;
    
    @Value("${security.arcgis.userInfoURI:}")
    private String arcgisUserInfoURI;
    
    @Value("${security.arcgis.userNameAttr:}")
    private String arcgisUserNameAttr;
    

    @jakarta.annotation.PostConstruct
    private void validate() {
        StringBuilder missing = new StringBuilder();
        if (issuer == null || issuer.isEmpty()) missing.append("security.issuer, ");
        if (uiClientId == null || uiClientId.isEmpty()) missing.append("security.ui.clientId, ");
        if (uiRedirectUri == null || uiRedirectUri.isEmpty()) missing.append("security.ui.redirectUri, ");
        if (apiAdminClientId == null || apiAdminClientId.isEmpty()) missing.append("security.api.admin.clientId, ");
        if (apiAdminClientSecret == null || apiAdminClientSecret.isEmpty()) missing.append("security.api.admin.clientSecret, ");
        if (apiReadClientId == null || apiReadClientId.isEmpty()) missing.append("security.api.read.clientId, ");
        if (apiReadClientSecret == null || apiReadClientSecret.isEmpty()) missing.append("security.api.read.clientSecret, ");

        if (arcgisAuthEnabled) {
            if (arcgisClientId == null || arcgisClientId.isEmpty()) missing.append("security.arcgis.clientId, ");
            if (arcgisClientSecret == null || arcgisClientSecret.isEmpty()) missing.append("security.arcgis.clientSecret, ");
            if (arcgisAuthorizationURI == null || arcgisAuthorizationURI.isEmpty()) missing.append("security.arcgis.authorizationURI, ");
            if (arcgisTokenURI == null || arcgisTokenURI.isEmpty()) missing.append("security.arcgis.tokenURI, ");
            if (arcgisUserInfoURI == null || arcgisUserInfoURI.isEmpty()) missing.append("security.arcgis.userInfoURI, ");
            if (arcgisUserNameAttr == null || arcgisUserNameAttr.isEmpty()) missing.append("security.arcgis.userNameAttr, ");
        }

        if (missing.length() > 0) {
            // remove trailing comma+space
            String list = missing.substring(0, Math.max(0, missing.length() - 2));
            throw new IllegalStateException("Missing required security properties: " + list + ". Provide them via environment variables or in config/hrv.properties.");
        }
    }

    public String getIssuer() {
        return issuer;
    }

    public String getUiClientId() {
        return uiClientId;
    }

    public String getUiRedirectUri() {
        return uiRedirectUri;
    }

    public String getApiAdminClientId() {
        return apiAdminClientId;
    }

    public String getApiAdminClientSecret() {
        return apiAdminClientSecret;
    }

    public String getApiReadClientId() {
        return apiReadClientId;
    }

    public String getApiReadClientSecret() {
        return apiReadClientSecret;
    }

    public boolean isArcgisAuthEnabled() {
        return arcgisAuthEnabled;
    }
    
    public String getArcgisAuthorizationURI() {
		return arcgisAuthorizationURI;
	}

	public void setArcgisAuthorizationURI(String arcgisAuthorizationURI) {
		this.arcgisAuthorizationURI = arcgisAuthorizationURI;
	}

	public String getArcgisClientId() {
		return arcgisClientId;
	}
	public String getArcgisTokenURI() {
		return arcgisTokenURI;
	}

	public String getArcgisUserInfoURI() {
		return arcgisUserInfoURI;
	}

	public String getArcgisUserNameAttr() {
		return arcgisUserNameAttr;
	}

    public List<String> getPublicEndpointsList() {
        List<String> publicEndpointsList = splitAndTrim(publicEndpoints);
        return publicEndpointsList;       
    }
    
    private List<String> splitAndTrim(String csv) {
        if (csv == null || csv.trim().isEmpty()) return List.of();
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

	public String getArcgisClientSecret() {		
		return arcgisClientSecret != null && !arcgisClientSecret.isEmpty() ? arcgisClientSecret : null;
	}
    
    
}
