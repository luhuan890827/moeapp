package fm.moe.luhuan.http;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import android.util.Log;


public class MoeOauth {
	private OAuthService oService;
	private Token token;
	private  final String CONSUMER_KEY = "420f4049d93b1c64f5e811187ad3364c05016179a";
	private  final String CONSUMER_SECRET = "8191c1951ee62331d84944743ddc3ca0";
	
	public MoeOauth() {
		oService = new ServiceBuilder().provider(MoeOauthAPI.class)
				.apiKey(CONSUMER_KEY).apiSecret(CONSUMER_SECRET).build();

	}

	public void setToken(String accessToken, String accessSecret) {
		token = new Token(accessToken, accessSecret);
		Log.e("token", ""+token);
	}
	
	public String requestToken() {
		token = oService.getRequestToken();
		return oService.getAuthorizationUrl(token);
	}

	public String[] accessToken(String vCode) {
		Verifier v = new Verifier(vCode);
		token = oService.getAccessToken(token, v);
		String access_token = token.getToken();
		String access_secret = token.getSecret();
		String[] ss = {access_token,access_secret};
		return ss;
	}
	public String oauthRequest(String url) {
		Log.e("url", url);
		if(token!=null&&!token.isEmpty()){
			OAuthRequest req = new OAuthRequest(Verb.GET, url);
			req.setConnectTimeout(3000, TimeUnit.MILLISECONDS);
			oService.signRequest(token, req);
			Response resp = req.send();
//			HttpParams params = new BasicHttpParams();
//			HttpConnectionParams.setConnectionTimeout(params, 30000);
//			HttpClient h = new DefaultHttpClient(params);
//			
//			HttpGet get = new HttpGet("");
//			
//			HttpResponse resp2 = h.execute(get);
			return resp.getBody();
			
			
		}else{
			return null;
		}
	}
	
}
