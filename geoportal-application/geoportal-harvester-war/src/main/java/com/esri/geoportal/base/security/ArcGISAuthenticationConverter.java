package com.esri.geoportal.base.security;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Custom Authentication Converter for ArcGIS OAuth2 that intercepts the authentication
 * and adds proper Spring Security roles based on ArcGIS user info.
 */
public class ArcGISAuthenticationConverter implements AuthenticationConverter {
    
    private static final Logger logger = LoggerFactory.getLogger(ArcGISAuthenticationConverter.class);

    @Override
    public Authentication convert(HttpServletRequest request) {
        // This converter is called during the OAuth2 login process
        // We don't need to do anything here as Spring handles the conversion
        // This is just a placeholder for reference
        return null;
    }

    /**
     * Called after OAuth2 login to enhance the authentication with proper authorities
     */
    public static Authentication enhanceAuthentication(OAuth2AuthenticationToken token) {
        logger.info("Enhancing OAuth2 authentication for ArcGIS user");
        
        try {
            OAuth2User oauth2User = token.getPrincipal();
            Map<String, Object> attributes = oauth2User.getAttributes();
            
            logger.info("ArcGIS OAuth2 attributes: {}", attributes.keySet());
            
            // Extract username
            String username = (String) attributes.get("username");
            if (username == null) {
                username = (String) attributes.get("email");
            }
            
            logger.info("Processing ArcGIS user: {}", username);
            
            // Create authorities
            Set<GrantedAuthority> authorities = new HashSet<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_PUBLISHER"));
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            
            // Check for admin role in ArcGIS
            Object role = attributes.get("role");
            if (role != null && "admin".equalsIgnoreCase(role.toString())) {
                logger.info("User {} has admin role in ArcGIS", username);
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            }
            
            logger.info("User {} authenticated with authorities: {}", username, authorities);
            
            // Create enhanced OAuth2User
            ArcGISOAuth2User enhancedUser = new ArcGISOAuth2User(oauth2User, authorities);
            
            // Return new authentication token with enhanced user and authorities
            return new OAuth2AuthenticationToken(
                enhancedUser,
                enhancedUser.getAuthorities(),
                token.getAuthorizedClientRegistrationId()
            );
            
        } catch (Exception e) {
            logger.error("Error enhancing ArcGIS authentication: {}", e.getMessage(), e);
            return token;
        }
    }

    /**
     * Custom OAuth2User that wraps the ArcGIS user with proper Spring Security authorities
     */
    public static class ArcGISOAuth2User implements OAuth2User {
        private final OAuth2User delegate;
        private final Set<GrantedAuthority> authorities;

        public ArcGISOAuth2User(OAuth2User delegate, Set<GrantedAuthority> authorities) {
            this.delegate = delegate;
            this.authorities = authorities;
        }

        @Override
        public Map<String, Object> getAttributes() {
            return delegate.getAttributes();
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return authorities;
        }

        @Override
        public String getName() {
            return delegate.getName();
        }
    }
}
