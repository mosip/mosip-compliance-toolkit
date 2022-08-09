package io.mosip.compliance.toolkit.util;

import java.nio.charset.StandardCharsets;

import io.mosip.kernel.core.util.CryptoUtil;

public final class StringUtil {
	public static String base64UrlEncode (byte [] arg)
    {
        return CryptoUtil.encodeToURLSafeBase64(arg);
    }

	public static String base64UrlEncode (String arg)
    {
        return CryptoUtil.encodeToURLSafeBase64(arg.getBytes(StandardCharsets.UTF_8));
    }

    public static byte[] base64UrlDecode (String arg)
    {
    	return CryptoUtil.decodeURLSafeBase64(arg);
	}

    public static byte [] toUtf8ByteArray (String arg)
    {
        return arg.getBytes (StandardCharsets.UTF_8);
    }
    
    public static String toUtf8String (byte[] arg)
    {
        return new String (arg, StandardCharsets.UTF_8);
    }
} 