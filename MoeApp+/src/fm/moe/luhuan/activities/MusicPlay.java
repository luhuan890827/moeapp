package fm.moe.luhuan.activities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.scribe.model.OAuthRequest;

import com.alibaba.fastjson.JSON;

import fm.moe.luhuan.FileStorageHelper;
import fm.moe.luhuan.JSONUtils;
import fm.moe.luhuan.MoeDbHelper;
import fm.moe.luhuan.R;
import fm.moe.luhuan.beans.data.SimpleData;
import fm.moe.luhuan.http.MoeHttp;
import fm.moe.luhuan.service.PlayService;
import fm.moe.luhuan.service.PlayService.PlayerBinder;
import android.R.integer;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class MusicPlay extends Activity {
	

	public static final String PLAY_ACT_CREATE = "play act create";
	
	private MoeHttp http;
	private MoeDbHelper dbHelper;
	private Texts texts = new Texts();
	private Buttons buttons = new Buttons();
	
	private ImageView albumCover;
//	private ArrayList<SimpleData> playList;
//	private String playListId;
//	private int nowIndex = -1;
	private SimpleData currentItem;
	private Map<Integer, SimpleData> parentWikis = new HashMap<Integer, SimpleData>();
	private AsyncTask coverTask;
	private PlayService musicService;
	private boolean onbind = false;
	private LruCache<Integer, Bitmap> imageCache = new LruCache<Integer, Bitmap>(1024*1024*4);
	private FileStorageHelper fileHelper;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player);
		texts.bindView();
		buttons.bindView();
		albumCover = (ImageView) findViewById(R.id.big_cover);
		http = new MoeHttp();
		fileHelper = new FileStorageHelper(getApplicationContext());
		SharedPreferences pref = getSharedPreferences("token", MODE_PRIVATE);
		// 若token未设置成功（例如access_token为空）,则进行httprequest时将直接返回NULL
		http.setToken(pref.getString("access_token", ""),
				pref.getString("access_secret", ""));

		dbHelper = new MoeDbHelper(this);

		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();

		List<SimpleData> playList = (ArrayList<SimpleData>) bundle.get(PlayService.BUNDLE_KEY_PLAYLIST);
		int nowIndex = (Integer) bundle.get(PlayService.BUNDLE_KEY_SELECTED_INDEX);
		currentItem = playList.get(nowIndex);
		//playListId = (String) bundle.get(BUNDLE_KEY_PLAYLIST_ID);
		
		Intent serviceIntent = new Intent(this,PlayService.class);
		serviceIntent.setAction(PLAY_ACT_CREATE);
		bindService(serviceIntent, conn, BIND_AUTO_CREATE);
		serviceIntent.putExtras(bundle);
		//need to explicitly call startservice ,otherwise the onstartcommand() won't be called
		startService(serviceIntent);
		setStaticView();
	}

	private void setStaticView() {
		
		Log.e("parent id", currentItem.getParentId()+"");
		texts.title.setText(currentItem.getTitle());
		texts.artist.setText(currentItem.getArtist());
		if(currentItem.isFav()){
			buttons.fav.setImageDrawable(getResources().getDrawable(android.R.drawable.btn_star_big_on));
		}
		if(currentItem.getArtist()==null||currentItem.getArtist().equals("")){
			texts.artist.setText("未知艺术家");
		}
		texts.album.setText(currentItem.getParentTitle());
		loadCover(currentItem.getAlbumnCoverUrl());
		

	}

	private void loadCover(final String albumnCoverUrl) {
		if(coverTask!=null){
			coverTask.cancel(true);
		}
		
		Bitmap bm = imageCache.get(currentItem.getId());
		if(bm!=null){
			albumCover.setImageBitmap(bm);
		}else{
			coverTask = new AsyncTask<Object, Integer, Bitmap>() {

				@Override
				protected Bitmap doInBackground(Object... params) {
					// TODO Auto-generated method stub
					Bitmap bm = null;
					//Log.e("loading bitmap", albumnCoverUrl);
					try {
						bm = http.getBitmap(albumnCoverUrl);
					}  catch (IOException e) {
						// TODO Auto-generated catch block
						Log.e("load cover err", "",e);
						e.printStackTrace();
					}
					imageCache.put(currentItem.getId(), bm);
					return bm;
				}
				@Override
				protected void onPostExecute(Bitmap result) {
					// TODO Auto-generated method stub
					super.onPostExecute(result);
					albumCover.setImageBitmap(result);
					
				}
			};
			coverTask.execute("");
		}
		
		
	}

	private SimpleData getSongDetail(int id) {

		return null;
	}

	

	private class Texts {
		public TextView title;
		public TextView artist;
		public TextView album;
		public TextView played;
		public TextView remain;

		public void bindView() {
			title = (TextView) findViewById(R.id.playing_song_title);
			artist = (TextView) findViewById(R.id.playing_song_artist);
			album = (TextView) findViewById(R.id.playing_song_albumn);
			played = (TextView) findViewById(R.id.time_played);
			remain = (TextView) findViewById(R.id.time_remain);
		}
	}

	private class Buttons {
		public ImageButton showList;
		public ImageButton fav;
		public ImageButton down;
		public ImageButton share;
		public ImageButton prev;
		public ImageButton pp;
		public ImageButton next;

		public void bindView() {
			showList = (ImageButton) findViewById(R.id.player_view_switch);
			fav = (ImageButton) findViewById(R.id.song_fav);
			down = (ImageButton) findViewById(R.id.song_save);
			share = (ImageButton) findViewById(R.id.song_share);
			prev = (ImageButton) findViewById(R.id.ib_prev);
			pp = (ImageButton) findViewById(R.id.ib_pp);
			next = (ImageButton) findViewById(R.id.ib_next);
			
			

		}

	}
	private ServiceConnection conn = new ServiceConnection() {
		
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			onbind = false;
		}
		
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			onbind = true;
			musicService =((PlayerBinder)service).getService();
			
		}
	};
	private OnClickListener onButtonsClick=new OnClickListener() {
		
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.song_save:
				
				break;

			default:
				break;
			}
			
		}
	};
}
