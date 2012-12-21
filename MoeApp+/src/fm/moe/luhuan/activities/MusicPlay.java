package fm.moe.luhuan.activities;


import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

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

import android.graphics.Bitmap;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class MusicPlay extends Activity {

	public static final String PLAY_ACT_CREATE = "play act create";
	public static final String ACTION_RESUME = "resume playactivity";
	private SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");
	private MoeHttp moeHttp;
	private CommonHttpHelper commonHttp = new CommonHttpHelper();
	private SimpleData currentItem;
	private Handler mHandler = new Handler();
	private AsyncTask coverTask;
	private PlayService musicService;
	private LruCache<Integer, Bitmap> imageCache = new LruCache<Integer, Bitmap>(
			15);
	private LocalBroadcastManager broadcastManager;;
	private IntentFilter intentFilter = new IntentFilter();
	//widgets
	private Texts texts = new Texts();
	private Buttons buttons = new Buttons();
	private SeekBar seekBar;
	private ImageView albumCover;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player);
		initViews();
		broadcastManager = LocalBroadcastManager
				.getInstance(getApplicationContext());

		
		moeHttp = new MoeHttp(this);

		Intent intent = getIntent();

		Bundle bundle = intent.getExtras();
		if (bundle != null) {
			
			List<SimpleData> playList = (ArrayList<SimpleData>) bundle
					.get(PlayService.BUNDLE_KEY_PLAYLIST);
			int nowIndex = (Integer) bundle
					.get(PlayService.BUNDLE_KEY_SELECTED_INDEX);

			Intent serviceIntent = new Intent(this, PlayService.class);
			serviceIntent.putExtras(bundle);
			serviceIntent.setAction(PLAY_ACT_CREATE);
			startService(serviceIntent);

		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		Intent bindIntent = new Intent(this, PlayService.class);
		bindService(bindIntent, conn, BIND_AUTO_CREATE);
		broadcastManager.registerReceiver(broadcastReceiver, intentFilter);
		
	}

	@Override
	protected void onStop() {
		super.onStop();
		broadcastManager.unregisterReceiver(broadcastReceiver);
		mHandler.removeCallbacks(refreshPlayedTimeRunnable);
		unbindService(conn);
	}
	private void initViews(){
		intentFilter.addAction(PlayService.ACTION_PLAYER_STATE_CHANGE);
		intentFilter.addAction(DownloadService.ACTION_DOWNLOAD_STATE_CHANGE);
		texts.bindView();
		buttons.bindView();
		seekBar = (SeekBar) findViewById(R.id.playing_seekbar);
		seekBar.setOnSeekBarChangeListener(seekPos);
		albumCover = (ImageView) findViewById(R.id.big_cover);
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);

		albumCover.setLayoutParams(new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, dm.widthPixels));
	}
	private void setStaticView() {
		currentItem = musicService.playList.get(musicService.nowIndex);
		// Log.e("parent id", currentItem.getParentId() + "");
		texts.title.setText(currentItem.getTitle());
		texts.artist.setText(currentItem.getArtist());
		if (currentItem.isFav()) {
			buttons.fav.setImageDrawable(getResources().getDrawable(
					android.R.drawable.btn_star_big_on));
		} else {
			buttons.fav.setImageDrawable(getResources().getDrawable(
					android.R.drawable.btn_star_big_off));
		}
		if (currentItem.getArtist() == null
				|| currentItem.getArtist().equals("")) {
			texts.artist.setText("未知艺术家");
		}
		if (musicService.player.isPlaying()) {
			buttons.pp.setImageDrawable(getResources().getDrawable(
					android.R.drawable.ic_media_pause));
		} else {
			buttons.pp.setImageDrawable(getResources().getDrawable(
					android.R.drawable.ic_media_play));
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
					Bitmap bm = musicService.fileHelper
							.getItemCoverBitmap(currentItem);
					// Log.e("loading bitmap", albumnCoverUrl);
					if (bm == null) {
						bm = commonHttp.getBitmap(albumnCoverUrl);

					}

					imageCache.put(currentItem.getId(), bm);
					return bm;
				}

				@Override
				protected void onPostExecute(Bitmap result) {
					super.onPostExecute(result);
					albumCover.setImageBitmap(result);

				}
			};
			coverTask.execute("");

		}

	}

	private class Texts {
		public TextView title;
		public TextView artist;
		public TextView album;
		public TextView played;
		public TextView fullTime;

		public void bindView() {
			title = (TextView) findViewById(R.id.playing_song_title);
			artist = (TextView) findViewById(R.id.playing_song_artist);
			album = (TextView) findViewById(R.id.playing_song_albumn);
			played = (TextView) findViewById(R.id.time_played);
			fullTime = (TextView) findViewById(R.id.time_full);
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
			//onbind = false;
		}

		public void onServiceConnected(ComponentName name, IBinder service) {
			//onbind = true;
			musicService = ((PlayerBinder) service).getService();
			
			setStaticView();
			mHandler.post(refreshPlayedTimeRunnable);

		}
	};
	private OnClickListener onButtonsClick = new OnClickListener() {
		private Timer timer = new Timer();
		private TimerTask lastClickTask;
		// private int lastClickTime = 0;
		private int clickCount = 0;

		public void onClick(View v) {

			switch (v.getId()) {
			case R.id.song_save:
				if (musicService == null) {
					return;
				}
				if (musicService.fileHelper.isItemSaved(currentItem)) {
					Toast.makeText(MusicPlay.this, "该歌曲已下载", Toast.LENGTH_SHORT)
							.show();
				} else {
					Intent downloadIntent = new Intent(getApplicationContext(),
							DownloadService.class);
					downloadIntent.putExtra(DownloadService.EXTRA_SONG_ITEM,
							currentItem);
					startService(downloadIntent);
				}

				break;
			case R.id.ib_pp:
				if (musicService == null) {
					return;
				}
				if (musicService.player.isPlaying()) {
					musicService.player.pause();
					((ImageButton) v).setImageDrawable(getResources()
							.getDrawable(android.R.drawable.ic_media_play));
				} else if(musicService.isPrepared){
					musicService.player.start();
					((ImageButton) v).setImageDrawable(getResources()
							.getDrawable(android.R.drawable.ic_media_pause));
				}
				break;
			case R.id.ib_next:
				doChangeSong(true);
				break;
			case R.id.ib_prev:
				doChangeSong(false);
				break;
			default:
				break;
			}

		}

		private void doChangeSong(final boolean isNext) {
			if (lastClickTask != null) {
				lastClickTask.cancel();
			}
			if (isNext) {
				clickCount++;
			} else {
				clickCount--;
			}
			lastClickTask = new TimerTask() {

				@Override
				public void run() {

					int tarIndex = musicService.nowIndex + clickCount;
					if (tarIndex < 0) {
						tarIndex = 0;
					} else if (tarIndex > musicService.playList.size() - 1) {
						tarIndex = musicService.playList.size() - 1;
					}
					musicService.playSongAtIndex(tarIndex);
					clickCount = 0;
					mHandler.removeCallbacks(refreshPlayedTimeRunnable);
					mHandler.post(setStaticViewRunnable);
					
				}
			};
			timer.schedule(lastClickTask, 300);

		}
	};
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			// Log.e("action", action);
			if (action.equals(PlayService.ACTION_PLAYER_STATE_CHANGE)) {
				onPlayerStateChange(intent);
			}

			if (action.equals(DownloadService.ACTION_DOWNLOAD_STATE_CHANGE)) {
				onDownloadStateChange(intent);
			}

		}

		private void onDownloadStateChange(Intent intent) {
			switch (intent.getIntExtra(DownloadService.EXTRA_DOWNLOAD_STATE, 0)) {
			case 0:
				Toast.makeText(MusicPlay.this, "已开始下载", Toast.LENGTH_SHORT)
						.show();
				break;
			case 1:
				Toast.makeText(MusicPlay.this, "下载已完成", Toast.LENGTH_SHORT)
						.show();
				break;
			case -1:
				String info = intent
						.getStringExtra(DownloadService.EXTRA_CONN_PROBLEM_INFO);
				Toast.makeText(MusicPlay.this, info + ",下载已暂停",
						Toast.LENGTH_SHORT).show();
				break;

			default:
				break;
			}
		}

		private void onPlayerStateChange(Intent intent) {
			switch (intent.getIntExtra(PlayService.EXTRA_PLAYER_STATE, 0)) {
			case 0:// for prepared
				buttons.pp.setImageDrawable(getResources().getDrawable(
						android.R.drawable.ic_media_pause));
				texts.fullTime.setText(dateFormat.format(musicService.player
						.getDuration()));
				mHandler.post(refreshPlayedTimeRunnable);
				//isPlayerPrepared = true;
				break;
			case 1:// for completion
				setStaticView();
				mHandler.removeCallbacks(refreshPlayedTimeRunnable);
				mHandler.post(resetTimeinfoRunnable);
				break;
			case 2:// for buffer update
				seekBar.setSecondaryProgress(intent.getIntExtra(
						PlayService.EXTRA_PLAYER_BUFFERED_PERCENT, 0));
				break;
			case -1:// for err
				break;
			default:
				break;
			}
		}
	};
	private Runnable setStaticViewRunnable = new Runnable() {

		public void run() {
			setStaticView();
		}
	};
	private Runnable refreshPlayedTimeRunnable = new Runnable() {

		public void run() {
			if(musicService.player.isPlaying()){
				texts.played.setText(dateFormat.format(musicService.player
						.getCurrentPosition()));
				if(!seekBar.isPressed()){
					seekBar.setProgress(musicService.player.getCurrentPosition()*100
							/ musicService.player.getDuration());
				}
				
			}
			
			//Log.e("progress", "current="+musicService.player.getCurrentPosition()+",total="+ musicService.player.getDuration());
			mHandler.postDelayed(this, 1000);
		}
	};
	private Runnable resetTimeinfoRunnable = new Runnable() {
		
		public void run() {
			texts.played.setText("00:00");
			texts.fullTime.setText("00:00");
			seekBar.setProgress(0);
			seekBar.setSecondaryProgress(0);
		}
	};
	private OnSeekBarChangeListener seekPos = new OnSeekBarChangeListener() {
		
		public void onStopTrackingTouch(SeekBar seekBar) {
			if(musicService!=null&&musicService.isPrepared){
				musicService.player.seekTo(seekBar.getProgress() * musicService.player.getDuration() / 100);
			}
		}
		
		public void onStartTrackingTouch(SeekBar seekBar) {
			
		}
		
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			
			
		}
	};
}
