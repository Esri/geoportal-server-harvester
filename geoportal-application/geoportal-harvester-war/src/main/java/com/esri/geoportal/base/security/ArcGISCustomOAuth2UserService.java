package com.esri.geoportal.base.security;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom OAuth2 user service for ArcGIS Portal authentication.
 * 
 * This service:
 * 1. Fetches user information from ArcGIS Portal
 * 2. Creates a Spring Security OAuth2User with proper authorities
 * 3. Automatically assigns ROLE_PUBLISHER to all authenticated users from ArcGIS
 * 
 * ArcGIS Portal returns user info in format:
 * {
 *   "username": "user@domain.com",
 *   "email": "user@domain.com",
 *   "fullName": "John Doe",
 *   "firstName": "John",
 *   "lastName": "Doe",
 *   ... other fields
 * }
 */
public class ArcGISCustomOAuth2UserService extends DefaultOAuth2UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(ArcGISCustomOAuth2UserService.class);

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            logger.info("Loading OAuth2 user from ArcGIS Portal");
            
            // Call parent to fetch user info from ArcGIS
            OAuth2User oauth2User = super.loadUser(userRequest);
            
            // Get user attributes
            Map<String, Object> attributes = oauth2User.getAttributes();
            logger.info("ArcGIS user attributes received: {}", attributes.keySet());
            
            // Get username (primary identifier)
            String username = (String) attributes.get("username");
            if (username == null) {
                username = (String) attributes.get("email");
            }
            
            logger.info("Authenticated ArcGIS user: {}", username);
            
            // Create authorities set with ROLE_PUBLISHER for all ArcGIS authenticated users
            Set<GrantedAuthority> authorities = new HashSet<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_PUBLISHER"));
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            
            // Check if user is admin in ArcGIS (optional - if your portal has admin flag)
            Object role = attributes.get("role");
            if (role != null && "admin".equalsIgnoreCase(role.toString())) {
                logger.info("User {} has admin role in ArcGIS Portal", username);
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            }
            
            logger.info("User {} authenticated with authorities: {}", username, authorities);
            
            // Return new OAuth2User with our custom authorities
            return new ArcGISOAuth2User(oauth2User, authorities);
            
        } catch (OAuth2AuthenticationException e) {
            logger.error("OAuth2 authentication failed: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Error loading ArcGIS user: {}", e.getMessage(), e);
            throw new OAuth2AuthenticationException("Failed to load ArcGIS user: " + e.getMessage());
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