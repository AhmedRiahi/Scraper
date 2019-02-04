package com.pp.crawler.core;

import com.pp.crawler.exception.IrrelevantLinkException;
import com.pp.database.model.crawler.Cookie;
import com.pp.database.model.engine.HttpParam;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.HttpStatusException;
import org.springframework.http.HttpMethod;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Data
@NoArgsConstructor
public class URLDownloader {

	private URL url;
	private String urlContent = "";
	private int timeout = 60*1000;
	private boolean followRedirection = true;
	private List<Cookie> cookies;
	private String[] userAgents = 	{"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.101 Safari/537.36"};
	private HttpMethod httpMethod = HttpMethod.GET;
	private List<HttpParam> bodyParams;
	
	
	public URLDownloader(URL url){
		this.url = url;
		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, new TrustManager[] { new TrustAllX509TrustManager() }, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			HttpsURLConnection.setDefaultHostnameVerifier((x,y) ->  true);
		} catch (NoSuchAlgorithmException | KeyManagementException e) {
			log.error(e.getMessage(),e);
		}
		
		HttpURLConnection.setFollowRedirects(this.followRedirection);
	}
	
	
	public void download() throws IrrelevantLinkException, IOException{
		this.urlContent = "";
		HttpURLConnection urlConnection =  (HttpURLConnection) this.url.openConnection();
		urlConnection.setRequestProperty("User-agent",this.getRandomUserAgent());
		String cookiesString = "";
		if(this.cookies != null) {
			cookiesString = this.cookies.stream().map(cookie -> cookie.getName()+"="+cookie.getValue()).collect(Collectors.joining(";"));
		}
		urlConnection.setRequestProperty("Cookie", cookiesString);
		urlConnection.setRequestMethod(this.httpMethod.name());
		if(this.httpMethod.equals(HttpMethod.POST)){
			urlConnection.setDoOutput(true);
			urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			StringBuilder postData = new StringBuilder();
			for (HttpParam param : this.bodyParams) {
				if (postData.length() != 0) postData.append('&');
				postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
				postData.append('=');
				postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
			}
			byte[] postDataBytes = postData.toString().getBytes("UTF-8");
			urlConnection.getOutputStream().write(postDataBytes);
		}
		urlConnection.setConnectTimeout(this.timeout);
		urlConnection.setReadTimeout(this.timeout);
		urlConnection.setInstanceFollowRedirects(this.followRedirection);

		int status = urlConnection.getResponseCode();
		if(urlConnection.getContentType().toLowerCase().contains("html")){
			switch(status){
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
				log.info("URL moved from "+url+" "+urlConnection.getHeaderField("Location"));
				this.setUrl(new URL(urlConnection.getHeaderField("Location")));
				this.download();
				break;
			
			default:
				throw new HttpStatusException("Not yet managed status", status, this.url.toString());
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
		StringBuilder contentBuilder = new StringBuilder();
		while((line=br.readLine())!=null){
			contentBuilder.append(line);
		}
		stream.close();
		br.close();
		return contentBuilder.toString();
		
	}
	
	
	private String getRandomUserAgent(){
		return this.userAgents[new Random().nextInt() % this.userAgents.length];
	}


}

class TrustAllX509TrustManager implements X509TrustManager {
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }

    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
    	//no implementation yet
    }

    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
		//no implementation yet
    }

}