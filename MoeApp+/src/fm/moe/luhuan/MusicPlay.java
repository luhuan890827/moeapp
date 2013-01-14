package fm.moe.luhuan;

import java.io.File;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.alibaba.fastjson.JSON;
//
import fm.moe.luhuan.R;
import fm.moe.luhuan.adapter.SimpleDataAdapter;
import fm.moe.luhuan.beans.data.SimpleData;
import fm.moe.luhuan.http.CommonHttpHelper;
import fm.moe.luhuan.http.MoeHttp;
import fm.moe.luhuan.service.DownloadService;
import fm.moe.luhuan.service.PlayBackService;
import fm.moe.luhuan.service.PlayService;
import fm.moe.luhuan.service.QueueDownloadService;
import fm.moe.luhuan.utils.DataStorageHelper;
import fm.moe.luhuan.utils.MyUncaughtExceptionHandler;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;

import android.content.res.Resources.NotFoundException;

import android.graphics.Bitmap;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;

import android.text.Html;
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

	private ArrayList<SimpleData> playList;
	private SparseArray<Bitmap> imageCache = new SparseArray<Bitmap>();
	private IntentFilter intentFilter = new IntentFilter();
	// widgets
	private Texts texts = new Texts();
	private Buttons buttons = new Buttons();
	private SeekBar seekBar;
	private ImageView albumCover;
	private ListView listView;
	private RelativeLayout songView;
	private ConnectivityManager connectivityManager;
	private boolean onbind = false;
	private IPlaybackService playbackService;
	private ServiceConnection servConn = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			onbind = false;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			onbind = true;
			playbackService = IPlaybackService.Stub.asInterface(service);
			if(playList==null){
				try {
					playList = (ArrayList<SimpleData>) playbackService.getList();
					SimpleDataAdapter adapter = new SimpleDataAdapter(MusicPlay.this, playList);
					listView.setAdapter(adapter);
					listView.setOnItemClickListener(onListViewClick);
					
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			
			try {
				setDisplayInfo();
			} catch (RemoteException e) {
				Log.e("", "", e);
				e.printStackTrace();
			} catch (NotFoundException e) {
				Log.e("", "", e);
				e.printStackTrace();
			}
		}
		//MyUncaughtExceptionHandler crashHandler = new MyUncaughtExceptionHandler(getApplicationContext());
		
		
	};

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.player);
		Log.e("music play", "create");
		initViews();
		listView = (ListView) findViewById(R.id.player_list_view);
		songView = (RelativeLayout) findViewById(R.id.player_song_view);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		moeHttp = new MoeHttp(this);
		fileHelper = new DataStorageHelper(this);
		intentFilter.addAction(PlayBackService.ACTION_PLAYER_STATE_CHANGE);
		intentFilter.addAction(DownloadService.ACTION_DOWNLOAD_STATE_CHANGE);

		connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo ni = connectivityManager.getActiveNetworkInfo();

		Intent incomingIntent = getIntent();
		Bundle bundle = incomingIntent.getExtras();
		String action = incomingIntent.getAction();
		if (bundle != null) {
			playList = (ArrayList<SimpleData>) bundle
					.get(PlayBackService.EXTRA_PLAYLIST);
			
			
			// from browser
			if (action == null&&!isPlaybackServiceRunning()) {

				Intent serviceIntent = new Intent(this, PlayBackService.class);
				serviceIntent.putExtras(bundle);
				
				startService(serviceIntent);
			}
			if(playList!=null){
				SimpleDataAdapter adapter = new SimpleDataAdapter(this, playList);
				listView.setAdapter(adapter);
				listView.setOnItemClickListener(onListViewClick);
			}else
			if(!isPlaybackServiceRunning()){
				Intent i = new Intent(this,MusicBrowse.class);
				startActivity(i);
				this.finish();
			}

		}
		
		

	}
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		Bundle bundle = intent.getExtras();
		String action = intent.getAction();
		if (bundle != null) {
			playList = (ArrayList<SimpleData>) bundle
					.get(PlayBackService.EXTRA_PLAYLIST);
			
			
			// from browser
			if (action == null) {

				Intent serviceIntent = new Intent(this, PlayBackService.class);
				serviceIntent.putExtras(bundle);
				
				startService(serviceIntent);
			}
			if(playList!=null){
				SimpleDataAdapter adapter = new SimpleDataAdapter(this, playList);
				listView.setAdapter(adapter);
				listView.setOnItemClickListener(onListViewClick);
			}

		}
	}
	@Override
	protected void onStart() {

		super.onStart();
		
		mHandler.post(refreshPlayingStatusRunnable);
		Intent bindIntent = new Intent(this, PlayBackService.class);
		bindService(bindIntent, servConn, BIND_AUTO_CREATE);
		
		registerReceiver(broadcastReceiver, intentFilter);
		if(onbind){
			try {
				playbackService.stopAsForeGround();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		if(onbind){
			try {
				if(playbackService.isPlayerPlaying()){
					playbackService.setAsForeGround();
				}
				
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		mHandler.removeCallbacks(refreshPlayingStatusRunnable);
		unregisterReceiver(broadcastReceiver);
		unbindService(servConn);
		
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
		
		Intent intent = new Intent(this, MusicBrowse.class);
		startActivity(intent);
		

	}

	@Override
	public boolean onSearchRequested() {
		startSearch(null, false, null, false);

		return true;
	}
	private boolean isPlaybackServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		List<RunningServiceInfo> services = manager
				.getRunningServices(Integer.MAX_VALUE);
		String mServiceName = PlayBackService.class.getName();
		for (int i = 0; i < services.size(); i++) {
			if (services.get(i).service.getClassName().equals(mServiceName)) {
				return true;
			}
		}

		return false;
	}
	private void initViews() {
		texts.bindView();
		buttons.bindView();
		seekBar = (SeekBar) findViewById(R.id.playing_seekbar);
		seekBar.setOnSeekBarChangeListener(onSeekPos);
		albumCover = (ImageView) findViewById(R.id.big_cover);
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);

		albumCover.setLayoutParams(new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, dm.widthPixels));

	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setDisplayInfo() throws RemoteException, NotFoundException {
		int nowIndex = playbackService.getNowIndex();
		SimpleData item = playList.get(nowIndex);
		getActionBar().setTitle(
				"正在播放:" + (nowIndex + 1) + "/" + playList.size());
		// Log.e("parent id", currentItem.getParentId() + "");
		texts.title.setText(Html.fromHtml(item.getTitle()));

		if (item.isFav()) {
			buttons.fav.setImageDrawable(getResources().getDrawable(
					android.R.drawable.btn_star_big_on));
		} else {
			buttons.fav.setImageDrawable(getResources().getDrawable(
					android.R.drawable.btn_star_big_off));
		}
		if (item.getArtist() == null || item.getArtist().equals("")) {
			texts.artist.setText("未知艺术家");
		} else {
			texts.artist.setText(item.getArtist());
		}
		if (playbackService.isPlayerPlaying()) {
			buttons.pp.setImageDrawable(getResources().getDrawable(
					android.R.drawable.ic_media_pause));

		} else {
			buttons.pp.setImageDrawable(getResources().getDrawable(
					android.R.drawable.ic_media_play));
		}
		if (playbackService.isPlayerPrepared()) {
			texts.fullTime.setText(dateFormat.format(playbackService
					.getSongDuration()));
		}else{
			texts.fullTime.setText("00:00");
			texts.played.setText("00:00");
			seekBar.setProgress(0);
			seekBar.setSecondaryProgress(0);
		}

		texts.album.setText(item.getParentTitle());

		loadCover(item);

	}

	private void loadCover(final SimpleData item) {
		if (coverTask != null) {
			coverTask.cancel(true);
		}

		Bitmap bm = imageCache.get(item.getParentId());
		if (bm != null) {
			albumCover.setImageBitmap(bm);
		} else {
			coverTask = new AsyncTask<Object, Integer, Bitmap>() {

				@Override
				protected Bitmap doInBackground(Object... params) {
					Bitmap bm = fileHelper.getItemCoverBitmap(item);
					// Log.e("loading bitmap", albumnCoverUrl);
					if (bm == null) {
						bm = commonHttp.getBitmap(item.getAlbumnCoverUrl());

					}

					imageCache.put(item.getParentId(), bm);
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
			artist.setOnClickListener(onKeywordClick);
			album.setOnClickListener(onKeywordClick);
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
				try {
					downloadSong();
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				break;
			case R.id.ib_pp:
				try {
					switchPlayPause(v);
				} catch (RemoteException e) {
					Log.e("", "",e);
					e.printStackTrace();
				} catch (NotFoundException e) {
					Log.e("", "",e);
					e.printStackTrace();
				}
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
					if (onbind) {
						SimpleData item = null;
						try {
							item = playList.get(playbackService.getNowIndex());
						} catch (RemoteException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						if (item.isFav()) {
							ope = "delete";
						} else {
							ope = "add";
						}
						String url = "http://api.moefou.org/fav/"
								+ ope
								+ ".json?fav_obj_type=song&fav_type=1&fav_obj_id="
								+ item.getId();
						try {
							String json = moeHttp.oauthRequest(url);

							if (!JSON.parseObject(json)
									.getJSONObject("response")
									.getJSONObject("information")
									.getBoolean("has_error")) {
								item.setFav(!item.isFav());
								fileHelper.updateFav(item);
								final SimpleData param = item;
								mHandler.post(new Runnable() {

									public void run() {
										// Log.e("!!", "!!");
										if (param.isFav()) {
											buttons.fav
													.setImageDrawable(getResources()
															.getDrawable(
																	android.R.drawable.btn_star_big_on));
											Toast.makeText(
													getApplicationContext(),
													"收藏成功", Toast.LENGTH_SHORT)
													.show();
										} else {
											buttons.fav
													.setImageDrawable(getResources()
															.getDrawable(
																	android.R.drawable.btn_star_big_off));
											Toast.makeText(
													getApplicationContext(),
													"已取消收藏", Toast.LENGTH_SHORT)
													.show();
										}
									}
								});
							}

							// Log.e("fav", json);
						} catch (Exception e) {
							Log.e("", "", e);
						}
					}

				}
			}.start();
		}

		private void shareSong() {

			if (onbind) {
				Intent shareIntent = new Intent(Intent.ACTION_SEND);
				try {
					SimpleData item = playList.get(playbackService
							.getNowIndex());
					Bitmap bm = imageCache.get(item.getParentId());
					String imageUrl = fileHelper.getTempImageUri(bm);
					Uri uri = Uri.fromFile(new File(imageUrl));
					shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
					shareIntent.setType("image/jpeg");
					shareIntent.putExtra(Intent.EXTRA_TEXT,
							"http://moe.fm/listen?song=" + item.getId()
									+ "  ,分享一首来自@萌否网 的歌曲," + item.getTitle());
				} catch (IOException e) {
					Log.e("", "", e);
					e.printStackTrace();
				} catch (RemoteException e) {
					Log.e("", "", e);
					e.printStackTrace();
				}

				startActivity(Intent.createChooser(shareIntent, "分享到..."));
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
					if (onbind) {
						int tarIndex = 0;
						try {
							tarIndex = playbackService.getNowIndex()
									+ clickCount;
						} catch (RemoteException e) {
							Log.e("", "", e);
							e.printStackTrace();
						}
						if (tarIndex < 0) {
							tarIndex = 0;
						} else if (tarIndex > playList.size() - 1) {
							tarIndex = playList.size() - 1;
						}
						try {
							playbackService.playSongAtIndex(tarIndex);
						} catch (RemoteException e) {
							Log.e("", "",e);
							e.printStackTrace();
						}
						mHandler.post(new Runnable() {
							
							@Override
							public void run() {
								try {
									setDisplayInfo();
								} catch (RemoteException e) {
									e.printStackTrace();
									Log.e("", "",e);
								} catch (NotFoundException e) {
									e.printStackTrace();
									Log.e("", "",e);
								}
							}
						});
						clickCount = 0;
					}

				}
			};
			timer.schedule(lastClickTask, 300);

		}

		private void switchPlayPause(View v) throws RemoteException, NotFoundException {
			Intent ppIntent = new Intent(MusicPlay.this, PlayService.class);
			ppIntent.setAction(PlayService.ACTION_USER_OPERATE);
			if (onbind) {
				
					if (playbackService.isPlayerPlaying()) {

						((ImageButton) v).setImageDrawable(getResources()
								.getDrawable(android.R.drawable.ic_media_play));
						playbackService.pause();
						playbackService.stopAsForeGround();
					} else if (playbackService.isPlayerPrepared()) {

						((ImageButton) v)
								.setImageDrawable(getResources().getDrawable(
										android.R.drawable.ic_media_pause));
						playbackService.start();
					}
				
			}

		}

		private void downloadSong() throws RemoteException {
			if (onbind) {
				SimpleData item = playList.get(playbackService.getNowIndex());
				if (fileHelper.isItemSaved(item)) {
					Toast.makeText(MusicPlay.this, "该歌曲已下载", Toast.LENGTH_SHORT)
							.show();
				} else {
					Intent downloadIntent = new Intent(getApplicationContext(),
							QueueDownloadService.class);
					downloadIntent.putExtras(getIntent());
					downloadIntent.putExtra(DownloadService.EXTRA_SONG_ITEM,
							item);
					startService(downloadIntent);
				}
			}

		}
	};
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (action.equals(PlayBackService.ACTION_PLAYER_STATE_CHANGE)) {
				try {
					onPlayerStateChange(intent);
				} catch (RemoteException e) {
					Log.e("", "", e);
					e.printStackTrace();
				}
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

			default:
				break;
			}
		}

		private void onPlayerStateChange(Intent intent) throws RemoteException {
			if (!onbind) {
				return;
			}
			switch (intent.getIntExtra(PlayBackService.EXTRA_PLAYER_STATUS, 0)) {
			case PlayBackService.PLAYER_PREPARED:// for prepared
				buttons.pp.setImageDrawable(getResources().getDrawable(
						android.R.drawable.ic_media_pause));

				texts.fullTime.setText(dateFormat.format(playbackService
						.getSongDuration()));

				break;
			case PlayBackService.PLAYER_COMPLETION:// for completion

				if (playList.size() - 1 != playbackService.getNowIndex()) {
					setDisplayInfo();
				}else{
					buttons.pp.setImageDrawable(getResources().getDrawable(
							android.R.drawable.ic_media_play));
				}

				break;
			case PlayBackService.PLAYER_BURRERING:// for buffer update
				seekBar.setSecondaryProgress(intent.getIntExtra(
						PlayBackService.EXTRA_PLAYER_BUFFERING_PERCENT, 0));
				break;
			case -1:// for err
				break;
			default:
				break;
			}
		}
	};
	

	
	private OnSeekBarChangeListener onSeekPos = new OnSeekBarChangeListener() {

		public void onStopTrackingTouch(SeekBar seekBar) {
			if(onbind){
				try {
					if (playbackService.isPlayerPrepared()) {
						playbackService.seekTo(seekBar.getProgress() * playbackService.getSongDuration() / 100);
					}
				} catch (RemoteException e) {
					Log.e("", "",e);
					e.printStackTrace();
				}
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
			if(onbind){
				try {
					playbackService.playSongAtIndex(arg2);
					setDisplayInfo();
				} catch (RemoteException e) {
					Log.e("", "",e);
					e.printStackTrace();
				}
			}
		}
	};
	private Runnable refreshPlayingStatusRunnable = new Runnable() {
		
		@Override
		public void run() {
			if(onbind){
				try {
					if(playbackService.isPlayerPlaying()){
						int currentPosition = playbackService.getSongCurrentPosition();
						texts.played.setText(dateFormat.format(currentPosition));
						if (!seekBar.isPressed()) {
							seekBar.setProgress(currentPosition * 100
									/ playbackService.getSongDuration());
						}
					}
				} catch (RemoteException e) {
					Log.e("", "",e);
					e.printStackTrace();
				}
			}
			mHandler.postDelayed(refreshPlayingStatusRunnable, 500);
		}
	};
	private OnClickListener onKeywordClick = new OnClickListener() {

		public void onClick(View v) {
			if(onbind){
				SimpleData item = null;
				try {
					item = playList.get(playbackService.getNowIndex());
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				switch (v.getId()) {
				case R.id.playing_song_artist:
					if (!item.getArtist().equals("")) {
						startSearch(item.getArtist(), false,
								null, false);
					}
					break;
				case R.id.playing_song_albumn:
					startSearch(item.getParentTitle(), false,
							null, false);
					break;
				default:
					break;
				}
			}
			
		}
	};
	
}
