package fm.moe.luhuan.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
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
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MusicBrowse extends Activity {
	private ViewPager mViewPager;
	private Status vStatus = new Status();
	private LayoutInflater inflater;
	private RelativeLayout mainWrapper;
	private ProgressBar loadingProgress;
	private TextView loadingMore;
	private MoeOauth http;
	//private Object lock = new Object();
	private OnItemClickListener onMainCataClick = new OnMainCataClick();
	private OnItemClickListener onWikiClick = new OnWikiItemClick();
	private OnItemClickListener onSubClick = new OnSubItemClick();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setTheme(android.R.style.Theme_Holo);
		setContentView(R.layout.music_browse);
		inflater = LayoutInflater.from(this);
		mainWrapper = (RelativeLayout) findViewById(R.id.main_wrapper);
		mViewPager = (ViewPager) findViewById(R.id.view_pager);
		loadingProgress = (ProgressBar) inflater.inflate(
				R.layout.progress_view, null);
		loadingMore = (TextView) inflater.inflate(R.layout.big_text_item, null);
		loadingMore.setText("加载更多");

		http = new MoeOauth();
		SharedPreferences pref = getSharedPreferences("token", MODE_PRIVATE);
		//若token未设置成功（例如access_token为空）,则进行httprequest时将直接返回NULL
		http.setToken(pref.getString("access_token", ""),
				pref.getString("access_secret", ""));

		getExploreInfo();
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
			ListView lv = (ListView) vStatus.views.get(mViewPager
					.getCurrentItem());
			lv.removeFooterView(loadingProgress);
			lv.removeAllViewsInLayout();
			AdapterDataSet dataset = stack.pop();
			// Log.e("adapter", ""+adapter);
			dataset.task.cancel(true);
			lv.setAdapter(dataset.adapter);
			lv.setOnItemClickListener(dataset.onItemClickListener);

		}

	}

	private String getJsonData(String url) {
		String json = null;

		try {
			json = http.oauthRequest(url);
		} catch (Exception e) {
			Log.e("get JsonData Failed", "err", e);
		}

		return json;
	}

	private boolean getExploreInfo() {

		String url = "http://moe.fm/explore?api=json&api_key=420f4049d93b1c64f5e811187ad3364c05016179a&new_musics=1&hot_musics=1&hot_radios=1&musics=1";
		String json = getJsonData(url);
		if (json == null) {
			return false;
		}

		Log.e("raw", json + "");
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
		vStatus.views = new ArrayList<View>();
		vStatus.views.add(getVExplore());
		vStatus.views.add(getVFavs());
		vStatus.views.add(getVSaved());
		PagerAdapter pa = new MyViewPagerAdapter(vStatus.views, titles);
		mViewPager.setAdapter(pa);

	}

	private View getVSaved() {

		// TODO Auto-generated method stub
		View view = null;

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

		if (c == null || c.getCount() == 0) {
			TextView t = (TextView) inflater.inflate(R.layout.big_text_item,
					null);
			t.setText("尚未保存歌曲到设备");
			view = t;
			c.close();
			
		} else {
			ListAdapter adapter = new MyCursorAdapter(getApplicationContext(),
					c, false);
			//使用cursoradapter切勿调用cursor.close();
			ListView lv = new ListView(this);
			lv.setAdapter(adapter);
			view = lv;

		}
		db.close();
		return view;
	}

	private View getVFavs() {
		// TODO Auto-generated method stub
		ListView listView = new ListView(this);

		String[] tags = new String[] { "收藏的专辑>>", "收藏的电台>>", "喜欢的歌曲>>" };
		ListAdapter adapter = new ArrayAdapter<String>(this,
				R.layout.big_text_item, tags);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(onMainCataClick);
		return listView;
	}

	private View getVExplore() {

		ListView listView = new ListView(this);

		String[] tags = new String[] { "音乐热榜>>>", "精选电台>>>", "魔力播放>>>" };
		ListAdapter adapter = new ArrayAdapter<String>(this,
				R.layout.big_text_item, tags);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(onMainCataClick);

		return listView;
	}

	private class Status {

		public List<View> views;
		public HashMap<String, List<SimpleData>> datas = new HashMap<String, List<SimpleData>>();
		public Stack<AdapterDataSet>[] adapterDatas = new Stack[] {
				new Stack<AdapterDataSet>(), new Stack<AdapterDataSet>(),
				new Stack<AdapterDataSet>() };

	}

	private class AdapterDataSet {
		public OnItemClickListener onItemClickListener;
		public ListAdapter adapter;
		public AsyncTask task;

	}

	private class OnWikiItemClick implements OnItemClickListener {
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			AdapterDataSet set = new AdapterDataSet();
			set.adapter = (ListAdapter) arg0.getAdapter();
			set.onItemClickListener = arg0.getOnItemClickListener();
			MyTask task = new MyTask();
			set.task = task;

			vStatus.adapterDatas[mViewPager.getCurrentItem()].push(set);
			SimpleData clickedData = vStatus.datas.get(
					arg0.getContentDescription()).get(arg2);
			int wikiId = clickedData.getId();
			String url = "http://api.moefou.org/music/subs.json?wiki_id="
					+ wikiId;

			loadingProgress.setVisibility(View.VISIBLE);
			task.execute(url, clickedData.getArtist(), vStatus.views.get(0));

		}
	}

	public class OnMainCataClick implements OnItemClickListener {

		public void onItemClick(AdapterView<?> arg0, View arg1, final int arg2,
				long arg3) {
			AdapterDataSet set = new AdapterDataSet();
			set.adapter = (ListAdapter) arg0.getAdapter();
			set.onItemClickListener = arg0.getOnItemClickListener();
			vStatus.adapterDatas[mViewPager.getCurrentItem()].push(set);

			arg0.setAdapter(null);
			AsyncTask task = new AsyncTask<Object, Void, Void>() {

				@Override
				protected Void doInBackground(Object... params) {
					while (vStatus.datas.size() == 0) {

					}
					return null;
				}

				protected void onPostExecute(Void result) {
					switch (mViewPager.getCurrentItem()) {
					case 0:
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
						break;
					case 1:
						switch (arg2) {
						case 0:

							break;
						case 1:

							break;
						case 2:

							break;
						default:
							break;
						}
						break;
					case 2:

						break;
					default:
						break;
					}
				};
			};
			set.task = task;
			task.execute("");

		}

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
			listView.setOnItemClickListener(onWikiClick);
			//
		}

	}

	public class OnSubItemClick implements OnItemClickListener {

		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub

		}

	}

	public class MyTask extends AsyncTask<Object, Void, Object[]> {
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			ListView lv = (ListView) vStatus.views.get(mViewPager
					.getCurrentItem());
			lv.addFooterView(loadingProgress);
			lv.setAdapter(null);
			super.onPreExecute();

		}

		@Override
		protected Object[] doInBackground(Object... params) {
			// TODO Auto-generated method stub

			String json = http.oauthRequest((String) params[0]);
			List<SimpleData> l = JSONUtils
					.getSubsList(json, (String) params[1]);
			ListAdapter adapter = new SimpleDataAdapter(MusicBrowse.this, l);

			Object[] res = new Object[] { params[2], adapter };
			return res;
		}

		@Override
		protected void onPostExecute(Object[] result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);

			ListView l = (ListView) result[0];
			ListAdapter adapter = (ListAdapter) result[1];
			l.setAdapter(adapter);
			l.removeFooterView(loadingProgress);

		}

	}

}
