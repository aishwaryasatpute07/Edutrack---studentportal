package com.edutrack.studentportal.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter){
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/admin.html").permitAll()
                    .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/users/login").permitAll()

                        .requestMatchers("/users/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/students/search", "/students/sort/**", "/students/pagination")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/students/*")
                        .hasAnyRole("ADMIN", "STUDENT")

                        .requestMatchers("/students/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/courses/hierarchy", "/courses/*/audit")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/courses/**")
                        .hasAnyRole("ADMIN", "STUDENT", "INSTRUCTOR")

                        .requestMatchers("/courses/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/enrollments/student/*")
                        .hasAnyRole("ADMIN", "STUDENT")

                        .requestMatchers(HttpMethod.GET, "/enrollments/course/*")
                        .hasRole("ADMIN")

                        .requestMatchers("/enrollments/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/reports/performance/student/*")
                        .hasAnyRole("ADMIN", "STUDENT", "INSTRUCTOR")

                        .requestMatchers("/reports/**").hasRole("ADMIN")

                        .requestMatchers("/batches/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/attendance/student/*", "/attendance/student/*/report")
                        .hasAnyRole("ADMIN", "STUDENT", "INSTRUCTOR")

                        .requestMatchers(HttpMethod.GET, "/attendance/student/*/report/pdf")
                        .hasAnyRole("ADMIN", "STUDENT", "INSTRUCTOR")

                        .requestMatchers(HttpMethod.GET, "/attendance/**")
                        .hasRole("ADMIN")

                        .requestMatchers("/attendance/**")
                        .hasAnyRole("ADMIN", "INSTRUCTOR")

                        .requestMatchers(HttpMethod.GET, "/payments").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/payments/students/*/summary", "/payments/students/*/history",
                                "/payments/*/receipt", "/payments/*/receipt/pdf")
                        .hasAnyRole("ADMIN", "STUDENT")

                        .requestMatchers(HttpMethod.POST, "/payments/webhook").permitAll()

                        .requestMatchers(HttpMethod.POST, "/payments/create-order", "/payments/verify")
                        .hasRole("STUDENT")

                        .requestMatchers("/payments/**").hasRole("ADMIN")

                        .requestMatchers("/academic-records/student/*", "/academic-records/student/*/marksheet")
                        .hasAnyRole("ADMIN", "STUDENT", "INSTRUCTOR")

                        .requestMatchers("/academic-records/student/*/marksheet/pdf")
                        .hasAnyRole("ADMIN", "STUDENT", "INSTRUCTOR")

                        .requestMatchers(HttpMethod.GET, "/academic-records").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/academic-records").hasAnyRole("ADMIN", "INSTRUCTOR")
                        .requestMatchers(HttpMethod.PUT, "/academic-records/*").hasAnyRole("ADMIN", "INSTRUCTOR")
                        .requestMatchers(HttpMethod.DELETE, "/academic-records/*").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/hall-tickets/student/*", "/hall-tickets/*/pdf")
                        .hasAnyRole("ADMIN", "STUDENT")
                        .requestMatchers(HttpMethod.GET, "/hall-tickets").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/hall-tickets").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/hall-tickets/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/hall-tickets/*").hasRole("ADMIN")

                        .requestMatchers("/instructors/me/students").hasRole("INSTRUCTOR")

                        .requestMatchers("/payment.html")
                        .permitAll()

                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
