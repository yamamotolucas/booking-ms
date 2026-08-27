package br.com.booking_ms.config.security;

import br.com.booking_ms.config.filters.token.TokenFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    public SecurityConfiguration(CustomAccessDeniedHandler customAccessDeniedHandler) {
        this.customAccessDeniedHandler = customAccessDeniedHandler;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, TokenFilter tokenFilter) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler(customAccessDeniedHandler)
                )
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests

                        //SWAGGER
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger.yaml").permitAll()

                        // PUBLIC
                        .requestMatchers(HttpMethod.GET, "/v1/bookings/test-headers").permitAll()

                        // BARBER SERVICES
                        .requestMatchers(HttpMethod.POST, "/v1/barber/services/create").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/v1/barber/services/list").permitAll()

                        // BOOKINGS
                        .requestMatchers(HttpMethod.POST, "/v1/bookings/").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.POST, "/v1/bookings/availability").hasAnyRole("ADMIN", "CUSTOMER", "BARBER")
                        .requestMatchers(HttpMethod.GET, "/v1/bookings/myBookings").hasAnyRole("CUSTOMER", "BARBER")
                        .requestMatchers(HttpMethod.PATCH, "/v1/bookings/cancel/*").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.GET, "/v1/bookings/confirm/*").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/v1/bookings/complete/*").hasRole("BARBER")
                        .requestMatchers(HttpMethod.GET, "/v1/bookings/test-headers").hasRole("ADMIN")

                        // TEST
                        .requestMatchers(HttpMethod.GET, "/test/barber").hasRole("BARBER")
                        .requestMatchers(HttpMethod.GET, "/test/customer").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.GET, "/test/admin").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/test/customerAndBarber").hasAnyRole("CUSTOMER", "BARBER")
                        .requestMatchers(HttpMethod.GET, "/test/permitAll").permitAll()


                        .anyRequest()
                        .authenticated())
                .addFilterBefore(
                        tokenFilter,
                        UsernamePasswordAuthenticationFilter.class
                )
                .build();
    }

    //ADMIN > MANAGER > BARBER > CUSTOMER
    @Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.fromHierarchy(
                "ROLE_ADMIN > ROLE_MANAGER\n" +
                        "ROLE_MANAGER > ROLE_BARBER\n" +
                        "ROLE_BARBER > ROLE_CUSTOMER"
        );
    }
}
