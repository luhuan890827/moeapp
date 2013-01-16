package fm.moe.luhuan;
import fm.moe.luhuan.R;
import fm.moe.luhuan.http.MoeHttp;
import fm.moe.luhuan.utils.AppContextUtils;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import android.net.UrlQuerySanitizer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

public class AppInit extends Activity {
	private WebView wv;
	private MoeHttp oauth;
	private ImageView logo;
	private SharedPreferences pref;
	private String authUrl;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// getActionBar().hide();
		setContentView(R.layout.app_init);
		if(AppContextUtils.isPlaybackServiceRunning(getApplicationContext())){
			Intent intent = new Intent(this,MusicBrowse.class);
			startActivity(intent);
			this.finish();
		}
		oauth = new MoeHttp(this);
		logo = (ImageView) findViewById(R.id.init_logo);
		wv = (WebView) findViewById(R.id.oauth_wv);
		
		pref =getSharedPreferences("token", MODE_PRIVATE);
		if (pref.contains("access_token")) {
			startAlphaAnimation(1600, false);
		} else {
			ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo ni = cm.getActiveNetworkInfo();
			if (ni == null ||  !ni.isAvailable() ||!ni.isConnected()) {
				Toast.makeText(this, "首次启动需要有效的网络连接，请检查你的网络设置",
						Toast.LENGTH_SHORT).show();
			} else {
				loadOauthPage();
			}

		}

	}

	public void startAlphaAnimation(int duration, final boolean showWebView) {
		AlphaAnimation aa = new AlphaAnimation(1f, 0.5f);
		aa.setAnimationListener(new AnimationListener() {

			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub

			}

			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			public void onAnimationEnd(Animation animation) {
				logo.setVisibility(View.INVISIBLE);
				findViewById(R.id.init_progress).setVisibility(View.INVISIBLE);
				if (showWebView) {
					wv.setVisibility(View.VISIBLE);
				} else {
					Intent i = null;
					if (pref.getBoolean("default play magic", true)) {
						 i = new Intent(AppInit.this,MusicBrowse.class);
						// i.putExtra("magic", true);
						 startActivity(i);
						AppInit.this.finish();
					}
				}

			}
		});
		aa.setDuration(duration);
		logo.startAnimation(aa);
	}

	public void loadOauthPage() {
		wv.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				// TODO Auto-generated method stub
				final UrlQuerySanitizer sanitizer = new UrlQuerySanitizer(url);
				if (sanitizer.getValue("verifier") != null) {
					final String verifyCode = sanitizer.getValue("verifier");

					new AsyncTask<Object, Object, Object>() {
						@Override
						protected void onPreExecute() {
							// TODO Auto-generated method stub
							super.onPreExecute();
							wv.setVisibility(View.GONE);
							findViewById(R.id.init_progress).setVisibility(
									View.VISIBLE);
						};

						@Override
						protected Object doInBackground(Object... params) {
							// TODO Auto-generated method stub
							String[] s = oauth.accessToken(verifyCode);
							Editor e = pref.edit();
							e.putString("access_token", s[0]);
							e.putString("access_secret", s[1]);
							e.commit();
							return null;
						}

						protected void onPostExecute(Object result) {
							super.onPostExecute(result);
							wv.setVisibility(View.GONE);
							findViewById(R.id.init_progress).setVisibility(
									View.INVISIBLE);
							findViewById(R.id.oauth_success_text)
									.setVisibility(View.VISIBLE);
							findViewById(R.id.start_moe).setVisibility(
									View.VISIBLE);
							findViewById(R.id.start_moe).setOnClickListener(
									new OnClickListener() {

										public void onClick(View v) {
											Intent i = new Intent(AppInit.this,MusicBrowse.class);
											startActivity(i);
											AppInit.this.finish();

										}
									});
						};

					}.execute(1);
				}

				return super.shouldOverrideUrlLoading(view, url);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				// TODO Auto-generated method stub
				super.onPageFinished(view, url);
				if (url.equals(authUrl)) {
					startAlphaAnimation(1000, true);
				}

			}
		});
		new AsyncTask<Object, Integer, String>() {

			@Override
			protected String doInBackground(Object... params) {
				// Log.e("request", "start");
				try{
					authUrl = oauth.requestToken();
				}catch(Exception e){
					//Log.e("oauth failed", "",e);
					mHandler.post(onOauthFailed);
				}
				// Log.e("request", "complete");
				return null;
			}

			protected void onPostExecute(String result) {
				wv.loadUrl(authUrl);

			};

		}.execute();

	}
	private Handler mHandler = new Handler();
	private Runnable onOauthFailed = new Runnable() {
		
		public void run() {
			// TODO Auto-generated method stub
			Toast.makeText(AppInit.this, "萌否服务器似乎有些问题，请稍后再来", Toast.LENGTH_SHORT).show();
			AppInit.this.finish();
			
		}
	};
}
