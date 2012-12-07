package fm.moe.luhuan.http;

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
	public String oauthRequest(String url){
		Log.e("url", url);
		if(token!=null&&!token.isEmpty()){
			OAuthRequest req = new OAuthRequest(Verb.GET, url);
			oService.signRequest(token, req);
			Response resp = req.send();
			return resp.getBody();
		}else{
			return null;
		}
	}
	
}
