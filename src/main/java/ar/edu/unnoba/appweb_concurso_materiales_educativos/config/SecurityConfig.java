package ar.edu.unnoba.appweb_concurso_materiales_educativos.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsService userService;

    @Autowired
    public SecurityConfig(UserDetailsService userService) {
        this.userService = userService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .userDetailsService(userService)
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers(
                                "/",
                                "/error",
                                "/webjars/**",
                                "/resources/**",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/uploads/**",
                                "/login",
                                "/materiales-participantes",
                                "/materiales-participantes/**",
                                "/materiales-ganadores",
                                "/materiales-ganadores/**",
                                "/participar",
                                "/download/**",
                                "/view/**"
                        ).permitAll()
                        .requestMatchers("/concursante/**").hasRole("CONCURSANTE")
                        .requestMatchers("/administrador/**").hasRole("ADMINISTRADOR")
                        .requestMatchers("/evaluador/**").hasRole("EVALUADOR")
                        .anyRequest().authenticated()
                )
                .formLogin((form) -> form
                        .loginPage("/login").permitAll()
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/default", true)
                        .failureHandler((request, response, exception) -> {
                            String error = "error";
                            if (exception instanceof DisabledException || exception instanceof InternalAuthenticationServiceException) {
                                error = "disabled";
                            } else if (exception instanceof BadCredentialsException) {
                                error = "bad_credentials";
                            }
                            response.sendRedirect("/login?error=" + error);
                        }))
                .logout((logout) -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll())
                .build();
    }
}
