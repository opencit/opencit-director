package com.intel.director.store;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;

import com.intel.director.exception.ImageStoreException;

public class StoreSslUtil {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(StoreSslUtil.class);
;

	public static void addSslSettings(HttpClient httpClient, String url)
			throws ImageStoreException {

		URL glanceUrl;
		try {
			glanceUrl = new URL(url);
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			throw new ImageStoreException("Malformed url", e1);
		}
		String protocol = glanceUrl.getProtocol();
		// / log.info("protocol::" + protocol);
		int portNo = glanceUrl.getPort();
		if (protocol.equals("http")) {
			log.info("Not adding SSL spec data as url is not https");
			return;
		}
		KeyStore trustStore;
		try {
			trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			// log.info("Got the keystore");
		} catch (KeyStoreException e) {
			String msg = "Unable to get the default key store";
			log.error(msg, e);
			throw new ImageStoreException(msg, e);
		}

		String systemJavaHome = System.getenv("JAVA_HOME");
		// log.info("systemJavaHome::" + systemJavaHome);

		String keystorePath = systemJavaHome + "/jre/lib/security/cacerts";
		// log.info("keystorePath  :: " + keystorePath);

		File keystoreFile = new File(keystorePath);
		if (!keystoreFile.exists()) {
			throw new ImageStoreException(
					"Director Keystore file does not exist");
		}
		FileInputStream instream;
		try {
			instream = new FileInputStream(keystoreFile);
			// log.info("Got the stream opened to the keystore");
		} catch (FileNotFoundException e) {
			String msg = "Unable to open the stream to key store";
			log.error(msg, e);
			throw new ImageStoreException(msg, e);
		}

		try {
			try {
				trustStore.load(instream, "changeit".toCharArray());
				// log.info("Loaded the keystore;");
			} catch (NoSuchAlgorithmException e) {
				String msg = "Unable to load key store";
				log.error(msg, e);
				throw new ImageStoreException(msg, e);
			} catch (CertificateException e) {
				String msg = "Unable to load key store";
				log.error(msg, e);
				throw new ImageStoreException(msg, e);
			} catch (IOException e) {
				String msg = "Unable to load key store";
				log.error(msg, e);
				throw new ImageStoreException(msg, e);
			}
		} finally {
			try {
				instream.close();
			} catch (IOException e) {
				String msg = "Unable to close stream to  key store";
				log.error(msg, e);
			}
		}

		SSLSocketFactory socketFactory;
		try {
			socketFactory = new SSLSocketFactory(trustStore);
			// log.info("Got the socket factory");
		} catch (KeyManagementException | UnrecoverableKeyException
				| NoSuchAlgorithmException | KeyStoreException e) {
			String msg = "Unable to get the socket factory";
			log.error(msg, e);
			throw new ImageStoreException(msg, e);
		}

		log.info("Getting scheme for https portNo::" + portNo);
		Scheme sch = new Scheme("https", socketFactory, portNo);

		httpClient.getConnectionManager().getSchemeRegistry().register(sch);

	}

}
