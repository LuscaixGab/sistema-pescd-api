package br.ufscar.dc.dsw.pescd.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.Customizer;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final RoleBasedSuccessHandler roleBasedSuccessHandler;
    private final FriendlyAccessDeniedHandler friendlyAccessDeniedHandler;

    public SecurityConfig(RoleBasedSuccessHandler roleBasedSuccessHandler,
                          FriendlyAccessDeniedHandler friendlyAccessDeniedHandler) {
        this.roleBasedSuccessHandler = roleBasedSuccessHandler;
        this.friendlyAccessDeniedHandler = friendlyAccessDeniedHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public org.springframework.security.authentication.AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/api/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.GET, "/api/ofertas-publicas").permitAll()
                        .requestMatchers("/api/aluno/documentacao/**").permitAll() // TODO: remover permissão de teste AL.03
                        .requestMatchers("/api/aluno/relatorio/**").hasRole("ALUNO")
                        .requestMatchers("/api/professor-supervisor/**").hasRole("PROFESSOR")
                                       
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .requestMatchers("/api/v1/usuarios", "/api/v1/usuarios/**").hasRole("ADMINISTRADOR")
                        .requestMatchers("/api/v1/plano-trabalho", "/api/v1/plano-trabalho/**").hasRole("ALUNO")
                        .requestMatchers("/api/v1/ofertas", "/api/v1/ofertas/**").hasRole("SECRETARIO")
                        .requestMatchers("/api/v1/professor-responsavel/documentacoes", "/api/v1/professor-responsavel/documentacoes/**").hasRole("PROFESSOR")
                        .anyRequest().denyAll())
                .httpBasic(Customizer.withDefaults())
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new ApiAuthenticationEntryPoint(new com.fasterxml.jackson.databind.ObjectMapper()))
                        .accessDeniedHandler(new ApiAccessDeniedHandler(new com.fasterxml.jackson.databind.ObjectMapper())));

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize
                // ADICIONADO O "/error" AQUI NA LINHA ABAIXO:
                .requestMatchers("/", "/login", "/error", "/ofertas-publicas", "/erro/**", "/css/**", "/js/**", "/images/**", "/webjars/**", "/api/ofertas-publicas").permitAll()
                .requestMatchers("/administrador", "/administrador/**").hasRole("ADMINISTRADOR")

                // PR.04: Libera as rotas de acompanhamento para ambos (Secretário e Professor)
                .requestMatchers(HttpMethod.GET, "/ofertas").hasAnyRole("SECRETARIO", "PROFESSOR")
                .requestMatchers(HttpMethod.GET, "/ofertas/*/acompanhamento").hasAnyRole("SECRETARIO", "PROFESSOR")
                .requestMatchers(HttpMethod.GET, "/ofertas/*/alunos/*/detalhes").hasAnyRole("SECRETARIO", "PROFESSOR")

                .requestMatchers("/ofertas/**").hasRole("SECRETARIO")
                
                .anyRequest().authenticated())
            .formLogin(form -> form
                    .loginPage("/login")
                    .loginProcessingUrl("/login")
                    .usernameParameter("nomeUsuario")
                    .passwordParameter("senha")
                    .successHandler(roleBasedSuccessHandler)
                    .failureUrl("/login?erro")
                    .permitAll())
            .logout(logout -> logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/login?logout")
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID")
                    .permitAll())
            .exceptionHandling(exception -> exception
                    .accessDeniedHandler(friendlyAccessDeniedHandler));

        return http.build();
    }
}
