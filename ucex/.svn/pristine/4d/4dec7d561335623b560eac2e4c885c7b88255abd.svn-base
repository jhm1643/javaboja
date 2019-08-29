package api.config;

import java.io.FileNotFoundException;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;

@Configuration
@EnableConfigurationProperties(SSLProperties.class)
public class SSLConfiguration {
	@Bean
	public EmbeddedServletContainerCustomizer containerCustomizer() {
		return new SSLCustomizer();
	}

	private static class SSLCustomizer implements EmbeddedServletContainerCustomizer {

		@Autowired
		private SSLProperties sslProperties;

		@Override
		public void customize(ConfigurableEmbeddedServletContainer factory) {
			if (factory instanceof TomcatEmbeddedServletContainerFactory) {
				TomcatEmbeddedServletContainerFactory containerFactory = (TomcatEmbeddedServletContainerFactory) factory;
				containerFactory.addConnectorCustomizers(new TomcatConnectorCustomizer() {

					@Override
					public void customize(Connector connector) {

						try {
							connector.setAttribute("keystoreFile", ResourceUtils.getFile(sslProperties.getKeystoreFile()).getAbsoluteFile());
						} catch (FileNotFoundException e) {
							System.err.println(e.getMessage());
							System.exit(1);
						}

						connector.setPort(sslProperties.getPort());
						connector.setSecure(sslProperties.isSecure());
						connector.setScheme(sslProperties.getSchema());
//						connector.setAttribute("keyAlias", sslProperties.getKeystoreAlias());
						connector.setAttribute("keystoreType", sslProperties.getKeystoreType());
						connector.setAttribute("keystorePass", sslProperties.getKeystorePassword());
						connector.setAttribute("clientAuth", sslProperties.isClientAuth());
						connector.setAttribute("sslProtocol", sslProperties.getProtocol());
						connector.setAttribute("SSLEnabled", sslProperties.isEnabled());
						connector.setAttribute("protocol", "org.apache.coyote.http11.Http11Protocol");
						
//						System.out.println("::::::::::::::: " + connector);
					}
				});
				
//				containerFactory.addAdditionalTomcatConnectors(createHttpConnector());
			}
		}

		private Connector createHttpConnector() {
			Connector con = new Connector("org.apache.coyote.http11.Http11NioProtocol");
//			Http11NioProtocol protocol = (Http11NioProtocol) con.getProtocolHandler();
			
			con.setScheme("http");
			con.setSecure(false);
			con.setPort(sslProperties.getHttp_port());
			
			return con;
		}
	}
}
