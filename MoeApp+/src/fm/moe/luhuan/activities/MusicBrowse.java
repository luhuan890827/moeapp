package fm.moe.luhuan.activities;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import com.alibaba.fastjson.JSON;

import fm.moe.luhuan.JSONUtils;
import fm.moe.luhuan.MoeDbHelper;
import fm.moe.luhuan.R;
import fm.moe.luhuan.adapters.MyCursorAdapter;
import fm.moe.luhuan.adapters.MyViewPagerAdapter;
import fm.moe.luhuan.adapters.SimpleDataAdapter;
import fm.moe.luhuan.beans.data.SimpleData;
import fm.moe.luhuan.http.MoeOauth;
import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;

import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
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

public class MusicBrowse extends Activity {
	private ViewPager mViewPager;
	private Status vStatus = new Status();
	private LayoutInflater inflater;
	private LinearLayout loadMoreBtn;
	private LinearLayout loadingProgress ;
	private MoeOauth http;
	// private Object lock = new Object();
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setTheme(android.R.style.Theme_Holo);
		setContentView(R.layout.music_browse);
		inflater = LayoutInflater.from(this);
		loadingProgress = (LinearLayout) inflater.inflate(
				R.layout.progress_view, null);
		loadMoreBtn = (LinearLayout) inflater.inflate(R.layout.load_more_view, null);
		loadMoreBtn.setOnClickListener(onLoadMoreBtnClick);
		
		// mainWrapper = (RelativeLayout) findViewById(R.id.main_wrapper);
		mViewPager = (ViewPager) findViewById(R.id.view_pager);

		http = new MoeOauth();
		SharedPreferences pref = getSharedPreferences("token", MODE_PRIVATE);
		// 若token未设置成功（例如access_token为空）,则进行httprequest时将直接返回NULL
		http.setToken(pref.getString("access_token", ""),
				pref.getString("access_secret", ""));

		setViewPager();

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

	private void backView() {
		Stack<AdapterDataSet> stack = vStatus.adapterDatas[mViewPager
				.getCurrentItem()];
		ListView lv = (ListView) vStatus.views.get(mViewPager.getCurrentItem());
		lv.removeFooterView(loadingProgress);
		lv.removeFooterView(loadMoreBtn);
		//clearHintView();
		AdapterDataSet dataset = stack.pop();
		// Log.e("adapter", ""+adapter);
		dataset.task.cancel(true);
		lv.setAdapter(dataset.adapter);
		lv.setOnItemClickListener(dataset.onItemClickListener);
	}

	private String getJsonData(String url) {
		String json = null;

		try {
			json = http.oauthRequest(url);
			boolean hasErr = JSON.parseObject(json)
					.getBooleanValue("has_error");

			if (hasErr) {
				atErr();

			}

		} catch (Exception e) {
			Log.e("get JsonData Failed", "err", e);
			atErr();

		}
		return json;
	}

	private void atErr() {
		// TODO Auto-generated method stub
		Message m = new Message();
		m.what = -1;
		mMessageHandler.sendMessage(m);
	}

	private boolean getExploreInfo() {

		String url = "http://moe.fm/explore?api=json&api_key=420f4049d93b1c64f5e811187ad3364c05016179a&new_musics=1&hot_musics=1&hot_radios=1&musics=1";
		String json = getJsonData(url);
		if (json == null) {
			return false;
		}

		// Log.e("raw", json + "");
		List<SimpleData> newAlbums = JSONUtils.getSimpelDataList(json,
				"new_musics");
		List<SimpleData> hotRadios = JSONUtils.getSimpelDataList(json,
				"hot_radios");
		List<SimpleData> musics = JSONUtils.getSimpelDataList(json, "musics");
		List<SimpleData> hotMusics = JSONUtils.getSimpelDataList(json,
				"hot_musics");
		
		vStatus.datas.put("newAlbums", newAlbums);
		vStatus.datas.put("hotRadios", hotRadios);
		vStatus.datas.put("musics", musics);
		vStatus.datas.put("hotMusics", hotMusics);
		return true;

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

		// TODO Auto-generated method stub
		LinearLayout ll = (LinearLayout) inflater.inflate(
				R.layout.list_wrapper, null);

		ListView listView = (ListView) ll.findViewById(R.id.wrapped_list);
		vStatus.views.add(listView);
		LinearLayout hint = (LinearLayout) ll.findViewById(R.id.hint_layout);
		//vStatus.hintViews.add(hint);
		MoeDbHelper dbHelper = new MoeDbHelper(this);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		// ContentValues cv = new ContentValues();
		// cv.put("_id", "1");
		// cv.put("title", "kotoko");
		// cv.put("artist", "kotoko");
		// cv.put("media_path", "xxx.xxx");
		// cv.put("cover_path", "xxx.xxx");
		// cv.put("insert_time", "1111111");
		// db.insert(MoeDbHelper.TABLE_NAME, null, cv);

		Cursor c = db.rawQuery("select _id,title,artist from local_songs_info",
				null);

		ListAdapter adapter = new MyCursorAdapter(getApplicationContext(), c,
				false);
		// 使用cursoradapter勿调用cursor.close();
		
		listView.setAdapter(adapter);

		return ll;
	}

