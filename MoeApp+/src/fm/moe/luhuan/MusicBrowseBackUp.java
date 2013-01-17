package fm.moe.luhuan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;

import com.alibaba.fastjson.JSON;
import fm.moe.luhuan.R;
import fm.moe.luhuan.adapter.MyCursorAdapter;
import fm.moe.luhuan.adapter.MyViewPagerAdapter;
import fm.moe.luhuan.adapter.SimpleDataAdapter;
import fm.moe.luhuan.beans.data.SimpleData;
import fm.moe.luhuan.http.MoeHttp;
import fm.moe.luhuan.service.PlayService;
import fm.moe.luhuan.utils.JSONUtils;
import fm.moe.luhuan.utils.MoeDbHelper;
import android.app.Activity;

import android.content.Intent;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.HeaderViewListAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

import android.widget.TextView;
import android.widget.Toast;

public class MusicBrowseBackUp extends Activity {
	private ViewPager mViewPager;
	private Status vStatus = new Status();
	private LayoutInflater inflater;
	private LinearLayout loadMoreBtn;
	private LinearLayout loadingProgress;
	private MoeHttp http;
	private ConnectivityManager connectivityManager;
	private SQLiteDatabase db;
	// private Object lock = new Object();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//SharedPreferences pref = getSharedPreferences("app_settings", MODE_PRIVATE);
		setContentView(R.layout.music_browse);
		inflater = LayoutInflater.from(this);
		loadingProgress = (LinearLayout) inflater.inflate(
				R.layout.progress_view, null);
		loadMoreBtn = (LinearLayout) inflater.inflate(R.layout.load_more_view,
				null);
		loadMoreBtn.setOnClickListener(onLoadMoreBtnClick);

		mViewPager = (ViewPager) findViewById(R.id.view_pager);

