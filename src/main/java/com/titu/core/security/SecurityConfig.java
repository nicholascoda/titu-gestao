package com.titu.core.security;

import com.titu.core.model.Usuario;
import com.titu.core.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
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

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Libera as pastas de imagens, css e js para a tela de login não ficar feia
                        .requestMatchers("/css/**", "/js/**", "/img/**", "/webjars/**").permitAll()
                        // Qualquer outra tela do sistema exige login
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        // Dizemos qual é a nossa rota da tela de login
                        .loginPage("/login")
                        // Se o login der certo, manda pro Dashboard
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        // Quando sair, volta pro login
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }

    // encriptador de senhas (padrão de mercado BCrypt)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // CRIANDO O SEU USUÁRIO MESTRE ---
    // Esse código roda sozinho quando o sistema liga. Se não tiver ninguém no banco, ele te cadastra!
    @Bean
    public CommandLineRunner initData(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (repository.findByEmail("admin@titu.com").isEmpty()) {
                Usuario admin = Usuario.builder()
                        .nome("Nicholas")
                        .email("admin@titu.com")
                        .senha(passwordEncoder.encode("123456")) // salva criptografada
                        .role("ADMIN")
                        .build();
                repository.save(admin);
            }
        };
    }
}