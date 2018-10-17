package com.intel.director.common;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang.StringUtils;

import com.intel.director.common.exception.DirectorException;

public class ValidationUtil {
	public static void validateUrl(String urlStr, String type) throws DirectorException {
		if(StringUtils.isBlank(urlStr)){
			throw new DirectorException("Invalid "+type +" url");
		}

		URL url;
		try {
			url = new URL(urlStr);
		} catch (MalformedURLException e) {
			throw new DirectorException("Invalid " + type + " url");
		}
		String hostByUser = url.getHost();
		if (StringUtils.isBlank(hostByUser)) {
			throw new DirectorException("Error validating " + type + " endpoint. No host specified. ");
		}
		if (StringUtils.isBlank(url.getProtocol())) {
			throw new DirectorException("Error validating " + type + " endpoint. No protocol specified. ");
		}

		if (url.getPort() == -1) {
			throw new DirectorException("Error validating " + type + " endpoint. No port specified.");
		}

		String path = url.getPath();
		if (StringUtils.isNotBlank(path)) {
			throw new DirectorException("Please provide the " + type + " endpoint in format http(s)://HOST:PORT");
		}
	}
}
