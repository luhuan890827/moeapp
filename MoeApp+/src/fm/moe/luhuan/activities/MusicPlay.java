package fm.moe.luhuan.activities;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.List;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import fm.moe.luhuan.R;
import fm.moe.luhuan.adapters.SimpleDataAdapter;
import fm.moe.luhuan.beans.data.SimpleData;
import fm.moe.luhuan.http.CommonHttpHelper;
import fm.moe.luhuan.http.MoeHttp;
import fm.moe.luhuan.service.DownloadService;
import fm.moe.luhuan.service.PlayService;
import fm.moe.luhuan.service.PlayService.PlayerBinder;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;

import android.graphics.Bitmap;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.LruCache;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.AdapterView.OnItemClickListener;
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
//	private LruCache<Integer, Bitmap> imageCache = new LruCache<Integer, Bitmap>(
//			15);
	private SparseArray<Bitmap> imageCache = new SparseArray<Bitmap>();
	private LocalBroadcastManager broadcastManager;;
	private IntentFilter intentFilter = new IntentFilter();
	private SharedPreferences pref;
	// widgets
	private Texts texts = new Texts();
	private Buttons buttons = new Buttons();
	private SeekBar seekBar;
	private ImageView albumCover;
	private ListView listView;
	private RelativeLayout songView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.player);
		// Log.e("music play", "create");
		initViews();
		listView = (ListView) findViewById(R.id.player_list_view);
		songView = (RelativeLayout) findViewById(R.id.player_song_view);
		broadcastManager = LocalBroadcastManager
				.getInstance(getApplicationContext());

		setBackTab();
		moeHttp = new MoeHttp(this);

		Intent intent = getIntent();

		Bundle bundle = intent.getExtras();
		if (bundle != null) {

			List<SimpleData> playList = (ArrayList<SimpleData>) bundle
					.get(PlayService.EXTRA_PLAYLIST);
			if (bundle.getBoolean(PlayService.EXTRA_IF_NEED_NETWORK)
					&& getSharedPreferences("App_settings", MODE_PRIVATE)
							.getBoolean(
									getResources()
											.getString(
													fm.moe.luhuan.R.string.pref_key_play_only_on_wifi),
									false)) {
				// to do
			}
			((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
					.cancel(PlayService.NOTIFICATION_ID);

			Intent serviceIntent = new Intent(this, PlayService.class);
			serviceIntent.putExtras(bundle);
			serviceIntent.setAction(PLAY_ACT_CREATE);
			startService(serviceIntent);

		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		// ((NotificationManager)
		// getSystemService(Context.NOTIFICATION_SERVICE)).cancelAll();
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

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			this.onBackPressed();
			break;

		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(this, MusicBrowse.class);
		startActivity(intent);
		super.onBackPressed();

	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setBackTab() {
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

	}

	private void initViews() {
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
		if (musicService.isPrepared) {
			texts.fullTime.setText(dateFormat.format(musicService.player
					.getDuration()));
			texts.played.setText(dateFormat.format(musicService.player
					.getCurrentPosition()));
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
			artist.getPaint().setUnderlineText(true);
			album.getPaint().setUnderlineText(true);
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
			// onbind = false;
		}

		public void onServiceConnected(ComponentName name, IBinder service) {
			musicService = ((PlayerBinder) service).getService();
			if (listView.getAdapter() == null) {
				SimpleDataAdapter adapter = new SimpleDataAdapter(getApplicationContext(), musicService.playList);
				listView.setAdapter(adapter);
				listView.setOnItemClickListener(onListViewClick);
			}
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
				downloadSong();
				break;
			case R.id.ib_pp:
				switchPlayPause(v);
				break;
			case R.id.ib_next:
				doChangeSong(true);
				break;
			case R.id.ib_prev:
				doChangeSong(false);
				break;
			case R.id.song_share:
				shareSong();
				break;
			case R.id.song_fav:
				switchSongFav();
				break;
			case R.id.player_view_switch:
				switchPlayerView();
				break;
			default:
				break;
			}

		}

		private void switchPlayerView() {
			if (songView.getVisibility() != View.VISIBLE) {
				songView.setVisibility(View.VISIBLE);
				listView.setVisibility(View.GONE);
			} else {
				songView.setVisibility(View.GONE);
				listView.setVisibility(View.VISIBLE);
			}
		}

		private void switchSongFav() {
			// TODO Auto-generated method stub

		}

		private void shareSong() {
			Intent shareIntent = new Intent(Intent.ACTION_SEND);
			
			//shareIntent.putExtra(Intent.EXTRA_TEXT, "test");
			try {
				Bitmap bm = imageCache.get(currentItem.getId());
				String imageUrl = musicService.fileHelper.getTempImageUri(bm);
				Uri uri = Uri.fromFile(new File(imageUrl));
				shareIntent.putExtra(Intent.EXTRA_STREAM,uri);
				shareIntent.setType("image/jpeg");
				shareIntent.putExtra(Intent.EXTRA_TEXT, "http://moe.fm/song/"+currentItem.getId());
			} catch (IOException e) {
				Log.e("", "",e);
				e.printStackTrace();
			}
			
			
			startActivity(Intent.createChooser(shareIntent, "分享到..."));
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
					mHandler.post(resetTimeinfoRunnable);

				}
			};
			timer.schedule(lastClickTask, 300);

		}

		private void switchPlayPause(View v) {
			if (musicService == null) {
				return;
			}
			if (musicService.player.isPlaying()) {
				musicService.player.pause();
				mHandler.removeCallbacks(refreshPlayedTimeRunnable);
				musicService.sendNotification(
						android.R.drawable.ic_media_pause, "播放已暂停", "播放已暂停",
						currentItem.getTitle());
				((ImageButton) v).setImageDrawable(getResources().getDrawable(
						android.R.drawable.ic_media_pause));

			} else if (musicService.isPrepared) {
				mHandler.post(refreshPlayedTimeRunnable);
				musicService.player.start();
				musicService.sendNotification(android.R.drawable.ic_media_play,
						"正在播放", "正在播放", currentItem.getTitle());
				((ImageButton) v).setImageDrawable(getResources().getDrawable(
						android.R.drawable.ic_media_play));
			}
		}

		private void downloadSong() {
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
				// isPlayerPrepared = true;
				break;
			case 1:// for completion
				setStaticView();
				mHandler.removeCallbacks(refreshPlayedTimeRunnable);
				if (!(musicService.playList.size() - 1 == musicService.nowIndex)) {
					mHandler.post(resetTimeinfoRunnable);
				}

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
			if (musicService.player.isPlaying()) {
				texts.played.setText(dateFormat.format(musicService.player
						.getCurrentPosition()));
				if (!seekBar.isPressed()) {
					seekBar.setProgress(musicService.player
							.getCurrentPosition()
							* 100
							/ musicService.player.getDuration());
				}

			}

			// Log.e("progress",
			// "current="+musicService.player.getCurrentPosition()+",total="+
			// musicService.player.getDuration());
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
			if (musicService != null && musicService.isPrepared
					&& musicService.bufferedPercent == 100) {
				musicService.player.seekTo(seekBar.getProgress()
						* musicService.player.getDuration() / 100);
			}
		}

		public void onStartTrackingTouch(SeekBar seekBar) {

		}

		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {

		}
	};
	private OnItemClickListener onListViewClick = new OnItemClickListener() {

		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			if(musicService!=null){
				musicService.playSongAtIndex(arg2);
			}
		}
	};
}
