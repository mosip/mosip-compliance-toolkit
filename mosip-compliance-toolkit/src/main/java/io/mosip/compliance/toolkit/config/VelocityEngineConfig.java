package io.mosip.compliance.toolkit.config;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.NullLogChute;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.velocity.runtime.resource.loader.FileResourceLoader;
import org.springframework.context.annotation.Bean;

public class VelocityEngineConfig {
	
	private static VelocityEngine engine;
	@Bean
	public static synchronized VelocityEngine getVelocityEngine() throws Exception {
		if (engine == null) {
			final Properties properties = new Properties();
			properties.put(RuntimeConstants.INPUT_ENCODING, StandardCharsets.UTF_8.name());
			properties.put(RuntimeConstants.OUTPUT_ENCODING, StandardCharsets.UTF_8.name());
			properties.put(RuntimeConstants.ENCODING_DEFAULT, StandardCharsets.UTF_8.name());
			properties.put(RuntimeConstants.RESOURCE_LOADER, "classpath");
			properties.put(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, ".");
			properties.put(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, NullLogChute.class.getName());
			properties.put("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
			properties.put("file.resource.loader.class", FileResourceLoader.class.getName());

			engine = new VelocityEngine(properties);
			engine.init();
	
		}
		return engine;
	}
}
