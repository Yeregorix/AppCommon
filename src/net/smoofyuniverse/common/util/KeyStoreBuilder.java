package net.smoofyuniverse.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class KeyStoreBuilder {
	private KeyStore keystore;
	
	private X509Certificate[] certs;
	
	public KeyStoreBuilder() throws KeyStoreException {
		this(KeyStore.getDefaultType());
	}
	
	public KeyStoreBuilder(String type) throws KeyStoreException {
		this.keystore = KeyStore.getInstance(type);
	}
	
	public void load() throws GeneralSecurityException, IOException {
		this.keystore.load(null, new char[0]);
	}
	
	public void loadSystem(char[] password) throws GeneralSecurityException, IOException {
		Path dir = Paths.get(System.getProperty("java.home"), "lib", "security");
		Path keystore = dir.resolve("jssecacerts");
		if (!Files.exists(keystore))
			keystore = dir.resolve("cacerts");
		load(keystore, password);
	}
	
	public void load(Path input) throws GeneralSecurityException, IOException {
		load(input, new char[0]);
	}
	
	public void load(Path input, char[] password) throws GeneralSecurityException, IOException {
		try (InputStream in = Files.newInputStream(input)) {
			load(in, password);
		}
	}
	
	public void load(InputStream input) throws GeneralSecurityException, IOException {
		load(input, new char[0]);
	}
	
	public void load(InputStream input, char[] password) throws GeneralSecurityException, IOException {
		this.keystore.load(input, password);
	}
	
	public void save(Path output) throws GeneralSecurityException, IOException {
		save(output, new char[0]);
	}
	
	public void save(Path output, char[] password) throws GeneralSecurityException, IOException {
		try (OutputStream out = Files.newOutputStream(output)) {
			save(out, password);
		}
	}
	
	public void save(OutputStream output) throws GeneralSecurityException, IOException {
		save(output, new char[0]);
	}
	
	public void save(OutputStream output, char[] password) throws GeneralSecurityException, IOException {
		this.keystore.store(output, password);
	}
	
	public X509Certificate[] listCertificates(String host) throws GeneralSecurityException, IOException {
		return listCertificates(host, 443);
	}
	
	public X509Certificate[] listCertificates(String host, int port) throws GeneralSecurityException, IOException {
		SSLContext context = SSLContext.getInstance("TLS");
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(this.keystore);
		X509TrustManager defaultTm = (X509TrustManager) tmf.getTrustManagers()[0];
		TrustManager tm = new X509TrustManager() {
			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				throw new UnsupportedOperationException();
			}

			@Override
			public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) throws CertificateException {
				KeyStoreBuilder.this.certs = certs;
				defaultTm.checkServerTrusted(certs, authType);
			}
		};
		context.init(null, new TrustManager[] {tm}, null);
		SSLSocketFactory factory = context.getSocketFactory();
		
		try (SSLSocket socket = (SSLSocket) factory.createSocket(host, port)) {
			socket.setSoTimeout(DownloadUtil.prefReadTimeout);
			socket.startHandshake();
		} catch (SSLException e) {}
		
		return this.certs;
	}
	
	public void installCertificate(String alias, X509Certificate cert) throws KeyStoreException {
		this.keystore.setCertificateEntry(alias, cert);
	}
	
	public void installCertificate(String host, int index) throws GeneralSecurityException, IOException {
		installCertificate(host, 443, index);
	}
	
	public void installCertificate(String host, int port, int index) throws GeneralSecurityException, IOException {
		installCertificate(host + "-" + (index +1), listCertificates(host, port)[index]);
	}
}
