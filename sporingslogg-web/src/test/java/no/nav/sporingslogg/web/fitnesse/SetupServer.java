package no.nav.sporingslogg.web.fitnesse;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Properties;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.jsslutils.extra.apachehttpclient.SslContextedSecureProtocolSocketFactory;

public class SetupServer {
	
	static String url;
	static BrukernavnOgPassord bruker;
	
	private Properties testUserPasswords = null;

	private static class BrukernavnOgPassord {
		final String brukernavn;
		final String passord;
		public BrukernavnOgPassord(String brukernavn, String passord) {
			this.brukernavn = brukernavn;
			this.passord = passord;
		}		
	}
	
    public SetupServer(String url) {
    	SetupServer.url = url.trim();
    	if (url.contains("https")) {
    		setupSsl();
    	}
    }
	
	public void bruker(String brukernavn) { 
		String passord = getPassord("fitnesseusers.txt", brukernavn);
		bruker = new BrukernavnOgPassord(brukernavn.trim(), passord.trim());
	}
	
	public static String brukernavn() {
		return bruker.brukernavn;
	}
	public static String passord() {
		return bruker.passord;
	}
	private String getPassord(String fil, String brukernavn) {
		try {
			if (testUserPasswords == null) {
				testUserPasswords = new Properties();
				testUserPasswords.load(getClass().getClassLoader().getResourceAsStream(fil));
			}
			String password = testUserPasswords.getProperty(brukernavn);
			if (password == null) {
				throw new RuntimeException("Kan ikke lese passord for " + brukernavn + " fra " + fil);
			}
			return password;
		} catch (IOException e) {
			throw new RuntimeException("Kan ikke lese passord for " + brukernavn + " fra " + fil);
		}
	}

	// Sett opp slik at HTTPS kan brukes uten sertifikatproblemer
	private void setupSsl() {
		try {
			X509TrustManager trustManager = new X509TrustManager() {
		        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
		        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
		        public X509Certificate[] getAcceptedIssuers() { return null; }
		    };
			SSLContext ctx = SSLContext.getInstance("TLS");
	        ctx.init(null, new TrustManager[] {trustManager}, null);
	        SSLContext.setDefault(ctx);
			SslContextedSecureProtocolSocketFactory secureProtocolSocketFactory = new SslContextedSecureProtocolSocketFactory(ctx);			
			secureProtocolSocketFactory.setHostnameVerification(false);
			Protocol.registerProtocol("https", new Protocol("https", (ProtocolSocketFactory) secureProtocolSocketFactory, 443));
		} catch (NoSuchAlgorithmException | KeyManagementException e) {
			throw new RuntimeException(e);
		}
	}
}
