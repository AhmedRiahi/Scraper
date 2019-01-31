package com.pp.crawler.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.stream.Collectors;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.jsoup.HttpStatusException;

import com.pp.crawler.exception.IrrelevantLinkException;
import com.pp.database.model.crawler.Cookie;

public class URLDownloader {

	private URL url;
	private String urlContent = "";
	private int timeout = 60*1000;
	private boolean followRedirection = true;
	private int status;
	private List<Cookie> cookies;
	private String[] userAgents = 	{	"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.101 Safari/537.36"
									};
	
	
	public URLDownloader(URL url){
		this.url = url;
		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, new TrustManager[] { new TrustAllX509TrustManager() }, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			HttpsURLConnection.setDefaultHostnameVerifier( new HostnameVerifier(){
			    public boolean verify(String string,SSLSession ssls) {
			        return true;
			    }
			});
		} catch (NoSuchAlgorithmException | KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		HttpURLConnection.setFollowRedirects(this.followRedirection);
	}
	
	public URLDownloader(){
		
	}
	
	
	public void download() throws IrrelevantLinkException, IOException{
		
		HttpURLConnection urlConnection =  (HttpURLConnection) this.url.openConnection();
		
		urlConnection.setRequestProperty("User-agent",this.getRandomUserAgent());
		String cookiesString = "";
		if(this.cookies != null) {
			cookiesString = this.cookies.stream().map(cookie -> cookie.getName()+"="+cookie.getValue()).collect(Collectors.joining(";"));
		}
		urlConnection.setRequestProperty("Cookie", cookiesString);
		urlConnection.setConnectTimeout(this.timeout);
		urlConnection.setReadTimeout(this.timeout);
		urlConnection.setInstanceFollowRedirects(this.followRedirection);

		this.status = urlConnection.getResponseCode();
		if(urlConnection.getContentType().toLowerCase().contains("html")){
			switch(this.status){
			case HttpURLConnection.HTTP_OK:
				String charset = "";
				int index = urlConnection.getContentType().indexOf("charset=")+8;
				if(index > 0) {
					charset = urlConnection.getContentType().substring(index);
				}
				try {
					this.urlContent = this.readUrlInputStream(urlConnection.getInputStream(), charset);
				}catch(UnsupportedEncodingException e) {
					this.urlContent = this.readUrlInputStream(urlConnection.getInputStream(), null);
				}
				
				break;
				
			case HttpURLConnection.HTTP_MOVED_PERM:
			case HttpURLConnection.HTTP_MOVED_TEMP:
				System.out.println("URL moved from "+url+" "+urlConnection.getHeaderField("Location"));
				this.setUrl(new URL(urlConnection.getHeaderField("Location")));
				this.download();
				break;
			
			default:
				throw new HttpStatusException("Not yet managed status", this.status, this.url.toString());
			}
		}else{
			throw new IrrelevantLinkException();
		}
	}
	
	
	private String readUrlInputStream(InputStream stream,String charset) throws IOException {
		BufferedReader br ;
		if(charset != null) {
			br = new BufferedReader(new InputStreamReader(stream,charset));
		}else {
			br = new BufferedReader(new InputStreamReader(stream));
		}
		String line = "";
		String content = "";
		while((line=br.readLine())!=null){
			content += line;
		}
		stream.close();
		br.close();
		return content;
		
	}
	
	
	private String getRandomUserAgent(){
		return this.userAgents[(int) (Math.random()*this.userAgents.length)];
	}

	
	// -------------------------------- GETTER / SETTER --------------------------------

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public boolean isFollowRedirection() {
		return followRedirection;
	}

	public void setFollowRedirection(boolean followRedirection) {
		this.followRedirection = followRedirection;
	}

	public URL getUrl() {
		return url;
	}


	public void setUrl(URL url) {
		this.urlContent = "";
		this.url = url;
	}


	public String[] getUserAgents() {
		return userAgents;
	}


	public void setUserAgents(String[] userAgents) {
		this.userAgents = userAgents;
	}


	public String getUrlContent() {
		return urlContent;
	}

	public List<Cookie> getCookies() {
		return cookies;
	}

	public void setCookies(List<Cookie> cookies) {
		this.cookies = cookies;
	}

}

class TrustAllX509TrustManager implements X509TrustManager {
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }

    public void checkClientTrusted(java.security.cert.X509Certificate[] certs,
            String authType) {
    }

    public void checkServerTrusted(java.security.cert.X509Certificate[] certs,
            String authType) {
    }

}