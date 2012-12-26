package fm.moe.luhuan.service;

import java.io.IOException;
import java.util.ArrayList;

import fm.moe.luhuan.DataStorageHelper;
import fm.moe.luhuan.activities.MusicPlay;
import fm.moe.luhuan.beans.data.SimpleData;

import android.R;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class PlayService extends Service {

	public static final String EXTRA_IF_NEED_NETWORK = "need network";
	public static final String EXTRA_PLAYLIST = "playList";
	public static final String EXTRA_PLAYLIST_ID = "playListId";
	public static final String EXTRA_SELECTED_INDEX = "selectedIndex";
	public static final String EXTRA_SONG_CURRENT_POSITION="current position";
	public static final String ACTION_PLAYER_STATE_CHANGE = "player state change";
	public static final String ACTION_START_PLAY = "start play";
	public static final String ACTION_INIT_LIST = "init list";
	public static final String ACTION_USER_OPERATE = "user operate";
	
	public static final int NOTIFICATION_ID = 1;
	public static final int PLAYER_PAUSE = -2;
	public static final int PLAYER_PLAY = -1;
	// 1 for prepaed,0 for complete,-1 for err,2 for bufferedUpdate
	public static final String EXTRA_PLAYER_STATUS = "player state";
	public static final String EXTRA_PLAYER_BUFFERED_PERCENT = "buffered percent";
	public static final String EXTRA_TARGET_SONG_INDEX = "target index";
	public static final String EXTRA_SONG_DURATION = "song duration";
	public  static final String EXTRA_NOW_PLAYING_INDEX = "now playing index";
	public static final String EXTRA_SWITCH_PLAY_OR_PAUSE = "play or pause";
	public static final String EXTRA_SEEK_TO = "seek to";

	//private LocalBroadcastManager broadcastManager;
	private Intent broadcast = new Intent();
	private PlayerBinder binder = new PlayerBinder();
	private Builder notificationBuilder;
	private Handler mHandler = new Handler();
	private NotificationManager notificationManager;
	public ArrayList<SimpleData> playList;
	public String playListId;
	public int nowIndex = -1;
	public MediaPlayer player = new MediaPlayer();
	public DataStorageHelper fileHelper;
	public boolean isPrepared = false;
	public int bufferedPercent = 0;
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub

		super.onCreate();
		mHandler.post(isPlayingRunnable);
		initMediaPlayer();
		fileHelper = new DataStorageHelper(this);
		registerReceiver(receiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
		// Log.e("service", "oncreate");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String action = intent.getAction();
		Bundle bundle = intent.getExtras();
		
		if (action.equals(ACTION_INIT_LIST)) {
			
			playList = (ArrayList<SimpleData>) bundle.get(EXTRA_PLAYLIST);
			nowIndex = (Integer) bundle.get(EXTRA_SELECTED_INDEX);
		} else if (action.equals(ACTION_START_PLAY)) {
			initNotification(bundle);
			nowIndex = (Integer) bundle.get(EXTRA_SELECTED_INDEX);
			playSongAtIndex(nowIndex);
		}else if(action.equals(ACTION_USER_OPERATE)){
			if(bundle.containsKey(EXTRA_TARGET_SONG_INDEX)){
				playSongAtIndex(bundle.getInt(EXTRA_TARGET_SONG_INDEX));
			}
			if(bundle.containsKey(EXTRA_SEEK_TO)){
				player.seekTo(bundle.getInt(EXTRA_SEEK_TO));
			}
			if(bundle.containsKey(EXTRA_SWITCH_PLAY_OR_PAUSE)){
				if(bundle.getInt(EXTRA_SWITCH_PLAY_OR_PAUSE)==PLAYER_PAUSE){
					player.pause();
				}else{
					player.start();
				}
			}
		}
		
		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		Log.e("service", "onbind");

		return binder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		return super.onUnbind(intent);

	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		notificationManager.cancelAll();
		player.release();
		unregisterReceiver(receiver);
	}

	public void playSongAtIndex(int n) {
		// TODO Auto-generated method stub

		player.reset();

		String url = fileHelper.getItemMp3Url(playList.get(n));

		// Uri uri = Uri.parse(url);
		// why block here????
		try {
			// fav song的时候为NULL？
			player.setDataSource(url);
			isPrepared = false;
			
		} catch (Exception e) {
			Log.e("setDataSource err", "", e);
		}
		notificationManager.cancel(NOTIFICATION_ID);
		player.prepareAsync();
		nowIndex = n;

	}

	private void initMediaPlayer() {
		player.setOnBufferingUpdateListener(onSongBuffering);
		player.setOnErrorListener(onPlayerErr);
		player.setOnPreparedListener(onPlayerPrepared);
		player.setOnCompletionListener(onPlayComplete);
		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		
	}

	private void initNotification(Bundle bundle) {
		Intent resumePlayActivity = new Intent(this, MusicPlay.class);
		resumePlayActivity.putExtras(bundle);
		resumePlayActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

		notificationBuilder = new Builder(this);
		
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				resumePlayActivity, PendingIntent.FLAG_UPDATE_CURRENT);
		notificationBuilder.setContentIntent(pendingIntent);
	}

	public void sendNotification(int drawableId, String tickerText,
			String title, String content) {
		notificationBuilder.setSmallIcon(drawableId);
		notificationBuilder.setTicker("萌否音乐:" + tickerText);
		notificationBuilder.setContentTitle("萌否音乐:" + title);
		notificationBuilder.setContentText(content);
		notificationManager
				.notify(NOTIFICATION_ID, notificationBuilder.build());
	}

	public class PlayerBinder extends Binder {
		public PlayService getService() {
			return PlayService.this;
		}
	}

	private OnErrorListener onPlayerErr = new OnErrorListener() {

		public boolean onError(MediaPlayer mp, int what, int extra) {
			// TODO Auto-generated method stub
			broadcast.setAction(ACTION_PLAYER_STATE_CHANGE);
			broadcast.putExtra(EXTRA_PLAYER_STATUS, -1);
			sendBroadcast(broadcast);
			sendNotification(R.drawable.stat_notify_error, "播放已终止", "播放已终止",
					"播放器出错");
			player.reset();
			// Log.e("broadcast", "send");
			return true;
		}
	};
	private OnPreparedListener onPlayerPrepared = new OnPreparedListener() {

		public void onPrepared(MediaPlayer mp) {
			isPrepared = true;
			bufferedPercent = 100;
			mp.start();
			sendNotification(R.drawable.ic_media_play, playList.get(nowIndex)
					.getTitle(), "正在播放", playList.get(nowIndex).getTitle());
			broadcast.setAction(ACTION_PLAYER_STATE_CHANGE);
			broadcast.putExtra(EXTRA_PLAYER_STATUS, 0);
			broadcast.putExtra(EXTRA_SONG_DURATION, player.getDuration());
			broadcast.putExtra(EXTRA_NOW_PLAYING_INDEX, nowIndex);
			sendBroadcast(broadcast);
			// Log.e("player", "prepare");
		}
	};
	private OnInfoListener onInfo = new OnInfoListener() {
		
		public boolean onInfo(MediaPlayer mp, int what, int extra) {
			// TODO Auto-generated method stub
			Log.e("media info", "what="+what+",extra="+extra);
			return false;
		}
	};
	private OnCompletionListener onPlayComplete = new OnCompletionListener() {

		public void onCompletion(MediaPlayer mp) {
			if (playList.size() - 1 > nowIndex) {
				playSongAtIndex(++nowIndex);
			}
			mHandler.removeCallbacks(isPlayingRunnable);
			new DoPersistThread().start();
			broadcast.setAction(ACTION_PLAYER_STATE_CHANGE);
			broadcast.putExtra(EXTRA_PLAYER_STATUS, 1);
			broadcast.putExtra(EXTRA_NOW_PLAYING_INDEX, nowIndex);
			sendBroadcast(broadcast);
			notificationManager.cancel(1);
			// Log.e("broadcast", "send");

		}
	};
	private OnBufferingUpdateListener onSongBuffering = new OnBufferingUpdateListener() {

		public void onBufferingUpdate(MediaPlayer mp, int percent) {
			bufferedPercent = percent;
			broadcast.setAction(ACTION_PLAYER_STATE_CHANGE);
			broadcast.putExtra(EXTRA_PLAYER_STATUS, 2);
			broadcast.putExtra(EXTRA_PLAYER_BUFFERED_PERCENT, percent);
			sendBroadcast(broadcast);
			Log.e("buffered update", percent+"");

		}
	};
	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getIntExtra("state", 1) == 0) {
				if (player.isPlaying()) {
					player.pause();
					sendNotification(R.drawable.ic_media_pause, "播放已暂停",
							"播放已暂停", "耳机已拔出");
				}
			}

		}
	};

	private class DoPersistThread extends Thread {

		public void run() {
			try {
				fileHelper.persistState(playList, nowIndex);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	};
	private Runnable isPlayingRunnable = new Runnable() {
		
		public void run() {
			
			//Log.e("currentPosition", player.getCurrentPosition()+"");
			broadcast.setAction(ACTION_PLAYER_STATE_CHANGE);
			broadcast.putExtra(EXTRA_SONG_CURRENT_POSITION, player.getCurrentPosition());
			sendBroadcast(broadcast);
			mHandler.postDelayed(isPlayingRunnable, 1000);
		}
	};

}
