package com.tcc.taskmanaging.config;

import com.tcc.taskmanaging.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .userDetailsService(customUserDetailsService) // Adiciona o userDetailsService
            .authorizeHttpRequests(auth -> auth
                // Permite acesso a recursos estáticos e páginas de autenticação
                .requestMatchers("/auth", "/cadastro", "/login", "/css/**", "/js/**", "/img/**").permitAll()
                // Exige autenticação para qualquer outra requisição
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/auth") // Sua página de login customizada
                .loginProcessingUrl("/login") // O endpoint que o Spring Security monitora
                .defaultSuccessUrl("/", true) // Para onde ir após o login
                .failureUrl("/auth?error=true") // Para onde ir se o login falhar
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout") // Endpoint para deslogar
                .logoutSuccessUrl("/auth?logout") // Para onde ir após deslogar
                .permitAll()
            )
            // ATENÇÃO: Desabilitar o CSRF é um risco de segurança.
            // É aceitável para um TCC, mas em produção, você deve habilitá-lo
            // e incluir o token CSRF nos seus formulários POST.
            .csrf(csrf -> csrf.disable()); 

        return http.build();
    }
}