package vitalsanity.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.authentication.preauth.x509.X509AuthenticationFilter;
import org.springframework.security.web.authentication.preauth.x509.SubjectDnX509PrincipalExtractor;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsByNameServiceWrapper;

@Configuration
public class SecurityConfig {

    @Autowired
    private CertificateAuthenticationSuccessHandler successHandler;

    @Autowired
    private CertificateUserDetailsService certificateUserDetailsService;

    @Bean
    @Order(1)
    public SecurityFilterChain certificateFilterChain(HttpSecurity http) throws Exception {
        SubjectDnX509PrincipalExtractor principalExtractor = new SubjectDnX509PrincipalExtractor();
        principalExtractor.setSubjectDnRegex(".*-\\s*(?:NIF:)?([0-9]{8}[A-Z])");  // Método corregido

        X509AuthenticationFilter x509Filter = new X509AuthenticationFilter();
        x509Filter.setPrincipalExtractor(principalExtractor);
        x509Filter.setAuthenticationSuccessHandler(successHandler);
        x509Filter.setContinueFilterChainOnUnsuccessfulAuthentication(true);

        PreAuthenticatedAuthenticationProvider authProvider = new PreAuthenticatedAuthenticationProvider();
        authProvider.setPreAuthenticatedUserDetailsService(
                new UserDetailsByNameServiceWrapper<>(certificateUserDetailsService)
        );

        AuthenticationManager authManager = new ProviderManager(authProvider);
        x509Filter.setAuthenticationManager(authManager);

        http
                .securityMatcher(new AntPathRequestMatcher("/login/certificate"))
                .addFilter(x509Filter)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain defaultFilterChain(HttpSecurity http) throws Exception {
        http
                /*
                Solo para los endpoints que manejan AutoFirma (y su versión para móviles) desactivamos la protección CSRF.
                Esto lo hacemos debido a que, al estar trabajando con un certificado SSL autogenerado para HTTPS, el navegador por defecto
                rechazará el esquema personalizado afirma:// debido a la protección CSRF. Este problema no estará presente en producción ya que
                en producción estaríamos trabajando con un certificado SSL sde confianza asociado con un dominio específico, y gracias a esto, como
                bien se menciona el Manual Integrador de AutoFirma, en dicho caso los navegadores no rechazarían el esquema personalizado afirma://
                y todo funcionaría correctamente sin necesidad de desactivar la protección CSRF para los endpoints que invoquen a AutoFirma.
                 */
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(

                                //FIRMA DE LA AUTORIZACIÓN POR EL PROFESIONAL MÉDICO
                                new AntPathRequestMatcher(
                                        "/api/profesional-medico/generar-pdf-autorizacion",
                                        HttpMethod.POST.name()
                                ),

                                new AntPathRequestMatcher(
                                        "/api/profesional-medico/pdf-autorizacion-firmada",
                                        HttpMethod.POST.name()
                                ),


                                //COFIRMA DE LA AUTORIZACIÓN POR EL PACIENTE
                                new AntPathRequestMatcher(
                                        "/api/paciente/solicitud-autorizacion-firmada",
                                        HttpMethod.POST.name()
                                ),

                                new AntPathRequestMatcher(
                                        "/api/paciente/aws-pdf-autorizacion-cofirmada",
                                        HttpMethod.POST.name()
                                ),


                                //FIRMA DE LOS INFORMES POR EL PROFESIONAL MÉDICO

                                new AntPathRequestMatcher(
                                        "/api/profesional-medico/generar-pdf-informe",
                                        HttpMethod.POST.name()
                                ),

                                new AntPathRequestMatcher(
                                        "/api/profesional-medico/pdf-informe-firmado",
                                        HttpMethod.POST.name()
                                )
                        )
                )
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .rememberMe(rememberMe -> rememberMe
                        .key("m3JW/0pp2Qmu6W/5y3Xs8XIQrDwVMxH+FnZt0CTnYsU=")
                        .tokenValiditySeconds(86400)
                )
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }
}