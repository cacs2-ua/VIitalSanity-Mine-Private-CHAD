package vitalsanity.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import vitalsanity.interceptor.*;

/**
 * Web configuration to register interceptors.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Autowired
    private AdminInterceptor adminInterceptor;

    @Autowired
    private CentroMedicoInterceptor centroMedicoInterceptor;

    @Autowired
    private ProfesionalMedicoInterceptor profesionalMedicoInterceptor;

    @Autowired
    private PacienteInterceptor pacienteInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/auth/**");

        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/api/admin/**");

        registry.addInterceptor(centroMedicoInterceptor)
                .addPathPatterns("/api/centro-medico/**");

        registry.addInterceptor(profesionalMedicoInterceptor)
                .addPathPatterns("/api/profesional-medico/**");

        registry.addInterceptor(pacienteInterceptor)
                .addPathPatterns("/api/paciente/**");
    }
}
