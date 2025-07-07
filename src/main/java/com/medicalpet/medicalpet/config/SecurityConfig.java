package com.medicalpet.medicalpet.config;

import com.medicalpet.medicalpet.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Configuración de seguridad para la aplicación MedicalPet.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
  private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
  private final UsuarioRepository usuarioRepository;

  /**
     * Constructor que inyecta el repositorio de usuarios.
     *
     * @param usuarioRepository Repositorio de usuarios
     */
  @Autowired
    public SecurityConfig(UsuarioRepository usuarioRepository) {
    this.usuarioRepository = usuarioRepository;
  }

  /**
     * Proporciona el servicio de detalles de usuario.
     *
     * @return Servicio de detalles de usuario
     */
  @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/login", "/registro", "/css/**", "/js/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/facturas/**", "/ventas/**", "/productos/**", 
                "/categorias/**", "/reportes/**").hasAnyRole("ADMIN", "EMPLEADO")
                .requestMatchers("/dashboard").authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
                .successHandler((request, response, authentication) -> {
                  logger.info("Autenticación exitosa para el usuario: {}",
                      authentication.getName());
                  response.sendRedirect("/dashboard");
                })
                .failureHandler((request, response, exception) -> {
                  logger.error("Fallo de autenticación: {}", exception.getMessage());
                  response.sendRedirect("/login?error=true");
                })
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .userInfoEndpoint(userInfo -> userInfo.userService(oauth2UserService()))
                .successHandler((request, response, authentication) -> {
                  logger.info("Autenticación OAuth2 exitosa para el usuario: {}", 
                        authentication.getName());
                  response.sendRedirect("/dashboard");
                })
                .failureHandler((request, response, exception) -> {
                  logger.error("Fallo de autenticación OAuth2: {}", exception.getMessage());
                  response.sendRedirect("/login?error=true");
                })
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
                .logoutSuccessHandler((request, response, authentication) -> {
                  logger.info("Cierre de sesión exitoso para el usuario: {}", 
                      authentication != null ? authentication.getName() : "desconocido");
                  response.sendRedirect("/login?logout=true");
                })
            )
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/access-denied")
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)
                .expiredUrl("/login?expired=true")
                
            )
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin())
                .cacheControl(cacheControl -> cacheControl.disable())
        );

    return http.build();
  }

  /**
     * Proporciona el servicio de detalles de usuario.
     *
     * @return Servicio de detalles de usuario
     */

  @Bean
    public UserDetailsService userDetailsService() {
    return username -> {
      logger.info("Buscando usuario: {}", username);
      com.medicalpet.medicalpet.model.Usuario usuario = usuarioRepository
              .findByUsername(username);
      if (usuario == null) {
        logger.error("Usuario no encontrado: {}", username);
        throw new UsernameNotFoundException("Usuario no encontrado: " + username);
      }
      logger.info("Usuario encontrado: {}, Contraseña: {}, Rol: {}", 
                     usuario.getUsername(), usuario.getPassword(), usuario.getRol());
      return User.withUsername(usuario.getUsername())
                      .password(usuario.getPassword())
                      .roles(usuario.getRol().replace("ROLE_", ""))
                      .build();
    };
  }
  /**
     * Proporciona el codificador de contraseñas BCrypt.
     *
     * @return Codificador de contraseñas
     */

  @Bean
    public BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
  /**
     * Proporciona el proveedor de autenticación DAO.
     *
     * @return Proveedor de autenticación
     */

  @Bean
    public DaoAuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService());
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
  }

  /**
     * Proporciona el servicio de usuario OAuth2.
     *
     * @return Servicio de usuario OAuth2
     */
  @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService() {
    DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
    return userRequest -> {
      OAuth2User oauth2User = delegate.loadUser(userRequest);
      logger.info("Usuario OAuth2 cargado: {}", oauth2User.getName());
            
      // Obtener el email del usuario de Google
      String email = oauth2User.getAttribute("email");
      if (email == null) {
        throw new RuntimeException("No se pudo obtener el email del usuario de Google");
      }
      logger.info("Email del usuario de Google: {}", email);
      // Buscar o crear un usuario en la base de datos
      com.medicalpet.medicalpet.model.Usuario usuario = usuarioRepository.findByUsername(email);
      if (usuario == null) {
        logger.info("Usuario no encontrado, creando uno nuevo para: {}", email);
        usuario = new com.medicalpet.medicalpet.model.Usuario();
        usuario.setUsername(email);
        usuario.setPassword(""); // No se necesita contraseña para OAuth2
        usuario.setRol("ROLE_EMPLEADO"); // Asignar un rol por defecto
        usuarioRepository.save(usuario);
      }

      return oauth2User;
    };
  }
}