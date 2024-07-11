/*
 * Copyright 2024 cont_anki.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esri.geoportal.base.security;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.util.StringUtils;

/**
 *
 * @author cont_anki
 */
public class CustomAccessTokenResponseConverter implements Converter<Map<String, String>, OAuth2AccessTokenResponse> {
	private static final Set<String> TOKEN_RESPONSE_PARAMETER_NAMES = Stream.of(
			OAuth2ParameterNames.ACCESS_TOKEN,
			OAuth2ParameterNames.TOKEN_TYPE,
			OAuth2ParameterNames.EXPIRES_IN,
			OAuth2ParameterNames.REFRESH_TOKEN,
			OAuth2ParameterNames.SCOPE).collect(Collectors.toSet());

	@Override
	public OAuth2AccessTokenResponse convert(Map<String, String> tokenResponseParameters) {
		String accessToken = tokenResponseParameters.get(OAuth2ParameterNames.ACCESS_TOKEN);

		OAuth2AccessToken.TokenType accessTokenType = OAuth2AccessToken.TokenType.BEARER;

		long expiresIn = 0;
		if (tokenResponseParameters.containsKey(OAuth2ParameterNames.EXPIRES_IN)) {
			try {
				expiresIn = Long.valueOf(tokenResponseParameters.get(OAuth2ParameterNames.EXPIRES_IN));
			} catch (NumberFormatException ex) { }
		}

		Set<String> scopes = Collections.emptySet();
		if (tokenResponseParameters.containsKey(OAuth2ParameterNames.SCOPE)) {
			String scope = tokenResponseParameters.get(OAuth2ParameterNames.SCOPE);
			scopes = Arrays.stream(StringUtils.delimitedListToStringArray(scope, " ")).collect(Collectors.toSet());
		}

		Map<String, Object> additionalParameters = new LinkedHashMap<>();
		tokenResponseParameters.entrySet().stream()
				.filter(e -> !TOKEN_RESPONSE_PARAMETER_NAMES.contains(e.getKey()))
				.forEach(e -> additionalParameters.put(e.getKey(), e.getValue()));

		return OAuth2AccessTokenResponse.withToken(accessToken)
				.tokenType(accessTokenType)
				.expiresIn(expiresIn)
				.scopes(scopes)
				.additionalParameters(additionalParameters)
				.build();
	}
}