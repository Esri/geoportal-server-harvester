package com.esri.geoportal.base.security;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
// === OAuth2 Login (federation to ArcGIS) ===
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
// UserInfo service imports
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
// === Authorization Server & Resource Server ===
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.web.client.RestTemplate;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  // Use SecurityProperties component to hold configuration loaded from config.properties
  private final SecurityConfigProperties configProperties;
  @Autowired private SecurityEndPointProp securityEndPointProp;

  public SecurityConfig(SecurityConfigProperties securityProperties) {
    this.configProperties = securityProperties;
  }

  // === AUTHORIZATION SERVER CHAIN
  @Bean
  @Order(1)
  public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
    OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
        OAuth2AuthorizationServerConfigurer.authorizationServer();
    http
      .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
      .with(authorizationServerConfigurer, (authorizationServer) ->
          authorizationServer.oidc(Customizer.withDefaults()) // Enable OpenID Connect
      )
      .authorizeHttpRequests((authorize) -> authorize.anyRequest().authenticated())
      .exceptionHandling((exceptions) -> exceptions.defaultAuthenticationEntryPointFor(
          new LoginUrlAuthenticationEntryPoint("/login.html"),
          new MediaTypeRequestMatcher(MediaType.TEXT_HTML)));
    return http.build();
  }

  // === APPLICATION / RESOURCE SERVER CHAIN ========================================
  @Bean
  @Order(2)
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      JwtAuthenticationFilter jwtAuthenticationFilter,
      ClientRegistrationRepository clientRegistrationRepository,
      OAuth2AuthorizedClientService authorizedClientService,
      OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> arcgisTokenClient,
      // Inject customized DefaultOAuth2UserService
      OAuth2UserService<OAuth2UserRequest, OAuth2User> arcgisUserService
  ) throws Exception {

    http
      .csrf(csrf -> csrf.disable())
      .headers(h -> h.frameOptions(f -> f.sameOrigin()))
      .authorizeHttpRequests(authorize -> {
        // Apply configured public endpoints (permitAll)
        for (String pattern : configProperties.getPublicEndpointsList()) {
          authorize.requestMatchers(pattern).permitAll();
        }
        // Apply secured endpoint rules (from config.properties)
        for (EndpointSecurityConfig rule : securityEndPointProp.getSecuredEndpoints()) {
          String pattern = rule.getPattern();
          String method = rule.getMethod();
          String roles = (rule.getRoles() == null) ? "" : rule.getRoles().trim();
          boolean isPermitAll = "permitAll".equalsIgnoreCase(roles);
          boolean isAuthenticated= "authenticated".equalsIgnoreCase(roles);
          if (method == null || method.trim().isEmpty()) {
            if (isPermitAll) {
              authorize.requestMatchers(pattern).permitAll();
            } else if (isAuthenticated) {
              authorize.requestMatchers(pattern).authenticated();
            } else if (!roles.isEmpty()) {
              authorize.requestMatchers(pattern).hasAnyAuthority(splitCsv(roles));
            }
          } else {
            for (String m : splitCsv(method)) {
              HttpMethod httpMethod;
              try { httpMethod = HttpMethod.valueOf(m.trim().toUpperCase()); }
              catch (IllegalArgumentException ex) { continue; }
              if (isPermitAll) {
                authorize.requestMatchers(httpMethod, pattern).permitAll();
              } else if (isAuthenticated) {
                authorize.requestMatchers(httpMethod, pattern).authenticated();
              } else if (!roles.isEmpty()) {
                authorize.requestMatchers(httpMethod, pattern).hasAnyAuthority(splitCsv(roles));
              }
            }
          }
        }
        // Everything else requires auth (so /harvester triggers login)
        authorize.anyRequest().authenticated();
      })
      .exceptionHandling(ex -> ex.defaultAuthenticationEntryPointFor(
          new LoginUrlAuthenticationEntryPoint("/login.html"),
          new MediaTypeRequestMatcher(MediaType.TEXT_HTML)))
      // Use custom login page (popup page)
      .formLogin(form -> form
          .loginPage("/custom-login.html")
          .loginProcessingUrl("/login")
          .defaultSuccessUrl("/custom-login.html?loggedin", true) // — force=true is critical
          .failureUrl("/custom-login.html?error")
          .permitAll())
      // ArcGIS federation
      .oauth2Login(oauth -> oauth
          .clientRegistrationRepository(clientRegistrationRepository)
          .authorizedClientService(authorizedClientService)
          .userInfoEndpoint(userInfo -> {
            // Use the customized DefaultOAuth2UserService (with CustomRequestEntityConverter)
            userInfo.userService(arcgisUserService);
          })
          .tokenEndpoint(token -> token.accessTokenResponseClient(arcgisTokenClient))
          .successHandler((request, response, authentication) -> {
            logger.info("OAuth2 Login Success Handler - Authentication: {}", authentication.getName());
            logger.info("Authentication Authorities: {}", authentication.getAuthorities());
            // Enhance the authentication with proper roles
            if (authentication instanceof OAuth2AuthenticationToken) {
              OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
              Authentication enhancedAuth = ArcGISAuthenticationConverter.enhanceAuthentication(token);
              logger.info("Enhanced Authentication Authorities: {}", enhancedAuth.getAuthorities());
              // Set the enhanced authentication in the SecurityContext
              org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(enhancedAuth);
            }
            // After successful OAuth2 login, redirect to home
            String context = request.getContextPath();
            response.sendRedirect(context + "/#/home");
          })
          .failureHandler((request, response, ex) -> {
            logger.error("OAuth2 login failed", ex);
            // Redirect to a friendlier error page
            response.sendRedirect(request.getContextPath() + "/custom-login.html?oauth2_error=" + ex.getClass().getSimpleName());
          })
      )
      .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {
        jwt.decoder(jwtDecoder(jwkSource()));
        jwt.jwtAuthenticationConverter(jwtAuthenticationConverter());
      }))
      .httpBasic(Customizer.withDefaults())
      .logout(logout -> logout
          .logoutUrl("/logout") // default is /logout
          .logoutSuccessUrl("/login.html?loggedout") // after server logout (optional)
          .invalidateHttpSession(true)
          .deleteCookies("JSESSIONID")
          .permitAll()
      );

    http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  private String[] splitCsv(String csv) {
    return Arrays.stream(csv.split(","))
        .map(String::trim).filter(s -> !s.isEmpty())
        .toArray(String[]::new);
  }

  //Password encoder utility
  public BCryptPasswordEncoder bcryptPassEncoder() { return new BCryptPasswordEncoder(); }

  // Registered clients
  @Bean
  public InMemoryRegisteredClientRepository registeredClientRepository() {
    TokenSettings tokenSettings = TokenSettings.builder()
        .accessTokenTimeToLive(Duration.ofMinutes(120))
        .build();

    RegisteredClient uiAppClient = RegisteredClient
        .withId(UUID.randomUUID().toString())
        .clientId(configProperties.getUiClientId())
        .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
        .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
        .redirectUri(configProperties.getUiRedirectUri())
        .tokenSettings(tokenSettings)
        .scope("openid").scope("profile").scope("api.read").scope("api.write")
        .clientSettings(ClientSettings.builder()
            .requireAuthorizationConsent(false)
            .build())
        .build();

    RegisteredClient apiClientRW = RegisteredClient
        .withId(UUID.randomUUID().toString())
        .clientId(configProperties.getApiAdminClientId())
        .clientSecret(configProperties.getApiAdminClientSecret())
        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
        .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
        .tokenSettings(tokenSettings)
        .scope("api.read").scope("api.write").scope("write").scope("read")
        .build();

    RegisteredClient apiClientRead = RegisteredClient
        .withId(UUID.randomUUID().toString())
        .clientId(configProperties.getApiReadClientId())
        .clientSecret(configProperties.getApiReadClientSecret())
        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
        .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
        .tokenSettings(tokenSettings)
        .scope("api.read").scope("read")
        .build();

    return new InMemoryRegisteredClientRepository(uiAppClient, apiClientRW, apiClientRead);
  }

  // === ArcGIS ClientRegistration & OAuth2 client beans =============================
  @Bean
  public ClientRegistrationRepository clientRegistrationRepository() {
    ClientRegistration arcgis = ClientRegistration.withRegistrationId("arcgis")
        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST) 
        .clientId(configProperties.getArcgisClientId())
        .clientSecret(configProperties.getArcgisClientSecret())
        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)        
        .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
        .scope("profile") // adjust scopes as needed
        .authorizationUri(configProperties.getArcgisAuthorizationURI())
        .tokenUri(configProperties.getArcgisTokenURI())
        .userInfoUri(configProperties.getArcgisUserInfoURI())
        .userNameAttributeName(configProperties.getArcgisUserNameAttr())
        .clientName("ArcGIS")
        .build();
    return new InMemoryClientRegistrationRepository(arcgis);
  }

  @Bean
  public OAuth2AuthorizedClientService authorizedClientService(ClientRegistrationRepository repo) {
    return new InMemoryOAuth2AuthorizedClientService(repo);
  }

  /**
   * Token client using CustomAccessTokenResponseConverter so additional parameters
   * from ArcGIS token response (e.g., username) are propagated into
   * OAuth2AccessTokenResponse#additionalParameters and then into OAuth2UserRequest.
   */
  @Bean
  public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> arcgisTokenClient() {
    OAuth2AccessTokenResponseHttpMessageConverter tokenConverter =
        new OAuth2AccessTokenResponseHttpMessageConverter();
    //Use custom converter so additional parameters are preserved
    tokenConverter.setAccessTokenResponseConverter(new CustomAccessTokenResponseConverter());

    RestTemplate rest = new RestTemplate(Arrays.asList(
        new FormHttpMessageConverter(), tokenConverter));
    rest.setErrorHandler(new OAuth2ErrorResponseErrorHandler());

    DefaultAuthorizationCodeTokenResponseClient client = new DefaultAuthorizationCodeTokenResponseClient();
    client.setRestOperations(rest);
    return client;
  }

  // ====== Custom RequestEntity converter & DefaultOAuth2UserService for UserInfo ======

  @Bean
  public CustomRequestEntityConverter customRequestEntityConverter() {
    // Appends "/{username}?f=json" to userInfoUri and sets headers appropriately.
    return new CustomRequestEntityConverter();
  }

  @Bean
  public OAuth2UserService<OAuth2UserRequest, OAuth2User> arcgisUserService(
      CustomRequestEntityConverter converter
  ) {
    DefaultOAuth2UserService svc = new DefaultOAuth2UserService();
    // Plug in the custom RequestEntity converter for the UserInfo call
    svc.setRequestEntityConverter(converter);
    return svc;
  }

  // JWK / JWT for AS & RS
  @Bean
  public JWKSource<SecurityContext> jwkSource() {
    KeyPair keyPair = generateRsaKey();
    RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
    RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
    RSAKey rsaKey = new RSAKey.Builder(publicKey).privateKey(privateKey)
        .keyID(UUID.randomUUID().toString()).build();
    JWKSet jwkSet = new JWKSet(rsaKey);
    return new ImmutableJWKSet<>(jwkSet);
  }

  private static KeyPair generateRsaKey() {
    try {
      KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
      kpg.initialize(2048);
      return kpg.generateKeyPair();
    } catch (Exception ex) {
      throw new IllegalStateException(ex);
    }
  }

  @Bean public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
    return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
  }
  @Bean public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
    return new NimbusJwtEncoder(jwkSource);
  }

  @Bean
  public JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtGrantedAuthoritiesConverter defaultConverter = new JwtGrantedAuthoritiesConverter();
    JwtGrantedAuthoritiesConverter customConverter = new JwtGrantedAuthoritiesConverter();
    customConverter.setAuthoritiesClaimName("authorities");
    customConverter.setAuthorityPrefix("");
    Converter<org.springframework.security.oauth2.jwt.Jwt, Collection<GrantedAuthority>> combined = jwt -> {
      Collection<GrantedAuthority> a1 = defaultConverter.convert(jwt);
      Collection<GrantedAuthority> a2 = customConverter.convert(jwt);
      Set<GrantedAuthority> merged = new HashSet<>();
      if (a1 != null) merged.addAll(a1);
      if (a2 != null) merged.addAll(a2);
      return new ArrayList<>(merged);
    };
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(combined);
    return converter;
  }

  @Bean
  public AuthorizationServerSettings authorizationServerSettings() {
    return AuthorizationServerSettings.builder()
        .issuer(configProperties.getIssuer())
        .build();
  }

  @Bean
  public org.springframework.web.servlet.handler.HandlerMappingIntrospector mvcHandlerMappingIntrospector() {
    return new org.springframework.web.servlet.handler.HandlerMappingIntrospector();
  }

  @Bean
  public JwtAuthenticationFilter jwtAuthenticationFilter(JwtDecoder jwtDecoder) {
    return new JwtAuthenticationFilter(jwtDecoder, configProperties);
  }

  /**
   * Custom OAuth2 user service for ArcGIS Portal authentication.
   * Kept for compatibility; not wired into oauth2Login when arcgisUserService is used.
   */
  @Bean
  public ArcGISCustomOAuth2UserService arcgisOAuth2UserService() {
    return new ArcGISCustomOAuth2UserService();
  }

  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
}