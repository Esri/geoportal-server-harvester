package com.esri.geoportal.harvester.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.esri.geoportal.base.security.SecurityConfigProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Security REST controller providing OAuth2 configuration to the UI.
 * This controller is auto-scanned and provides the ArcGIS OAuth2 configuration
 * needed by the login page to initiate the OAuth2 flow with ArcGIS Portal.
 */
@RestController
@RequestMapping("/rest/harvester/security")
public class SecurityController {

    @Autowired
    private SecurityConfigProperties securityConfigProperties;

    /**
     * Get ArcGIS OAuth2 configuration for the UI.
     * This endpoint is public (permitAll) and provides the ArcGIS client configuration
     * needed to initiate the OAuth2 authorization request.
     *
     * @return ArcGIS OAuth configuration including clientId, authorizationUri, etc.
     */
    @GetMapping("/arcgis-config")
    public ResponseEntity<?> getArcGISConfig() {
        try {
            String clientId = securityConfigProperties.getArcgisClientId();
            String authUri = securityConfigProperties.getArcgisAuthorizationURI();

            // Check if ArcGIS is configured
            if (clientId == null || clientId.isEmpty() || 
                authUri == null || authUri.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "ArcGIS OAuth2 is not configured. Please set HRV_ARCGIS_CLIENTID and HRV_ARCGIS_AUTHORIZATIONURI environment variables."));
            }

            Map<String, Object> config = new HashMap<>();
            config.put("clientId", clientId);
            config.put("authorizationUri", authUri);
            config.put("tokenUri", securityConfigProperties.getArcgisTokenURI());
            config.put("userInfoUri", securityConfigProperties.getArcgisUserInfoURI());
            config.put("userNameAttr", securityConfigProperties.getArcgisUserNameAttr());

            return ResponseEntity.ok(config);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to retrieve ArcGIS configuration: " + e.getMessage()));
        }
    }
}