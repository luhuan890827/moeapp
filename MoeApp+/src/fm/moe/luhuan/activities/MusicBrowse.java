package fm.moe.luhuan.activities;

import java.util.ArrayList;
import java.util.Currency;
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
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MusicBrowse extends Activity {
	private ViewPager mViewPager;
	private Status vStatus = new Status();
	private LayoutInflater inflater;
	private RelativeLayout mainWrapper;
	private ProgressBar loadingProgress;
	private TextView loadingMore;
	private MoeOauth http;
	// private Object lock = new Object();
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
		// 若token未设置成功（例如access_token为空）,则进行httprequest时将直接返回NULL
		http.setToken(pref.getString("access_token", ""),
				pref.getString("access_secret", ""));

		// getExploreInfo();
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

		//Log.e("raw", json + "");
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
		ArrayList<View> wrappers = new ArrayList<View>();
		wrappers.add(setVExplore());
		wrappers.add(setVFavs());
		wrappers.add(setVSaved());

		PagerAdapter pa = new MyViewPagerAdapter(wrappers, titles);
		mViewPager.setAdapter(pa);

	}

	private View setVSaved() {

		// TODO Auto-generated method stub
		LinearLayout ll = (LinearLayout) inflater.inflate(
				R.layout.list_wrapper, null);

		ListView listView = (ListView) ll.findViewById(R.id.wrapped_list);
		vStatus.views.add(listView);

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

	private View setVFavs() {
		// TODO Auto-generated method stub
		LinearLayout ll = (LinearLayout) inflater.inflate(
				R.layout.list_wrapper, null);

		ListView listView = (ListView) ll.findViewById(R.id.wrapped_list);
		vStatus.views.add(listView);
		// TextView tv = new TextView(this);

		String[] tags = new String[] { "收藏的专辑>>", "收藏的电台>>", "喜欢的歌曲>>" };
		ListAdapter adapter = new ArrayAdapter<String>(this,
				R.layout.big_text_item, tags);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(onMainCataClick);

		return ll;
	}

	private View setVExplore() {
		LinearLayout ll = (LinearLayout) inflater.inflate(
				R.layout.list_wrapper, null);

		ListView listView = (ListView) ll.findViewById(R.id.wrapped_list);
		vStatus.views.add(listView);
		String[] tags = new String[] { "音乐热榜>>>", "精选电台>>>", "魔力播放>>>" };
		ListAdapter adapter = new ArrayAdapter<String>(this,
				R.layout.big_text_item, tags);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(onMainCataClick);

		return ll;
	}

	private class Status {

		public List<View> views = new ArrayList<View>();
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
			String type = (String) arg0.getTag();
			set.adapter = (ListAdapter) arg0.getAdapter();
			set.onItemClickListener = arg0.getOnItemClickListener();
			WikiTask task = new WikiTask();
			set.task = task;

			vStatus.adapterDatas[mViewPager.getCurrentItem()].push(set);
//			SimpleData clickedData = vStatus.datas.get(
//					arg0.getContentDescription()).get(arg2);
//			int wikiId = clickedData.getId();
			String url = null;
			url = "http://moe.fm/listen/playlist?api=json&"+type+"="+arg1.findViewById(R.id.item_title).getTag(R.string.item_id);
//			if (type.equals("music")) {
//				url = "http://api.moefou.org/music/subs.json?wiki_id=" + wikiId;
//			} else if (type.equals("radio")) {
//				url = "http://moe.fm/listen/playlist?api=json&radio=" + wikiId;
//			}

			task.execute(url, "some data", vStatus.views.get(mViewPager.getCurrentItem()));

		}
	}

	public class OnMainCataClick implements OnItemClickListener {

		public void onItemClick(final AdapterView<?> arg0, View arg1,
				final int arg2, long arg3) {
			AdapterDataSet set = new AdapterDataSet();
			ListView listView = (ListView) arg0;
			set.adapter = listView.getAdapter();
			set.onItemClickListener = arg0.getOnItemClickListener();
			vStatus.adapterDatas[mViewPager.getCurrentItem()].push(set);
			listView.addFooterView(loadingProgress);
			arg0.setAdapter(null);

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
			ListView listView = (ListView) vStatus.views.get(1);

			FavTask task = new FavTask();
			vStatus.adapterDatas[1].peek().task = task;

			final String url = "http://api.moefou.org/user/favs/sub.json?obj_type=song&fav_type=1&perpage=25";
			task.execute(url, "sub","single");

		}

		private void showFavRadios() {
			ListView listView = (ListView) vStatus.views.get(1);

			FavTask task = new FavTask();
			vStatus.adapterDatas[1].peek().task = task;

			final String url = "http://api.moefou.org/user/favs/wiki.json?obj_type=radio&fav_type=1&perpage=25";
			task.execute(url, "wiki","radio");
		}

		private void showFavAlbums() {
			// TODO Auto-generated method stub
			ListView listView = (ListView) vStatus.views.get(1);
			
			FavTask task = new FavTask();
			vStatus.adapterDatas[1].peek().task = task;

			final String url = "http://api.moefou.org/user/favs/wiki.json?obj_type=music&fav_type=1&perpage=25";
			task.execute(url, "wiki","music");

		}

		private void clickAtMainExplore(final int arg2) {
			final ListView listView = (ListView) vStatus.views.get(0);
			AsyncTask task = new AsyncTask<Object, Void, Boolean>() {

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
					listView.removeFooterView(loadingProgress);

					if (!success) {
						Toast.makeText(getApplicationContext(), "网络似乎有问题哦",
								Toast.LENGTH_SHORT).show();
					} else {
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

	}

	public class OnSubItemClick implements OnItemClickListener {

		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub

		}

	}

	public class WikiTask extends AsyncTask<Object, Void, Object[]> {
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
			List<SimpleData> l = null;
			//if (url.indexOf("playlist") > 0) {
				l = JSONUtils.getSimpleDataFromPlayList(json);
//			} else {
//				l = JSONUtils.getSubsList(json, (String) params[1]);
//			}

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

	public class FavTask extends AsyncTask<String, Void, Object[]> {
		@Override
		protected Object[] doInBackground(String... params) {
			// TODO Auto-generated method stub
			String json = http.oauthRequest(params[0]);
			List<SimpleData> list = JSONUtils.getFavs(json, params[1]);
			ListAdapter adapter = new SimpleDataAdapter(MusicBrowse.this, list);
			Object[] res = new Object[]{adapter,params[1],params[2]};
			return res;
		}

		@Override
		protected void onPostExecute(Object[] result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			ListView listView = (ListView) vStatus.views.get(mViewPager
					.getCurrentItem());
			listView.setAdapter((ListAdapter) result[0]);
			if(((String)result[1]).equals("wiki")){
				
				listView.setOnItemClickListener(onWikiClick);
			}else{
				
				listView.setOnItemClickListener(onSubClick);
			}
			listView.setTag(result[2]);
			listView.removeFooterView(loadingProgress);
			
			
		}

	}
	private Handler mMessageHandler = new Handler(){};

}
