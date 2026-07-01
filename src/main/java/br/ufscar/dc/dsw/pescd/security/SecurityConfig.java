package br.ufscar.dc.dsw.pescd.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

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
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        
        // Ao invés de desligar tudo, ignoramos o CSRF apenas na rota que você vai testar no Postman
        http.csrf(csrf -> csrf.ignoringRequestMatchers("/api/aluno/documentacao/**", "/api/aluno/relatorio/**")); // TODO: remover essa linha, usada apenas para liberar o postman (AL.03 e AL.04)
        
        // Inicia a configuração de rotas em uma nova instrução
        http.authorizeHttpRequests(authorize -> authorize
                // ADICIONADO O "/error" AQUI NA LINHA ABAIXO:
                .requestMatchers("/", "/login", "/error", "/ofertas-publicas", "/erro/**", "/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                .requestMatchers("/administrador", "/administrador/**").hasRole("ADMINISTRADOR")

                // PR.04: Libera as rotas de acompanhamento para ambos (Secretário e Professor)
                .requestMatchers(HttpMethod.GET, "/ofertas").hasAnyRole("SECRETARIO", "PROFESSOR")
                .requestMatchers(HttpMethod.GET, "/ofertas/*/acompanhamento").hasAnyRole("SECRETARIO", "PROFESSOR")
                .requestMatchers(HttpMethod.GET, "/ofertas/*/alunos/*/detalhes").hasAnyRole("SECRETARIO", "PROFESSOR")

                .requestMatchers("/ofertas/**").hasRole("SECRETARIO")
                
                // AL.03: Permissão para testes
                .requestMatchers("/api/aluno/documentacao/**", "/api/aluno/relatorio/**").permitAll() // TODO: essa permissão é apenas para testes da AL.03 (remover depois)

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
