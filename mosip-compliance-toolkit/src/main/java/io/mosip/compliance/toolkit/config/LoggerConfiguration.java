package io.mosip.compliance.toolkit.config;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.logger.logback.factory.Logfactory;

public class LoggerConfiguration {

	/**
	 * Instantiates a new logger.
	 */
	private LoggerConfiguration() {

	}

	public static Logger logConfig(Class<?> clazz) {
		return Logfactory.getSlf4jLogger(clazz);
	}

}
