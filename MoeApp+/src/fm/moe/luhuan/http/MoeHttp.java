package fm.moe.luhuan.http;


import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import android.content.Context;
import android.content.SharedPreferences;

import android.util.Log;

public class MoeHttp {
	private OAuthService oService;
	private Token token;
	private final String CONSUMER_KEY = "420f4049d93b1c64f5e811187ad3364c05016179a";
	private final String CONSUMER_SECRET = "8191c1951ee62331d84944743ddc3ca0";


	public MoeHttp(Context c) {
		oService = new ServiceBuilder().provider(MoeOauthAPI.class)
				.apiKey(CONSUMER_KEY).apiSecret(CONSUMER_SECRET).build();
		
		SharedPreferences pref = c.getSharedPreferences("token",Context.MODE_PRIVATE);
		if(pref.contains("access_token")){
			token = new Token(pref.getString("access_token", ""),pref.getString("access_secret", ""));
		}
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
		String[] ss = { access_token, access_secret };
		return ss;
	}

	public String oauthRequest(String url)throws SocketTimeoutException {
		Log.e("url", url);
		if (token != null && !token.isEmpty()) {
			OAuthRequest req = new OAuthRequest(Verb.GET, url);
			req.setConnectTimeout(3000, TimeUnit.MILLISECONDS);
			req.setReadTimeout(4000, TimeUnit.MILLISECONDS);
			oService.signRequest(token, req);
			Response resp = req.send();
			return resp.getBody();

		} else {
			return null;
		}
	}



}