	private LinearLayout setVFavs() {
		// TODO Auto-generated method stub
		LinearLayout ll = (LinearLayout) inflater.inflate(
				R.layout.list_wrapper, null);

		ListView listView = (ListView) ll.findViewById(R.id.wrapped_list);
		vStatus.views.add(listView);
		LinearLayout hint = (LinearLayout) ll.findViewById(R.id.hint_layout);
		//vStatus.hintViews.add(hint);

		String[] tags = new String[] { "收藏的专辑>>", "收藏的电台>>", "喜欢的歌曲>>" };
		ListAdapter adapter = new ArrayAdapter<String>(this,
				R.layout.big_text_item, tags);
//		LinearLayout footer = (LinearLayout) inflater.inflate(R.layout.footer_view, null);
//		listView.addFooterView(footer);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(onMainCataClick);

		return ll;
	}

	private LinearLayout setVExplore() {
		LinearLayout ll = (LinearLayout) inflater.inflate(
				R.layout.list_wrapper, null);

		ListView listView = (ListView) ll.findViewById(R.id.wrapped_list);
		LinearLayout hint = (LinearLayout) ll.findViewById(R.id.hint_layout);
		//vStatus.hintViews.add(hint);
		vStatus.views.add(listView);
		String[] tags = new String[] { "音乐热榜>>>", "精选电台>>>", "魔力播放>>>" };
		ListAdapter adapter = new ArrayAdapter<String>(this,
				R.layout.big_text_item, tags);
		/**/
//		LinearLayout footer = (LinearLayout) inflater.inflate(R.layout.footer_view, null);
//		listView.addFooterView(footer);
		//vStatus.footers.add(footer);
		
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(onMainCataClick);
		
		
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
	
	
	
	
	
	
	private OnItemClickListener onWikiClick = new OnItemClickListener(){
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			((ListView)arg0).removeFooterView(loadMoreBtn);
			AdapterDataSet set = new AdapterDataSet();
			String type = (String) arg0.getTag();
			set.adapter = (ListAdapter) arg0.getAdapter();
			set.onItemClickListener = arg0.getOnItemClickListener();
			WikiTask task = new WikiTask();
			set.task = task;
//			TextView t = new TextView(getApplicationContext());
//			t.setText("1234");
			//vStatus.footers.get(0).addView(t);
			vStatus.adapterDatas[mViewPager.getCurrentItem()].push(set);
			
			String url = null;
			url = "http://moe.fm/listen/playlist?api=json&" + type + "="
					+ arg1.getTag(R.string.item_id) + "&perpage=20";

			task.execute(url, "some data",
					vStatus.views.get(mViewPager.getCurrentItem()),arg1.getTag(R.string.item_id),type);

		}
	};

	
	 private OnItemClickListener onMainCataClick = new OnItemClickListener() {

		public void onItemClick(final AdapterView<?> arg0, View arg1,
				final int arg2, long arg3) {
			AdapterDataSet set = new AdapterDataSet();
			ListView listView = (ListView) arg0;
			set.adapter = listView.getAdapter();
			set.onItemClickListener = arg0.getOnItemClickListener();
			vStatus.adapterDatas[mViewPager.getCurrentItem()].push(set);
			// use hint
//			ProgressBar pb = (ProgressBar) inflater.inflate(
//					R.layout.progress_view, null);
//			setHintView(pb);
			
			
			// Log.e("main caa", "");
			

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
			
			AsyncTask task = new AsyncTask<Object, Void, Boolean>() {
				@Override
				protected void onPreExecute() {
					// TODO Auto-generated method stub
					super.onPreExecute();
					vStatus.views.get(0).addFooterView(loadingProgress);
					vStatus.views.get(0).setAdapter(null);
				}
				@Override
				protected Boolean doInBackground(Object... params) {
					if (vStatus.datas.size() == 0) {
						if (getExploreInfo()) {
							return true;
						}
						return false;
					}
					return true;
				}

				protected void onPostExecute(Boolean success) {
					
					vStatus.views.get(0).removeFooterView(loadingProgress);
					if (success) {
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
					MusicBrowse.this.getApplicationContext(), radios);

			listView.setAdapter(adapter);
			listView.setTag("radio");

			listView.setOnItemClickListener(onWikiClick);

		}

		private void showNewAlbumn() {
			// TODO Auto-generated method stub
			List<SimpleData> albums = (List<SimpleData>) vStatus.datas
					.get("hotMusics");
			ListView listView = (ListView) vStatus.views.get(0);
			listView.setContentDescription("hotMusics");
			ListAdapter adapter = new SimpleDataAdapter(
					MusicBrowse.this.getApplicationContext(), albums);
			listView.setAdapter(adapter);
			listView.setTag("music");
			listView.setOnItemClickListener(onWikiClick);
			//
		}

	};
	private OnItemClickListener onSubClick = new OnItemClickListener() {

		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			int footerCount = ((ListView)arg0).getFooterViewsCount();
			SimpleDataAdapter adapter = null;
			String className = arg0.getAdapter().getClass().getName();
			if(className.indexOf("HeaderViewListAdapter")<0){
				adapter = (SimpleDataAdapter) arg0.getAdapter();
			}else{
				HeaderViewListAdapter hAdapter = (HeaderViewListAdapter) arg0.getAdapter();
				adapter = (SimpleDataAdapter) hAdapter.getWrappedAdapter();
			}
			List<SimpleData> playList = adapter.getData();
			int playListId = (Integer) arg0.getTag(R.string.play_list_id);
			Log.e("play info", "url="+arg1.getTag(R.string.item_mp3_url));
			Log.e("play list id",arg0.getTag(R.string.play_list_id)+"");
			Log.e("play list type", ""+arg0.getTag(R.string.play_list_type)+"");
		}

	};

	public String getUrl_more(String json) {
		boolean hasNext = JSON.parseObject(json).getJSONObject("response")
				.getJSONObject("information").getBooleanValue("may_have_next");
		// Log.e("information", JSON.parseObject(json).getJSONObject("response")
		// .getJSONObject("information").toJSONString());
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
			// TODO Auto-generated method stub
			ListView lv = (ListView) vStatus.views.get(mViewPager
					.getCurrentItem());
			// use hint
			
			//setHintView(pb);
			lv.addFooterView(loadingProgress);
			lv.setAdapter(null);
			super.onPreExecute();

		}

		@Override
		protected Object[] doInBackground(Object... params) {
			// TODO Auto-generated method stub

			String json = getJsonData((String) params[0]);
			String url_more = getUrl_more(json);
			List<SimpleData> l = JSONUtils.getSimpleDataFromPlayList(json);

			ListAdapter adapter = new SimpleDataAdapter(MusicBrowse.this, l);

			Object[] res = new Object[] { params[2], adapter, url_more,params[3] ,params[4]};
			return res;
		}

		@Override
		protected void onPostExecute(Object[] result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);

			ListView listView = (ListView) result[0];
			ListAdapter adapter = (ListAdapter) result[1];
			
			listView.removeFooterView(loadingProgress);
			//clearHintView();
			listView.setTag(R.string.play_list_id,result[3]);
			listView.setTag(R.string.play_list_type,result[4]);
			listView.setOnItemClickListener(onSubClick);
			if (result[2] != null) {
				listView.addFooterView(loadMoreBtn);
				loadMoreBtn.setTag(R.string.more_btn_url, result[2]);
				
				//Log.e("!!!!", "!!!!!!");
//				TextView tv = (TextView) inflater.inflate(
//						R.layout.big_text_item, null);
//				tv.setText("加载更多");
//				tv.setOnClickListener(onLoadMoreBtnClick);
//				tv.setTag(R.string.more_btn_url, result[2]);
				
				//setHintView(tv);
			}
			listView.setAdapter(adapter);
			
		}

	}

	public class FavTask extends AsyncTask<String, Void, Object[]> {
		@Override
		protected void onPreExecute() {
			ListView lv = (ListView) vStatus.views.get(mViewPager
					.getCurrentItem());
			// use hint
			
			//setHintView(pb);
			lv.addFooterView(loadingProgress);
			lv.setAdapter(null);
			super.onPreExecute();
		}
		@Override
		protected Object[] doInBackground(String... params) {
			// TODO Auto-generated method stub

			String json = getJsonData(params[0]);
			String url_more = getUrl_more(json);
			List<SimpleData> list = JSONUtils.getFavs(json, params[1]);
			ListAdapter adapter = new SimpleDataAdapter(MusicBrowse.this, list);
			Object[] res = new Object[] { adapter, params[1], params[2],
					url_more };
			return res;
		}

		@Override
		protected void onPostExecute(Object[] result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
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
			//clearHintView();
			if (result[3] != null) {
				TextView tv = (TextView) inflater.inflate(
						R.layout.big_text_item, null);
				tv.setText("加载更多");
				tv.setOnClickListener(onLoadMoreBtnClick);

				//setHintView(tv);

			}

		}

	}

	public class LoadMoreTaskPlayable extends AsyncTask<Object, Integer, Object[]> {

		@Override
		protected Object[] doInBackground(Object... params) {
			String json = getJsonData((String) params[0]);
			String url_more = getUrl_more(json);
			List<SimpleData> l = JSONUtils.getSimpleDataFromPlayList(json);
			Object[] res = new Object[] { l, url_more };
			return res;
		}

		@Override
		protected void onPostExecute(Object[] result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			//clearHintView();
			HeaderViewListAdapter hAdapter = (HeaderViewListAdapter)vStatus.views.get(
					mViewPager.getCurrentItem()).getAdapter();
			SimpleDataAdapter adapter = (SimpleDataAdapter) hAdapter.getWrappedAdapter();
			adapter.getData().addAll((List<SimpleData>) result[0]);
			adapter.notifyDataSetChanged();
			Log.e("next url", result[1] + "");
			if (result[1] != null) {
				TextView tv = (TextView) inflater.inflate(
						R.layout.big_text_item, null);
				tv.setText("加载更多");
				tv.setOnClickListener(onLoadMoreBtnClick);
				loadMoreBtn.setTag(R.string.more_btn_url, result[1]);
				//setHintView(tv);
			}else{
				vStatus.views.get(mViewPager.getCurrentItem()).removeFooterView(loadMoreBtn);
			}
			loadMoreBtn.findViewById(R.id.load_more_progress).setVisibility(View.GONE);
		}

	}

	private OnClickListener onLoadMoreBtnClick = new OnClickListener() {

		public void onClick(View v) {
//			clearHintView();
//			ProgressBar pb = (ProgressBar) inflater.inflate(
//					R.layout.progress_view, null);
//			setHintView(pb);
			// SimpleDataAdapter adapter =
			// (SimpleDataAdapter)vStatus.views.get(mViewPager.getCurrentItem()).getAdapter();
			v.findViewById(R.id.load_more_progress).setVisibility(View.VISIBLE);
			String url = (String) v.getTag(R.string.more_btn_url);
			LoadMoreTaskPlayable task = new LoadMoreTaskPlayable();
			task.execute(url);
			
		}
	};
	private Handler mMessageHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == -1) {
				Toast.makeText(getApplicationContext(), "无法连接到服务器哦",
						Toast.LENGTH_LONG).show();
				backView();
			}

		};
	};

}
