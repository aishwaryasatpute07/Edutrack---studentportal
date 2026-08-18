package com.edutrack.studentportal.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter){
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder(){

        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        //login
                        .requestMatchers("/users/login").permitAll()

                        //User Management -> ADMIN only
                        .requestMatchers("/users/**").hasRole("ADMIN")

                        //Students:
                        //ADMIN and STUDENT can make GET requests
                        .requestMatchers(HttpMethod.GET,"/students/**")
                        .hasAnyRole("ADMIN","STUDENT")

                        //Post, put, delete -> ADMIN only
                        .requestMatchers("/students/**").hasRole("ADMIN")

                        //Courses -> ADMIN only
                        .requestMatchers("/courses/**").hasRole("ADMIN")

                        //Batches -> ADMIN only
                        .requestMatchers("/batches/**").hasRole("ADMIN")

                        //Attendance:
                        //ADMIN and STUDENT can view attendance
                        .requestMatchers(HttpMethod.GET, "/attendance/**")
                        .hasAnyRole("ADMIN", "STUDENT")

                        //ADMIN can create, update and delete attendance
                        .requestMatchers("/attendance/**")
                        .hasRole("ADMIN")

                        // Payments
                        .requestMatchers(HttpMethod.GET, "/payments/**")
                        .hasAnyRole("ADMIN", "STUDENT")

                        .requestMatchers(HttpMethod.POST, "/payments/create-order")
                        .hasAnyRole("ADMIN", "STUDENT")

                        .requestMatchers(HttpMethod.POST, "/payments/verify")
                        .hasAnyRole("ADMIN", "STUDENT")

                        .requestMatchers("/payments/**")
                        .hasRole("ADMIN")

                        .requestMatchers("/payment.html")
                        .permitAll()

                        //everything else
                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
