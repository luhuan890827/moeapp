package fm.moe.luhuan.activities;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import fm.moe.luhuan.JSONUtils;
import fm.moe.luhuan.R;
import fm.moe.luhuan.adapters.SimpleDataAdapter;
import fm.moe.luhuan.beans.data.SimpleData;
import fm.moe.luhuan.http.MoeHttp;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.ListView;

public class SearchableActivity extends Activity {
	private MoeHttp http;
	private Handler mHandler = new Handler();
	private ListView listView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_result);
		Intent intent = getIntent();
		http = new MoeHttp(this);
		listView = (ListView) findViewById(R.id.result_album_list);
		
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			final String query = intent.getStringExtra(SearchManager.QUERY);
			
			new Thread(){
				public void run(){
					String keyword = null;
					try {
						keyword = URLEncoder.encode(query, "utf8");
						
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					
					String json_album = http.oauthRequest("http://api.moefou.org/search/wiki.json?wiki_type=music&keyword="+keyword);
					
					String json_song = http.oauthRequest("http://api.moefou.org/search/sub.json?sub_type=song&keyword="+keyword);
					final List<SimpleData>list = new ArrayList<SimpleData>();
					List<SimpleData> albumList = JSONUtils.getWikiList(json_album);
					List<SimpleData> songList = JSONUtils.getSublist(json_song);
					SimpleData data1 = new SimpleData();
					data1.setId(-1);
					data1.setTitle("×¨¼­");
					SimpleData data2 = new SimpleData();
					data2.setId(-1);
					data2.setTitle("ÇúÄ¿");
					list.add(data1);
					list.addAll(albumList);
					list.add(data2);
					list.addAll(songList);
					mHandler.post(new Runnable() {
						
						public void run() {
							SimpleDataAdapter adapter = new SimpleDataAdapter(SearchableActivity.this, list);
							listView.setAdapter(adapter);
							
							findViewById(R.id.result_progress).setVisibility(View.GONE);
						}
					});
				};
				
			}.start();
		}
	}
}
