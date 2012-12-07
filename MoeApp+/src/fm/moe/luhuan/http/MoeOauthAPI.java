package fm.moe.luhuan.http;

import org.scribe.builder.api.DefaultApi10a;
import org.scribe.model.Token;

public class MoeOauthAPI extends DefaultApi10a{

	private static final String AUTHORIZE_URL = "http://api.moefou.org/oauth/authorize";
	private static final String REQUEST_TOKEN_RESOURCE = "http://api.moefou.org/oauth/request_token";
	private static final String ACCESS_TOKEN_RESOURCE = "http://api.moefou.org/oauth/access_token";
	
	
	@Override
	public String getRequestTokenEndpoint() {
		// TODO Auto-generated method stub
		return REQUEST_TOKEN_RESOURCE;
	}
	@Override
	public String getAccessTokenEndpoint() {
		// TODO Auto-generated method stub
		return ACCESS_TOKEN_RESOURCE;
	}
	@Override
	public String getAuthorizationUrl(Token requestToken) {
		// TODO Auto-generated method stub
		return AUTHORIZE_URL
				+"?oauth_token=" + requestToken.getToken();
	}

}
