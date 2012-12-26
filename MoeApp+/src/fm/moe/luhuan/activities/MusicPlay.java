package fm.moe.luhuan.activities;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.alibaba.fastjson.JSON;
import fm.moe.luhuan.DataStorageHelper;
//
import fm.moe.luhuan.R;
import fm.moe.luhuan.adapters.SimpleDataAdapter;
import fm.moe.luhuan.beans.data.SimpleData;
import fm.moe.luhuan.http.CommonHttpHelper;
import fm.moe.luhuan.http.MoeHttp;
import fm.moe.luhuan.service.DownloadService;
import fm.moe.luhuan.service.PlayService;
import fm.moe.luhuan.service.QueueDownloadService;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import android.graphics.Bitmap;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
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

	private SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");
	private MoeHttp moeHttp;
	private CommonHttpHelper commonHttp = new CommonHttpHelper();
	
	private Handler mHandler = new Handler();
	private AsyncTask coverTask;
	private DataStorageHelper fileHelper;
	// private PlayService musicService;
	private int nowIndex = 0;

	private ArrayList<SimpleData> playList;
	private SparseArray<Bitmap> imageCache = new SparseArray<Bitmap>();
	private IntentFilter intentFilter = new IntentFilter();
	private SharedPreferences pref;
	// widgets
	private Texts texts = new Texts();
	private Buttons buttons = new Buttons();
	private SeekBar seekBar;
	private ImageView albumCover;
	private ListView listView;
	private RelativeLayout songView;
	private ConnectivityManager connectivityManager;
	private boolean isWaiting = false;
	// player flags and info
	private boolean isPlayerPlaying = false;
	private boolean isPlayerPrepared = false;
	private int playedTime = 0;
	private int fullTime = 0;
	// constants
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.player);
		Log.e("music play", "create");
		initViews();
		listView = (ListView) findViewById(R.id.player_list_view);
		songView = (RelativeLayout) findViewById(R.id.player_song_view);
		setBackTab();
		moeHttp = new MoeHttp(this);
		fileHelper = new DataStorageHelper(this);
		Intent incomingIntent = getIntent();
		connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		final Bundle bundle = incomingIntent.getExtras();

		pref = getSharedPreferences("app_settings", MODE_PRIVATE);
		NetworkInfo ni = connectivityManager.getActiveNetworkInfo();
		
		if (bundle != null) {
			boolean doNeedNetwork = bundle
					.getBoolean(PlayService.EXTRA_IF_NEED_NETWORK);
			boolean playOnlyOnWifi = pref.getBoolean(
					getResources().getString(
							R.string.pref_key_play_only_on_wifi), true);
			boolean isNetworkWifi = true;
			if (ni != null) {
				isNetworkWifi = ni.getType() == ConnectivityManager.TYPE_WIFI ? true
						: false;

			} else {
				isNetworkWifi = false;
			}
			playList = (ArrayList<SimpleData>) bundle
					.get(PlayService.EXTRA_PLAYLIST);
			nowIndex = bundle.getInt(PlayService.EXTRA_SELECTED_INDEX);
			
			setStaticView(playList.get(nowIndex));
			listView.setAdapter(new SimpleDataAdapter(this, playList));
			listView.setOnItemClickListener(onListViewClick);
			if (doNeedNetwork && playOnlyOnWifi && !isNetworkWifi) {
				Builder alertBuilder = new Builder(this);
				AlertDialog alert = alertBuilder.create();
				alert.setTitle("可能产生额外流量");
				alert.setMessage("当前网络为非WIFI网络，继续播放有可能产生额外流量，确定要继续播放吗？");
				alert.setButton(AlertDialog.BUTTON_NEGATIVE, "确定",
						positiveClick);
			}

			Intent serviceIntent = new Intent(this, PlayService.class);
			serviceIntent.putExtras(bundle);
			serviceIntent.setAction(PlayService.ACTION_INIT_LIST);
			startService(serviceIntent);

			serviceIntent.setAction(PlayService.ACTION_START_PLAY);
			startService(serviceIntent);

		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		
		// ((NotificationManager)
		// getSystemService(Context.NOTIFICATION_SERVICE)).cancelAll();
		//Intent bindIntent = new Intent(this, PlayService.class);
		// bindService(bindIntent, conn, BIND_AUTO_CREATE);
		

	}

	@Override
	protected void onStop() {
		super.onStop();
		//unregisterReceiver(broadcastReceiver);
		//mHandler.removeCallbacks(refreshPlayedTimeRunnable);
		// unbindService(conn);
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
		registerReceiver(broadcastReceiver, intentFilter);
	}

	private void setStaticView(SimpleData item) {

		// Log.e("parent id", currentItem.getParentId() + "");
		texts.title.setText(item.getTitle());
		texts.artist.setText(item.getArtist());
		if (item.isFav()) {
			buttons.fav.setImageDrawable(getResources().getDrawable(
					android.R.drawable.btn_star_big_on));
		} else {
			buttons.fav.setImageDrawable(getResources().getDrawable(
					android.R.drawable.btn_star_big_off));
		}
		if (item.getArtist() == null || item.getArtist().equals("")) {
			texts.artist.setText("未知艺术家");
		}

		texts.album.setText(item.getParentTitle());
		loadCover(item.getAlbumnCoverUrl());

	}

	private void sendChangeSongIntent(int targetIndex) {
		Intent changeSongIntent = new Intent(MusicPlay.this, PlayService.class);
		changeSongIntent.setAction(PlayService.ACTION_USER_OPERATE);
		changeSongIntent.putExtra(PlayService.EXTRA_TARGET_SONG_INDEX,
				targetIndex);
		isPlayerPlaying = false;
		isPlayerPrepared = false;
		fullTime = 0;
		playedTime = 0;
		nowIndex = targetIndex;
		startService(changeSongIntent);
		mHandler.removeCallbacks(refreshPlayedTimeRunnable);
		mHandler.post(setStaticViewRunnable);
	}

	private void loadCover(final String albumnCoverUrl) {
		if (coverTask != null) {
			coverTask.cancel(true);
		}

		Bitmap bm = imageCache.get(playList.get(nowIndex).getId());
		if (bm != null) {
			albumCover.setImageBitmap(bm);
		} else {
			coverTask = new AsyncTask<Object, Integer, Bitmap>() {

				@Override
				protected Bitmap doInBackground(Object... params) {
					Bitmap bm = fileHelper.getItemCoverBitmap(playList.get(nowIndex));
					// Log.e("loading bitmap", albumnCoverUrl);
					if (bm == null) {
						bm = commonHttp.getBitmap(albumnCoverUrl);

					}

					imageCache.put(playList.get(nowIndex).getId(), bm);
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

			new Thread() {
				public void run() {
					String ope = null;
					if (playList.get(nowIndex).isFav()) {
						ope = "delete";
					} else {
						ope = "add";
					}
					String url = "http://api.moefou.org/fav/" + ope
							+ ".json?fav_obj_type=song&fav_type=1&fav_obj_id="
							+ playList.get(nowIndex).getId();
					try {
						String json = moeHttp.oauthRequest(url);

						if (!JSON.parseObject(json).getJSONObject("response")
								.getJSONObject("information")
								.getBoolean("has_error")) {
							playList.get(nowIndex).setFav(!playList.get(nowIndex).isFav());
							fileHelper.updateFav(playList.get(nowIndex));
							mHandler.post(new Runnable() {

								public void run() {
									// Log.e("!!", "!!");
									if (playList.get(nowIndex).isFav()) {
										buttons.fav
												.setImageDrawable(getResources()
														.getDrawable(
																android.R.drawable.btn_star_big_on));
										Toast.makeText(getApplicationContext(),
												"收藏成功", Toast.LENGTH_SHORT)
												.show();
									} else {
										buttons.fav
												.setImageDrawable(getResources()
														.getDrawable(
																android.R.drawable.btn_star_big_off));
										Toast.makeText(getApplicationContext(),
												"已取消收藏", Toast.LENGTH_SHORT)
												.show();
									}
								}
							});
						}
						;

						// Log.e("fav", json);
					} catch (Exception e) {
						Log.e("", "", e);
					}
				}
			}.start();
		}

		private void shareSong() {
			Intent shareIntent = new Intent(Intent.ACTION_SEND);

			// shareIntent.putExtra(Intent.EXTRA_TEXT, "test");
			try {
				Bitmap bm = imageCache.get(playList.get(nowIndex).getId());
				String imageUrl = fileHelper.getTempImageUri(bm);
				Uri uri = Uri.fromFile(new File(imageUrl));
				shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
				shareIntent.setType("image/jpeg");
				shareIntent
						.putExtra(
								Intent.EXTRA_TEXT,
								"http://moe.fm/listen?song="
										+ playList.get(nowIndex).getId()
										+ "  ,分享一首来自@萌否网 的歌曲,"
										+ playList.get(nowIndex).getTitle());
			} catch (IOException e) {
				Log.e("", "", e);
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

					int tarIndex = nowIndex + clickCount;
					if (tarIndex < 0) {
						tarIndex = 0;
					} else if (tarIndex > playList.size() - 1) {
						tarIndex = playList.size() - 1;
					}
					
					sendChangeSongIntent(tarIndex);
					
					//mHandler.post(setStaticViewRunnable);
					mHandler.post(resetTimeinfoRunnable);
					clickCount = 0;

				}
			};
			timer.schedule(lastClickTask, 300);

		}

		private void switchPlayPause(View v) {
			Intent ppIntent = new Intent(MusicPlay.this, PlayService.class);
			ppIntent.setAction(PlayService.ACTION_USER_OPERATE);

			if (isPlayerPlaying) {
				ppIntent.putExtra(PlayService.EXTRA_SWITCH_PLAY_OR_PAUSE,
						PlayService.PLAYER_PAUSE);
				mHandler.removeCallbacks(refreshPlayedTimeRunnable);

				((ImageButton) v).setImageDrawable(getResources().getDrawable(
						android.R.drawable.ic_media_play));
				isPlayerPlaying = false;
			} else if (isPlayerPrepared) {
				mHandler.post(refreshPlayedTimeRunnable);
				ppIntent.putExtra(PlayService.EXTRA_SWITCH_PLAY_OR_PAUSE,
						PlayService.PLAYER_PLAY);

				((ImageButton) v).setImageDrawable(getResources().getDrawable(
						android.R.drawable.ic_media_pause));
				isPlayerPlaying = true;
			}
			startService(ppIntent);
		}

		private void downloadSong() {

			if (fileHelper.isItemSaved(playList.get(nowIndex))) {
				Toast.makeText(MusicPlay.this, "该歌曲已下载", Toast.LENGTH_SHORT)
						.show();
			} else {
				Intent downloadIntent = new Intent(getApplicationContext(),
						QueueDownloadService.class);
				downloadIntent.putExtras(getIntent());
				downloadIntent.putExtra(DownloadService.EXTRA_SONG_ITEM,
						playList.get(nowIndex));
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
				Toast.makeText(MusicPlay.this, "已加入到下载队列", Toast.LENGTH_SHORT)
						.show();
				break;
			case 1:
				Toast.makeText(MusicPlay.this,
						playList.get(nowIndex).getTitle() + "下载已完成", Toast.LENGTH_SHORT)
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
			switch (intent.getIntExtra(PlayService.EXTRA_PLAYER_STATUS, 0)) {
			case 0:// for prepared
				buttons.pp.setImageDrawable(getResources().getDrawable(
						android.R.drawable.ic_media_pause));
				isPlayerPrepared = true;
				isPlayerPlaying = true;
				fullTime = intent.getIntExtra(
						PlayService.EXTRA_SONG_DURATION, 1);
				
				texts.fullTime.setText(dateFormat.format(fullTime));
				mHandler.post(refreshPlayedTimeRunnable);
				
				break;
			case 1:// for completion
				nowIndex = intent.getIntExtra(
						PlayService.EXTRA_NOW_PLAYING_INDEX, 0);
				
				
				mHandler.removeCallbacks(refreshPlayedTimeRunnable);
				if ((playList.size() - 1 != intent.getIntExtra(
						PlayService.EXTRA_NOW_PLAYING_INDEX, 0))) {
					mHandler.post(resetTimeinfoRunnable);
					isPlayerPrepared = false;
					isPlayerPlaying = false;
					fullTime = 0;
					playedTime = 0;
					setStaticView(playList.get(nowIndex));
				}else{
					playedTime = 0;
					isPlayerPlaying = false;
					setStaticView(playList.get(nowIndex));
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

			setStaticView(playList.get(nowIndex));
		}
	};
	private Runnable refreshPlayedTimeRunnable = new Runnable() {

		public void run() {
			if (isPlayerPlaying) {
				texts.played.setText(dateFormat.format(playedTime));
				if (!seekBar.isPressed()) {
					seekBar.setProgress(playedTime * 100 / fullTime);
				}
				playedTime += 1000;
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
			if (isPlayerPrepared) {
				Intent seekIntent = new Intent(MusicPlay.this,
						PlayService.class);
				seekIntent.setAction(PlayService.ACTION_USER_OPERATE);
				seekIntent.putExtra(PlayService.EXTRA_SEEK_TO,
						seekBar.getProgress() * fullTime / 100);
				playedTime = seekBar.getProgress() * fullTime / 100;
				startService(seekIntent);
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
			sendChangeSongIntent(arg2);
		}
	};
	private DialogInterface.OnClickListener positiveClick = new DialogInterface.OnClickListener() {

		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub

		}
	};
}
