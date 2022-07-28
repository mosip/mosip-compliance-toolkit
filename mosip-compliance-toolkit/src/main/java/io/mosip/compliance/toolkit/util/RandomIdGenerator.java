package io.mosip.compliance.toolkit.util;

import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class RandomIdGenerator{
	public synchronized static String generateUUID(String prefix, String replaceHypen, int length)
	{
		String uniqueId = prefix + UUID.randomUUID().toString().replace("-", replaceHypen);
		if (uniqueId.length() <= length)
			return uniqueId;
		return uniqueId.substring(0, length);
	}
}