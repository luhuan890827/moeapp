package fm.moe.luhuan.activities;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import fm.moe.luhuan.JSONUtils;
import fm.moe.luhuan.R;
import fm.moe.luhuan.adapters.SimpleDataAdapter;
import fm.moe.luhuan.beans.data.SimpleData;
import fm.moe.luhuan.http.MoeHttp;
import fm.moe.luhuan.service.PlayService;
import android.app.ActionBar;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.HeaderViewListAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class SearchableActivity extends Activity {
	private MoeHttp http;
	private Handler mHandler = new Handler();
	private ListView listView;
	private LinearLayout loadMoreBtn;
	private String searchUrlBase;
	private ConnectivityManager connectivityManager;
	private List<SimpleData> wikiData;
	private SimpleDataAdapter listAdapter;
	private boolean isShowingPlaylist = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_result);
		Intent intent = getIntent();
		http = new MoeHttp(this);
		listView = (ListView) findViewById(R.id.result_album_list);
		connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		getActionBar().setDisplayOptions(1, ActionBar.DISPLAY_HOME_AS_UP);
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			final String query = intent.getStringExtra(SearchManager.QUERY);

			new QueryThread(query).start();
			getActionBar().setTitle("\"" + query + "\"的搜索结果(专辑)");
		}
		loadMoreBtn = (LinearLayout) LayoutInflater.from(this).inflate(
				R.layout.load_more_view, null);
		listView.addFooterView(loadMoreBtn);
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
	if(isShowingPlaylist){
		//not work properly
		listAdapter.getData().clear();
		listAdapter.getData().addAll(wikiData);
		listAdapter.notifyDataSetChanged();
		listView.setOnItemClickListener(onWikiClick);
		isShowingPlaylist = false;
	}else{
		super.onBackPressed();
	}
	// TODO Auto-generated method stub
	
}
	private OnItemClickListener onWikiClick = new OnItemClickListener() {

		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			 NetworkInfo ni = connectivityManager.getActiveNetworkInfo();
			 if (ni == null || !ni.isAvailable() ||!ni.isConnected()) {
			 Toast.makeText(SearchableActivity.this, "当前没有可用的网络连接",
			 Toast.LENGTH_SHORT).show();
			 return;
			 }
			HeaderViewListAdapter hAdapter = (HeaderViewListAdapter) listView
					.getAdapter();
			SimpleDataAdapter adapter = (SimpleDataAdapter) hAdapter
					.getWrappedAdapter();
			wikiData = adapter.getData();
			String url = "http://moe.fm/listen/playlist?api=json&" + "music="
					+ arg1.getTag(R.string.item_id) + "&perpage=20";
			findViewById(R.id.result_progress).setVisibility(View.VISIBLE);
			AsyncTask loadPlaylist = new LoadPlaylist();
			loadPlaylist.execute(url);
			
		}
	};
	private OnItemClickListener onPlaylistItemClick = new OnItemClickListener() {

		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			NetworkInfo ni = connectivityManager.getActiveNetworkInfo();
			if (ni == null ||  !ni.isAvailable() ||!ni.isConnected()) {
				Toast.makeText(SearchableActivity.this, "当前没有可用的网络连接",
						Toast.LENGTH_SHORT).show();
				return;
			}
			
			List<SimpleData> playList = listAdapter.getData();
			Intent playIntent = new Intent(SearchableActivity.this, MusicPlay.class);
			Bundle bundle = new Bundle();

			bundle.putSerializable(PlayService.EXTRA_PLAYLIST, (ArrayList<SimpleData>) playList);
			bundle.putInt(PlayService.EXTRA_SELECTED_INDEX, arg2);
			bundle.putString(PlayService.EXTRA_PLAYLIST_ID, arg0.getTag(R.string.play_list_id)
					+ "");
			bundle.putBoolean(PlayService.EXTRA_IF_NEED_NETWORK, true);
			playIntent.putExtras(bundle);
			startActivity(playIntent);
		}
	};
	private OnClickListener onLoadMoreBtnClick = new OnClickListener() {

		public void onClick(View v) {
//			 !!
			 NetworkInfo ni = connectivityManager.getActiveNetworkInfo();
			 if (ni == null || !ni.isAvailable() || !ni.isConnected()) {
			 Toast.makeText(SearchableActivity.this, "当前没有可用的网络连接",
			 Toast.LENGTH_SHORT).show();
			 return;
			 }
			v.findViewById(R.id.load_more_progress).setVisibility(View.VISIBLE);
			String url = (String) v.getTag(R.string.more_btn_url);
			AsyncTask task = new LoadmoreWiki();
			task.execute(url);

		}
	};
	private Runnable onNetworkErr = new Runnable() {

		public void run() {
			Toast.makeText(SearchableActivity.this, "无法连接到服务器",
					Toast.LENGTH_SHORT).show();

		}
	};

	private class LoadmoreWiki extends AsyncTask<Object, Integer, Object[]> {

		@Override
		protected Object[] doInBackground(Object... params) {
			String url = (String) params[0];
			List<SimpleData> l = null;
			String nextUrl = null;
			try {
				String json = http.oauthRequest(url);
				JSONObject infomation = JSON.parseObject(json)
						.getJSONObject("response").getJSONObject("information");
				l = JSONUtils.getWikiList(json);
				int totalCount = infomation.getIntValue("count");
				int page = infomation.getIntValue("page");
				int perPage = infomation.getIntValue("perpage");
				if (totalCount > page * perPage) {

					nextUrl = searchUrlBase + "&page=" + (1 + page);

				}
			} catch (Exception e) {
				nextUrl = "err";
			}
			return new Object[] { l, nextUrl };
		}

		@Override
		protected void onPostExecute(Object[] result) {
			super.onPostExecute(result);
			wikiData = listAdapter.getData();
			if (result[0] != null) {
				listAdapter.getData().addAll((List<SimpleData>) result[0]);
				
				listAdapter.notifyDataSetChanged();
			}
			if (result[1] == null) {
				loadMoreBtn.setVisibility(View.GONE);

			} else if (!result[1].equals("err")) {
				loadMoreBtn.setTag(R.string.more_btn_url, result[1]);
			}
			loadMoreBtn.findViewById(R.id.load_more_progress).setVisibility(
					View.GONE);
		}
	}

	private class LoadPlaylist extends AsyncTask<Object, Integer, Object[]> {

		@Override
		protected Object[] doInBackground(Object... params) {
			List<SimpleData> l = null;
			String nextUrl = null;
			try {
				String json = http.oauthRequest((String) params[0]);
				l = JSONUtils.getSimpleDataFromPlayList(json);
				JSONObject information = JSON.parseObject(json)
						.getJSONObject("response").getJSONObject("information");
				if (information.getBooleanValue("may_have_next")) {
					nextUrl = information.getString("next_url");
				}

			} catch (Exception e) {
				mHandler.post(onNetworkErr);
				e.printStackTrace();
			}
			return new Object[] { l, nextUrl };
		}

		@Override
		protected void onPostExecute(Object[] result) {
			super.onPostExecute(result);
			if (result[0] != null) {
				listAdapter.getData().clear();
				listAdapter.getData().addAll((List<SimpleData>) result[0]);
				listAdapter.notifyDataSetChanged();
				isShowingPlaylist = true;
				listView.setOnItemClickListener(onPlaylistItemClick);
			}
			if (result[1] == null) {
				loadMoreBtn.setVisibility(View.GONE);
				

			} else if (!result[1].equals("err")) {
				loadMoreBtn.setTag(R.string.more_btn_url, result[1]);
				loadMoreBtn.setVisibility(View.VISIBLE);
			}
			findViewById(R.id.result_progress).setVisibility(View.GONE);
			loadMoreBtn.findViewById(R.id.load_more_progress).setVisibility(
					View.GONE);
		}
	}

	private class QueryThread extends Thread {
		private String url;

		public QueryThread(String queryUrl) {
			url = queryUrl;
		}

		public void run() {
			String keyword = null;
			try {
				keyword = URLEncoder.encode(url, "utf8");

			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			String json_album = null;
			searchUrlBase = "http://api.moefou.org/search/wiki.json?wiki_type=music&keyword="
					+ keyword;
			try {
				json_album = http
						.oauthRequest("http://api.moefou.org/search/wiki.json?wiki_type=music&keyword="
								+ keyword);
			} catch (Exception e) {
				mHandler.post(new Runnable() {

					public void run() {

						Toast.makeText(SearchableActivity.this, "服务器连接超时",
								Toast.LENGTH_SHORT).show();
						finish();
					}
				});

			}
			if (json_album != null) {
				final List<SimpleData> list = new ArrayList<SimpleData>();
				JSONObject infomation = JSON.parseObject(json_album)
						.getJSONObject("response").getJSONObject("information");
				final int totalCount = infomation.getIntValue("count");
				final int page = infomation.getIntValue("page");
				final int perPage = infomation.getIntValue("perpage");
				List<SimpleData> albumList = JSONUtils.getWikiList(json_album);
				list.addAll(albumList);
				
				mHandler.post(new Runnable() {

					public void run() {
						wikiData = list;
						listAdapter = new SimpleDataAdapter(
								SearchableActivity.this,list );
						if (totalCount > page * perPage) {
							loadMoreBtn.setVisibility(View.VISIBLE);
							loadMoreBtn.setTag(R.string.more_btn_url,
									searchUrlBase + "&page=" + (1 + page));
							loadMoreBtn.setOnClickListener(onLoadMoreBtnClick);
						}
						listView.setAdapter(listAdapter);
						listView.setOnItemClickListener(onWikiClick);
						findViewById(R.id.result_progress).setVisibility(
								View.GONE);
					}
				});
			}

		}
	}

}
