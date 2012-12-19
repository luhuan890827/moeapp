package fm.moe.luhuan.activities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fm.moe.luhuan.FileStorageHelper;

import fm.moe.luhuan.MoeDbHelper;
import fm.moe.luhuan.R;
import fm.moe.luhuan.beans.data.SimpleData;
import fm.moe.luhuan.http.CommonHttpHelper;
import fm.moe.luhuan.http.MoeHttp;
import fm.moe.luhuan.service.DownloadService;
import fm.moe.luhuan.service.PlayService;
import fm.moe.luhuan.service.PlayService.PlayerBinder;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.LruCache;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MusicPlay extends Activity {

	public static final String PLAY_ACT_CREATE = "play act create";
	public static final String ACTION_RESUME = "resume playactivity";
	private MoeHttp moeHttp;
	private CommonHttpHelper commonHttp;
	private MoeDbHelper dbHelper;
	private Texts texts = new Texts();
	private Buttons buttons = new Buttons();

	private ImageView albumCover;
	// private ArrayList<SimpleData> playList;
	// private String playListId;
	// private int nowIndex = -1;
	private SimpleData currentItem;
	private Map<Integer, SimpleData> parentWikis = new HashMap<Integer, SimpleData>();
	private AsyncTask coverTask;
	private PlayService musicService;
	private boolean onbind = false;
	private LruCache<Integer, Bitmap> imageCache = new LruCache<Integer, Bitmap>(
			15);
	private FileStorageHelper fileHelper;
	private LocalBroadcastManager broadcastManager;
	private boolean isPlayerPrepared = false;
	private IntentFilter intentFilter = new IntentFilter();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player);

		broadcastManager = LocalBroadcastManager
				.getInstance(getApplicationContext());

		intentFilter.addAction(PlayService.ACTION_PLAYER_PREPARED);
		intentFilter.addAction(PlayService.ACTION_PLAYER_BUFFERED_UPDATE);
		intentFilter.addAction(PlayService.ACTION_PLAYER_ERR);
		intentFilter.addAction(PlayService.ACTION_PLAYER_COMPLETION);
		intentFilter.addAction(DownloadService.ACTION_NET_CONN_PROBLEM);
		intentFilter.addAction(ACTION_RESUME);
		texts.bindView();
		buttons.bindView();
		albumCover = (ImageView) findViewById(R.id.big_cover);
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);

		albumCover.setLayoutParams(new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, dm.widthPixels));
		moeHttp = new MoeHttp(this);
		commonHttp = new CommonHttpHelper();
		fileHelper = new FileStorageHelper(getApplicationContext());
		SharedPreferences pref = getSharedPreferences("token", MODE_PRIVATE);
		// 若token未设置成功（例如access_token为空）,则进行httprequest时将直接返回NULL

		dbHelper = new MoeDbHelper(this);

		Intent intent = getIntent();
		
		Bundle bundle = intent.getExtras();
		if(bundle!=null){
			List<SimpleData> playList = (ArrayList<SimpleData>) bundle
					.get(PlayService.BUNDLE_KEY_PLAYLIST);
			int nowIndex = (Integer) bundle
					.get(PlayService.BUNDLE_KEY_SELECTED_INDEX);
			currentItem = playList.get(nowIndex);

			Intent serviceIntent = new Intent(this, PlayService.class);
			serviceIntent.putExtras(bundle);
			serviceIntent.setAction(PLAY_ACT_CREATE);

			// shoud figure the logic partition between bindService and startService
			// in my opinion,call bindService only to get the instance of the
			// service,
			// call startService to actually start your task

			// // need to explicitly call startservice ,otherwise the
			// onstartcommand()
			// // won't be called
			startService(serviceIntent);
			setStaticView();
		}
		
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Intent bindIntent = new Intent(this, PlayService.class);
		bindService(bindIntent, conn, BIND_AUTO_CREATE);

		broadcastManager.registerReceiver(broadcastReceiver, intentFilter);
		// registerReceiver(new BroadcastReceiver() {
		//
		// @Override
		// public void onReceive(Context context, Intent intent) {
		// // TODO Auto-generated method stub
		// Log.e("action", intent.getAction());
		// }
		// }, new IntentFilter());
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		broadcastManager.unregisterReceiver(broadcastReceiver);
		unbindService(conn);
	}

	private void setStaticView() {

		// Log.e("parent id", currentItem.getParentId() + "");
		texts.title.setText(currentItem.getTitle());
		texts.artist.setText(currentItem.getArtist());
		if (currentItem.isFav()) {
			buttons.fav.setImageDrawable(getResources().getDrawable(
					android.R.drawable.btn_star_big_on));
		}
		if (currentItem.getArtist() == null
				|| currentItem.getArtist().equals("")) {
			texts.artist.setText("未知艺术家");
		}
		texts.album.setText(currentItem.getParentTitle());
		loadCover(currentItem.getAlbumnCoverUrl());

	}

	private void loadCover(final String albumnCoverUrl) {
		if (coverTask != null) {
			coverTask.cancel(true);
		}

		Bitmap bm = imageCache.get(currentItem.getId());
		if (bm != null) {
			albumCover.setImageBitmap(bm);
		} else {
			coverTask = new AsyncTask<Object, Integer, Bitmap>() {

				@Override
				protected Bitmap doInBackground(Object... params) {
					// TODO Auto-generated method stub
					Bitmap bm = null;
					// Log.e("loading bitmap", albumnCoverUrl);

					bm = commonHttp.getBitmap(albumnCoverUrl);

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

			showList.setOnClickListener(onButtonsClick);
			fav.setOnClickListener(onButtonsClick);
			down.setOnClickListener(onButtonsClick);
			share.setOnClickListener(onButtonsClick);
			prev.setOnClickListener(onButtonsClick);
			pp.setOnClickListener(onButtonsClick);
			next.setOnClickListener(onButtonsClick);

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
			musicService = ((PlayerBinder) service).getService();

		}
	};
	private OnClickListener onButtonsClick = new OnClickListener() {

		public void onClick(View v) {

			switch (v.getId()) {
			case R.id.song_save:
				Intent downloadIntent = new Intent(getApplicationContext(),
						DownloadService.class);
				downloadIntent.putExtra(DownloadService.DOWNLOAD_URL_KEY,
						currentItem.getMp3Url());
				downloadIntent.putExtra(DownloadService.ITEM_ID_KEY,
						currentItem.getId() + "");
				startService(downloadIntent);
				break;
			case R.id.ib_pp:
				if (musicService == null || !isPlayerPrepared) {
					return;
				}
				if (musicService.player.isPlaying()) {
					musicService.player.pause();
					((ImageButton) v).setImageDrawable(getResources()
							.getDrawable(android.R.drawable.ic_media_play));
				} else {
					musicService.player.start();
					((ImageButton) v).setImageDrawable(getResources()
							.getDrawable(android.R.drawable.ic_media_pause));
				}

			default:
				break;
			}

		}
	};
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			// Log.e("action", action);
			if (action.equals(PlayService.ACTION_PLAYER_PREPARED)) {
				musicService.player.start();
				buttons.pp.setImageDrawable(getResources().getDrawable(
						android.R.drawable.ic_media_pause));
				isPlayerPrepared = true;
			}
			if (action.equals(DownloadService.ACTION_NET_CONN_PROBLEM)) {
				Toast.makeText(
						MusicPlay.this,
						intent.getStringExtra(DownloadService.EXTRA_CONN_PROBLEM_INFO),
						Toast.LENGTH_SHORT).show();
			}
			if(action.equals(ACTION_RESUME)){
				onResume();
			}

		}
	};
}
