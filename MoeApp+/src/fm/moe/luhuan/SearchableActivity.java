package fm.moe.luhuan;

import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import fm.moe.luhuan.http.MoeHttp;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import android.view.MenuItem;
import android.view.View;

public class SearchableActivity extends FragmentActivity {
	private SearchableFragment mFragment;
	private Handler mHandler = new Handler();
	public static final String EXTRA_SEARCH_RESULT_JSON = "result json";
	public static final String EXTRA_SEARCH_URL = "searchUrl";

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.searchable);
		mFragment = new SearchableFragment();
		getActionBar().setDisplayOptions(1, ActionBar.DISPLAY_HOME_AS_UP);
		
		final String query = getIntent().getStringExtra(SearchManager.QUERY);
		getActionBar().setTitle("\"" + query + "\"的搜索结果(专辑)");

		new mySearchTread(query).start();

	}

	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.remove(mFragment);
		ft.commit();
		mFragment = new SearchableFragment();
		findViewById(R.id.result_progress).setVisibility(View.VISIBLE);
		String query = intent.getStringExtra(SearchManager.QUERY);
		getActionBar().setTitle("\"" + query + "\"的搜索结果(专辑)");
		new mySearchTread(query).start();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		if (mFragment != null) {
			if (!mFragment.backView()) {
				this.finish();
			}
		} else {
			this.finish();
		}
	}
@Override
protected void onStop() {
	// TODO Auto-generated method stub
	super.onStop();
	this.finish();
}
	class mySearchTread extends Thread {
		String query;

		public mySearchTread(String q) {
			query = q;
		}

		public void run() {
			MoeHttp http = new MoeHttp(getApplicationContext());
			String keyword = null;
			String json = null;

			try {
				keyword = URLEncoder.encode(query, "utf8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			try {
				json = http
						.oauthRequest("http://api.moefou.org/search/wiki.json?wiki_type=music&keyword="
								+ keyword);

			} catch (SocketTimeoutException e) {
				e.printStackTrace();
			}

			Bundle bundle = new Bundle();
			bundle.putString(EXTRA_SEARCH_RESULT_JSON, json);
			bundle.putString(EXTRA_SEARCH_URL,
					"http://api.moefou.org/search/wiki.json?wiki_type=music&keyword="
							+ keyword);
			mFragment.setArguments(bundle);

			mHandler.post(new Runnable() {

				@Override
				public void run() {
					FragmentTransaction ft = getSupportFragmentManager()
							.beginTransaction();
					ft.add(R.id.searchable_frag, mFragment);
					ft.commit();
					findViewById(R.id.result_progress).setVisibility(View.GONE);
				}
			});
		}
	}

}
