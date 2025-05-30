package vitalsanity.config;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11NioProtocol;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.tomcat.util.net.SSLHostConfigCertificate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdditionalHttpsConnectorConfig {

    @Value("${server.ssl.key-store}")
    private String keyStore;

    @Value("${server.ssl.key-store-password}")
    private String keyStorePassword;

    @Value("${server.ssl.key-store-type}")
    private String keyStoreType;

    @Value("${server.ssl.key-alias}")
    private String keyAlias;

    @Value("${server.ssl.trust-store}")
    private String trustStore;

    @Value("${server.ssl.trust-store-password}")
    private String trustStorePassword;

    @Bean
    public TomcatServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
        tomcat.addAdditionalTomcatConnectors(createSslConnector());
        return tomcat;
    }

    private Connector createSslConnector() {
        Connector connector = new Connector(Http11NioProtocol.class.getName());
        connector.setScheme("https");
        connector.setSecure(true);
        connector.setPort(11444);

        Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();
        protocol.setSSLEnabled(true);

        SSLHostConfig sslHostConfig = new SSLHostConfig();
        // Forzamos la verificacion de certificado de cliente
        sslHostConfig.setCertificateVerification("required");

        // Esta línea sirve para agregar la lista de revocación de certificados. Se comenta para que funcione en cualquier dispositivo,
        // ya que en desarrollo depende de una ubiación local del ordenador concreto.
        // sslHostConfig.setCertificateRevocationListFile("C:/certificate-revocation-list-file/ARLFNMTRCM.pem");

        // Configuramos el truststore para validar los certificados de cliente
        sslHostConfig.setTruststoreFile(trustStore);
        sslHostConfig.setTruststorePassword(trustStorePassword);

        SSLHostConfigCertificate certificate = new SSLHostConfigCertificate(sslHostConfig, SSLHostConfigCertificate.Type.RSA);
        certificate.setCertificateKeystoreFile(keyStore);
        certificate.setCertificateKeystorePassword(keyStorePassword);
        certificate.setCertificateKeystoreType(keyStoreType);
        certificate.setCertificateKeyAlias(keyAlias);

        sslHostConfig.addCertificate(certificate);

        connector.addSslHostConfig(sslHostConfig);
        return connector;
    }
}