		http = new MoeHttp(this);
		connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		setViewPager();
		
	}
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		db.close();
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		if(!db.isOpen()){
			MoeDbHelper dbHelper = new MoeDbHelper(this);
			db = dbHelper.getWritableDatabase();
			CursorAdapter adapter = (CursorAdapter) vStatus.views.get(2).getAdapter();
			adapter.changeCursor(db.rawQuery("select * from "+MoeDbHelper.TABLE_NAME+" order by insert_time",
					null));
			 
			((CursorAdapter)vStatus.views.get(2).getAdapter()).notifyDataSetChanged();
		}
		
		
	}
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub

		Stack<AdapterDataSet> stack = vStatus.adapterDatas[mViewPager
				.getCurrentItem()];
		if (stack.isEmpty()) {
			super.onBackPressed();
			
		} else {
			backView();
		}

	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		getMenuInflater().inflate(R.menu.app_menu, menu);
		
		return super.onCreateOptionsMenu(menu);
		
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.to_app_pref:
			Intent intent = new Intent(this, AppPref.class);
			startActivity(intent);
			break;

		default:
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}
	@Override
	public boolean onSearchRequested() {
		startSearch(null, false, null, false);
		
		return true;
	}
	
	private void backView() {
		
		Stack<AdapterDataSet> stack = vStatus.adapterDatas[mViewPager
				.getCurrentItem()];
		if(!stack.isEmpty()){
			ListView lv = (ListView) vStatus.views.get(mViewPager.getCurrentItem());
			lv.removeFooterView(loadingProgress);
			lv.removeFooterView(loadMoreBtn);
			AdapterDataSet dataset = stack.pop();
			// Log.e("adapter", ""+adapter);
			dataset.task.cancel(true);
			lv.setAdapter(dataset.adapter);
			lv.setOnItemClickListener(dataset.onItemClickListener);
		}
		
	}

	private String getJsonData(String url, boolean backOnErr) {
		String json = null;

		try {
			json = http.oauthRequest(url);
			boolean hasErr = JSON.parseObject(json)
					.getBooleanValue("has_error");
//			int errCode = JSON.parseObject(json).getJSONObject("error").getIntValue("code");
//			if(errCode!=200){
//				hasErr = true;
//			}
			if (hasErr) {
				mHandler.post(paramErrToast);
				if (backOnErr) {
					mHandler.post(backViewRunnable);
				}
			}
		} catch (Exception e) {
			//Log.e("get JsonData Failed", "err");
			if (backOnErr) {
				mHandler.post(backViewRunnable);
			}
			mHandler.post(httpErrToast);
		}

		return json;
	}

	private void setViewPager() {
		// TODO Auto-generated method stub
		final String[] titles = { "发现音乐", "我的收藏", "本地音乐" };
		ArrayList<LinearLayout> wrappers = new ArrayList<LinearLayout>();
		wrappers.add(setVExplore());
		wrappers.add(setVFavs());
		wrappers.add(setVSaved());
		
		PagerAdapter pa = new MyViewPagerAdapter(wrappers, titles);
		mViewPager.setAdapter(pa);

	}

	private LinearLayout setVSaved() {

//		LinearLayout ll = (LinearLayout) inflater.inflate(
//				R.layout.list_wrapper, null);
//
//		ListView listView = (ListView) ll.findViewById(R.id.wrapped_list);
//		vStatus.views.add(listView);
//
//		MoeDbHelper dbHelper = new MoeDbHelper(this);
//		db = dbHelper.getWritableDatabase();
//
//		 Cursor cursor = db.rawQuery("select * from "+MoeDbHelper.TABLE_NAME+" order by insert_time",
//				null);
//		 
//		ListAdapter adapter = new MyCursorAdapter(getApplicationContext(), cursor,
//				false);
//		// 使用cursoradapter勿调用cursor.close();
//		
//		listView.setAdapter(adapter);
//		listView.setOnItemClickListener(onLocalItemClick);
//		return ll;
		return null;
	}

	private LinearLayout setVFavs() {
//		LinearLayout ll = (LinearLayout) inflater.inflate(
//				R.layout.list_wrapper, null);
//
//		ListView listView = (ListView) ll.findViewById(R.id.wrapped_list);
//		vStatus.views.add(listView);
//		String[] tags = new String[] { "收藏的专辑>>", "收藏的电台>>", "喜欢的歌曲>>" };
//		ListAdapter adapter = new ArrayAdapter<String>(this,
//				R.layout.big_text_item, tags);
//
//		listView.setAdapter(adapter);
//		listView.setOnItemClickListener(onMainCataClick);
//
//		return ll;
		return null;
	}

	private LinearLayout setVExplore() {
		LinearLayout ll = (LinearLayout) inflater.inflate(
				R.layout.list_wrapper, null);
//
//		ListView listView = (ListView) ll.findViewById(R.id.wrapped_list);
//
//		vStatus.views.add(listView);
//		String[] tags = new String[] { "音乐热榜>>>", "精选电台>>>", "魔力播放>>>" };
//		ListAdapter adapter = new ArrayAdapter<String>(this,
//				R.layout.big_text_item, tags);
//
//		listView.setAdapter(adapter);
//		listView.setOnItemClickListener(onMainCataClick);

		return ll;
	}

	private class Status {
		public List<LinearLayout> hintViews = new ArrayList<LinearLayout>();
		public List<ListView> views = new ArrayList<ListView>();
		public HashMap<String, List<SimpleData>> datas = new HashMap<String, List<SimpleData>>();
		public Stack<AdapterDataSet>[] adapterDatas = new Stack[] {
				new Stack<AdapterDataSet>(), new Stack<AdapterDataSet>(),
				new Stack<AdapterDataSet>() };
		public List<LinearLayout> footers = new ArrayList<LinearLayout>();
	}

	private class AdapterDataSet {
		public OnItemClickListener onItemClickListener;
		public ListAdapter adapter;
		public AsyncTask task;

	}

	public void setHintView(View v) {
		LinearLayout hint = vStatus.hintViews.get(mViewPager.getCurrentItem());
		hint.addView(v);
	}

	public void clearHintView() {
		LinearLayout hint = vStatus.hintViews.get(mViewPager.getCurrentItem());
		hint.removeAllViews();
	}

	private OnItemClickListener onWikiClick = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			NetworkInfo ni = connectivityManager.getActiveNetworkInfo();
			if (ni == null ||  !ni.isAvailable() ||!ni.isConnected()) {
				Toast.makeText(MusicBrowseBackUp.this, "当前没有可用的网络连接",
						Toast.LENGTH_SHORT).show();
				return;
			}
			((ListView) arg0).removeFooterView(loadMoreBtn);
			AdapterDataSet set = new AdapterDataSet();
			String type = (String) arg0.getTag();
			set.adapter = (ListAdapter) arg0.getAdapter();
			set.onItemClickListener = arg0.getOnItemClickListener();
			WikiTask task = new WikiTask();
			set.task = task;
			vStatus.adapterDatas[mViewPager.getCurrentItem()].push(set);

			String url = null;
			url = "http://moe.fm/listen/playlist?api=json&" + type + "="
					+ arg1.getTag(R.string.item_id) + "&perpage=20";

			task.execute(url, vStatus.views.get(mViewPager.getCurrentItem()),
					arg1.getTag(R.string.item_id), type);

		}
	};

	private OnItemClickListener onMainCataClick = new OnItemClickListener() {

		public void onItemClick(final AdapterView<?> arg0, View arg1,
				final int arg2, long arg3) {
			NetworkInfo ni = connectivityManager.getActiveNetworkInfo();
//			if (ni == null || !ni.isConnected() || !ni.isAvailable()) {
//				Toast.makeText(MusicBrowse.this, "当前没有可用的网络连接",
//						Toast.LENGTH_SHORT).show();
//				return;
//			}
			AdapterDataSet set = new AdapterDataSet();
			ListView listView = (ListView) arg0;
			set.adapter = listView.getAdapter();
			set.onItemClickListener = arg0.getOnItemClickListener();
			vStatus.adapterDatas[mViewPager.getCurrentItem()].push(set);

			switch (mViewPager.getCurrentItem()) {
			case 0:
				clickAtMainExplore(arg2);

				break;
			case 1:
				clickAtMainFavs(arg2);

				break;
			case 2:

				break;
			default:
				break;
			}

		}

		private void clickAtMainFavs(int arg2) {
			switch (arg2) {
			case 0:
				showFavAlbums();
				break;
			case 1:
				showFavRadios();
				break;
			case 2:
				showFavSongs();
				break;
			default:
				break;
			}

		}

		private void showFavSongs() {

			FavTask task = new FavTask();
			vStatus.adapterDatas[1].peek().task = task;

			final String url = "http://api.moefou.org/user/favs/sub.json?obj_type=song&fav_type=1&perpage=25";
			task.execute(url, "sub", "single");

		}

		private void showFavRadios() {

			FavTask task = new FavTask();
			vStatus.adapterDatas[1].peek().task = task;

			final String url = "http://api.moefou.org/user/favs/wiki.json?obj_type=radio&fav_type=1&perpage=25";
			task.execute(url, "wiki", "radio");
		}

		private void showFavAlbums() {
			// TODO Auto-generated method stub

			FavTask task = new FavTask();
			vStatus.adapterDatas[1].peek().task = task;

			final String url = "http://api.moefou.org/user/favs/wiki.json?obj_type=music&fav_type=1&perpage=25";
			task.execute(url, "wiki", "music");

		}

		private void clickAtMainExplore(final int arg2) {

			AsyncTask task = new AsyncTask<Object, Void, Object[]>() {
				@Override
				protected void onPreExecute() {
					super.onPreExecute();
					vStatus.views.get(0).addFooterView(loadingProgress);
					vStatus.views.get(0).setOnItemClickListener(null);
					vStatus.views.get(0).setAdapter(null);
				}

				@Override
				protected Object[] doInBackground(Object... params) {
					String url = "http://moe.fm/explore?api=json&api_key=420f4049d93b1c64f5e811187ad3364c05016179a&new_musics=1&hot_musics=1&hot_radios=1&musics=1";
					
					if (vStatus.datas.size() == 0) {
						String json = getJsonData(url, true);
						if(json!=null){
							List<SimpleData> newAlbums = JSONUtils.getExpWikiList(
									json, "new_musics");
							List<SimpleData> hotRadios = JSONUtils.getExpWikiList(
									json, "hot_radios");
							List<SimpleData> musics = JSONUtils.getExpWikiList(
									json, "musics");
							List<SimpleData> hotMusics = JSONUtils.getExpWikiList(
									json, "hot_musics");

							vStatus.datas.put("newAlbums", newAlbums);
							vStatus.datas.put("hotRadios", hotRadios);
							vStatus.datas.put("musics", musics);
							vStatus.datas.put("hotMusics", hotMusics);
						}
						
					}
					return null;
				}

				protected void onPostExecute(Object[] obj) {
					if (vStatus.datas.size() != 0) {
						vStatus.views.get(0).removeFooterView(loadingProgress);

						switch (arg2) {
						case 0:
							showNewAlbumn();
							break;
						case 1:
							showHotFm();
							break;
						case 2:
							startMagic();
							break;
						default:
							break;
						}
					}

				}

			};
			vStatus.adapterDatas[0].peek().task = task;
			task.execute("");

		};

		private void startMagic() {
			// TODO Auto-generated method stub

		}

		private void showHotFm() {
			List<SimpleData> radios = (List<SimpleData>) vStatus.datas
					.get("hotRadios");
			ListView listView = (ListView) vStatus.views.get(0);
			listView.setContentDescription("hotRadios");
			ListAdapter adapter = new SimpleDataAdapter(
					MusicBrowseBackUp.this.getApplicationContext(), radios);

			listView.setAdapter(adapter);
			listView.setTag("radio");

			listView.setOnItemClickListener(onWikiClick);

		}

		private void showNewAlbumn() {
			List<SimpleData> albums = (List<SimpleData>) vStatus.datas
					.get("hotMusics");
			ListView listView = (ListView) vStatus.views.get(0);
			listView.setContentDescription("hotMusics");
			ListAdapter adapter = new SimpleDataAdapter(
					MusicBrowseBackUp.this.getApplicationContext(), albums);
			listView.setAdapter(adapter);
			listView.setTag("music");
			listView.setOnItemClickListener(onWikiClick);
			//
		}

	};
	private OnItemClickListener onSubClick = new OnItemClickListener() {

		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			NetworkInfo ni = connectivityManager.getActiveNetworkInfo();
			if (ni == null ||  !ni.isAvailable() ||!ni.isConnected()) {
				Toast.makeText(MusicBrowseBackUp.this, "当前没有可用的网络连接",
						Toast.LENGTH_SHORT).show();
				return;
			}
			SimpleDataAdapter adapter = null;
			String className = arg0.getAdapter().getClass().getName();
			if (className.indexOf("HeaderViewListAdapter") < 0) {
				adapter = (SimpleDataAdapter) arg0.getAdapter();
			} else {
				HeaderViewListAdapter hAdapter = (HeaderViewListAdapter) arg0
						.getAdapter();
				adapter = (SimpleDataAdapter) hAdapter.getWrappedAdapter();
			}
			List<SimpleData> playList = adapter.getData();
			Intent playIntent = new Intent(MusicBrowseBackUp.this, MusicPlay.class);
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
private OnItemClickListener onLocalItemClick = new OnItemClickListener() {

	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		Cursor c = ((CursorAdapter)arg0.getAdapter()).getCursor();
		c.moveToFirst();
		c.moveToPrevious();
		List<SimpleData> l = new ArrayList<SimpleData>();
		
		while(c.moveToNext()){
			SimpleData data = new SimpleData();
			data.setAlbumnCoverUrl(c.getString(7));
			data.setArtist(c.getString(2));
			if(c.getLong(5)==1){
				data.setFav(true);
			}
			data.setTitle(c.getString(1));
			data.setId(c.getInt(0));
			data.setMp3Url(c.getString(6));
			data.setParentId(c.getInt(3));
			data.setParentTitle(c.getString(4));
			data.setAlbumnCoverUrl(c.getString(7));
			l.add(data);
		}
		Intent playIntent = new Intent(MusicBrowseBackUp.this, MusicPlay.class);
		Bundle bundle = new Bundle();

		bundle.putSerializable("playList", (ArrayList<SimpleData>) l);
		bundle.putInt("selectedIndex", arg2);
		bundle.putString("playListId", arg0.getTag(R.string.play_list_id)
				+ "");

		playIntent.putExtras(bundle);

		startActivity(playIntent);
	}
};
	public String getUrl_more(String json) {
		if(json==null){
			return "err";
		}
		boolean hasNext = false;
		
		hasNext = JSON.parseObject(json).getJSONObject("response")
				.getJSONObject("information").getBooleanValue("may_have_next");

		String url_more = null;
		if (hasNext) {
			url_more = JSON.parseObject(json).getJSONObject("response")
					.getJSONObject("information").getString("next_url");
		}
		return url_more;
	}

	public class WikiTask extends AsyncTask<Object, Void, Object[]> {
		@Override
		protected void onPreExecute() {
			ListView lv = (ListView) vStatus.views.get(mViewPager
					.getCurrentItem());
			// use hint

			// setHintView(pb);
			lv.addFooterView(loadingProgress);
			lv.setOnItemClickListener(null);
			lv.setAdapter(null);
			super.onPreExecute();

		}

		/*
		 * @params
		 * url,vStatus.views.get(mViewPager.getCurrentItem()),arg1.getTag
		 * (R.string.item_id),type目标url,listview容器，wiki_id,wiki_type
		 */
		@Override
		protected Object[] doInBackground(Object... params) {
			// TODO Auto-generated method stub

			String json = getJsonData((String) params[0], true);
			String url_more = null;
			ListAdapter adapter = null;
			if (json != null) {
				url_more = getUrl_more(json);
				List<SimpleData> l = JSONUtils.getSimpleDataFromPlayList(json);
				adapter = new SimpleDataAdapter(MusicBrowseBackUp.this, l);
			}
			Object[] res = new Object[] { params[1], adapter, url_more,
					params[2], params[3] };
			return res;
		}

		@Override
		protected void onPostExecute(Object[] result) {
			super.onPostExecute(result);
			if (result[1] != null) {
				ListView listView = (ListView) result[0];
				ListAdapter adapter = (ListAdapter) result[1];

				listView.removeFooterView(loadingProgress);
				listView.setTag(R.string.play_list_id, result[3]);
				listView.setTag(R.string.play_list_type, result[4]);
				listView.setOnItemClickListener(onSubClick);
				if (result[2] != null) {
					listView.addFooterView(loadMoreBtn);
					loadMoreBtn.setTag(R.string.more_btn_url, result[2]);

				}
				listView.setAdapter(adapter);
			}

		}

	}

	public class FavTask extends AsyncTask<String, Void, Object[]> {
		@Override
		protected void onPreExecute() {
			ListView lv = (ListView) vStatus.views.get(mViewPager
					.getCurrentItem());
			lv.addFooterView(loadingProgress);
			lv.setOnItemClickListener(null);
			lv.setAdapter(null);
			super.onPreExecute();
		}

		@Override
		protected Object[] doInBackground(String... params) {

			String json = getJsonData(params[0], true);
			String url_more = null;
			ListAdapter adapter = null;
			if (json != null) {
				url_more = getUrl_more(json);
				List<SimpleData> list = JSONUtils.getFavs(json, params[1]);
				adapter = new SimpleDataAdapter(MusicBrowseBackUp.this, list);
			}
			Object[] res = new Object[] { adapter, params[1], params[2],
					url_more };
			return res;
		}

		@Override
		protected void onPostExecute(Object[] result) {
			super.onPostExecute(result);
			if (result[0] != null) {
				ListView listView = (ListView) vStatus.views.get(mViewPager
						.getCurrentItem());
				listView.setAdapter((ListAdapter) result[0]);
				listView.removeFooterView(loadingProgress);
				if (((String) result[1]).equals("wiki")) {

					listView.setOnItemClickListener(onWikiClick);
				} else {

					listView.setOnItemClickListener(onSubClick);
				}
				listView.setTag(result[2]);
				if (result[3] != null) {
					TextView tv = (TextView) inflater.inflate(
							R.layout.big_text_item, null);
					tv.setText("加载更多");
					tv.setOnClickListener(onLoadMoreBtnClick);
				}
			}

		}

	}

	public class LoadMoreTaskPlayable extends
			AsyncTask<Object, Integer, Object[]> {

		@Override
		protected Object[] doInBackground(Object... params) {
			String json = getJsonData((String) params[0], false);
			String url_more = getUrl_more(json);
			List<SimpleData> l = null;
			if(json!=null){
				
				l = JSONUtils.getSimpleDataFromPlayList(json);
			}
			
			Object[] res = new Object[] { l, url_more };
			return res;
		}

		@Override
		protected void onPostExecute(Object[] result) {
			super.onPostExecute(result);

			HeaderViewListAdapter hAdapter = (HeaderViewListAdapter) vStatus.views
					.get(mViewPager.getCurrentItem()).getAdapter();
			SimpleDataAdapter adapter = (SimpleDataAdapter) hAdapter
					.getWrappedAdapter();
			if(result[0]!=null){
				
				adapter.getData().addAll((List<SimpleData>) result[0]);
				adapter.notifyDataSetChanged();
			}
			
			//Log.e("next url", result[1] + "");
			if (result[1] != null) {
				if(!result[1].equals("err")){
//					TextView tv = (TextView) inflater.inflate(
//							R.layout.big_text_item, null);
//					tv.setText("加载更多");
//					tv.setOnClickListener(onLoadMoreBtnClick);
					loadMoreBtn.setTag(R.string.more_btn_url, result[1]);
				}
				
				// setHintView(tv);
			} else {
				vStatus.views.get(mViewPager.getCurrentItem())
						.removeFooterView(loadMoreBtn);
			}
			loadMoreBtn.findViewById(R.id.load_more_progress).setVisibility(
					View.GONE);
		}

	}

	private OnClickListener onLoadMoreBtnClick = new OnClickListener() {

		public void onClick(View v) {
			//!!
			NetworkInfo ni = connectivityManager.getActiveNetworkInfo();
			if (ni == null ||  !ni.isAvailable() ||!ni.isConnected()) {
				Toast.makeText(MusicBrowseBackUp.this, "当前没有可用的网络连接",
						Toast.LENGTH_SHORT).show();
				return;
			}
			v.findViewById(R.id.load_more_progress).setVisibility(View.VISIBLE);
			String url = (String) v.getTag(R.string.more_btn_url);
			AsyncTask task = new LoadMoreTaskPlayable();
			task.execute(url);

		}
	};

	private Handler mHandler = new Handler();
	private Runnable httpErrToast = new Runnable() {

		public void run() {
			Toast.makeText(getApplicationContext(), "无法连接到服务器",
					Toast.LENGTH_LONG).show();
			
		}
	};
	private Runnable paramErrToast = new Runnable() {

		public void run() {
			Toast.makeText(getApplicationContext(), "请求参数错误", Toast.LENGTH_LONG)
					.show();
			

		}
	};
	private Runnable backViewRunnable = new Runnable() {
		
		public void run() {
			// TODO Auto-generated method stub
			backView();
		}
	};
}
