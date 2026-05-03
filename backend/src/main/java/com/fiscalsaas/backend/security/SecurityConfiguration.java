package com.fiscalsaas.backend.security;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfiguration {

	@Bean
	SecurityFilterChain apiSecurity(
			HttpSecurity http,
			HeaderAuthenticationFilter headerAuthenticationFilter,
			JwtCurrentUserAuthenticationConverter jwtAuthenticationConverter,
			@Value("${app.security.auth-mode:demo}") String authMode) throws Exception {
		AuthenticationMode mode = AuthenticationMode.from(authMode);
		http
				.csrf(AbstractHttpConfigurer::disable)
				.cors(Customizer.withDefaults())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(exceptionHandling -> exceptionHandling.authenticationEntryPoint(unauthorizedEntryPoint()))
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers(HttpMethod.OPTIONS, "/api/**")
						.permitAll()
						.requestMatchers(HttpMethod.GET, "/api/health", "/actuator/health", "/actuator/health/**", "/actuator/prometheus")
						.permitAll()
						.anyRequest()
						.authenticated());

		if (mode == AuthenticationMode.OIDC) {
			http.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt
					.jwtAuthenticationConverter(jwtAuthenticationConverter)));
		}
		else {
			http.addFilterBefore(headerAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
		}

		return http.build();
	}

	@Bean
	AuthenticationEntryPoint unauthorizedEntryPoint() {
		return (request, response, authException) -> response.sendError(HttpStatus.UNAUTHORIZED.value());
	}

	@Bean
	UserDetailsService noDefaultPasswordUserDetailsService() {
		return username -> {
			throw new UsernameNotFoundException("Password-based login is disabled for this phase.");
		};
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource(
			@Value("${app.cors.allowed-origins}") String allowedOrigins) {
		CorsConfiguration configuration = new CorsConfiguration();
		List<String> origins = Arrays.stream(allowedOrigins.split(","))
				.map(String::trim)
				.filter(origin -> !origin.isBlank())
				.toList();
		configuration.setAllowedOrigins(origins);
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Tenant-Id", "X-User-Email"));
		configuration.setExposedHeaders(List.of("Location"));
		configuration.setAllowCredentials(false);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/api/**", configuration);
		return source;
	}
}
