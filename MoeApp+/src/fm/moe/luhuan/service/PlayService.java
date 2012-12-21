package fm.moe.luhuan.service;

import java.util.ArrayList;

import fm.moe.luhuan.FileStorageHelper;
import fm.moe.luhuan.activities.MusicPlay;
import fm.moe.luhuan.beans.data.SimpleData;

import android.R;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class PlayService extends Service {
	public static final String EXTRA_IF_NEED_NETWORK = "need network";
	public static final String EXTRA_PLAYLIST = "playList";
	public static final String EXTRA_PLAYLIST_ID = "playListId";
	public static final String EXTRA_SELECTED_INDEX = "selectedIndex";
	public static final String ACTION_PLAYER_STATE_CHANGE = "player state change";
	// 1 for prepaed,0 for complete,-1 for err,2 for bufferedUpdate
	public static final String EXTRA_PLAYER_STATE = "player state";
	public static final String EXTRA_PLAYER_BUFFERED_PERCENT = "buffered percent";
	private LocalBroadcastManager broadcastManager;
	private Intent broadcast = new Intent();
	private PlayerBinder binder = new PlayerBinder();
	private Builder notificationBuilder;
	private NotificationManager notificationManager;
	public ArrayList<SimpleData> playList;
	public String playListId;
	public int nowIndex = -1;
	public MediaPlayer player = new MediaPlayer();
	public FileStorageHelper fileHelper;
	public boolean isPrepared = false;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		initNotification();
		initMediaPlayer();
		fileHelper = new FileStorageHelper(this);
		broadcastManager = LocalBroadcastManager
				.getInstance(getApplicationContext());

		// Log.e("service", "oncreate");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		//Log.e("service", "onstartcommand");
		if (intent != null
				&& intent.getAction().equals(MusicPlay.PLAY_ACT_CREATE)) {

			onPlayerInit(intent);

		}
		return START_STICKY;
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

	private void onPlayerInit(Intent intent) {
		Bundle bundle = intent.getExtras();
		playList = (ArrayList<SimpleData>) bundle.get(EXTRA_PLAYLIST);
		nowIndex = (Integer) bundle.get(EXTRA_SELECTED_INDEX);
		playListId = (String) bundle.get(EXTRA_PLAYLIST_ID);
		playSongAtIndex(nowIndex);

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

	private void initNotification() {
		notificationBuilder = new Builder(this);
		broadcastManager = LocalBroadcastManager
				.getInstance(getApplicationContext());
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		Intent resumePlayActivity = new Intent(this, MusicPlay.class);
		resumePlayActivity.setAction(MusicPlay.ACTION_RESUME);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				resumePlayActivity, PendingIntent.FLAG_UPDATE_CURRENT);
		notificationBuilder.setContentIntent(pendingIntent);
	}

	private void sendNotification(int drawableId, String title, String content,
			int notificationId) {
		notificationBuilder.setSmallIcon(drawableId);
		notificationBuilder.setContentTitle("萌否音乐:" + title);
		notificationBuilder.setContentText(content);
		notificationManager.notify(notificationId, notificationBuilder.build());
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
			broadcast.putExtra(EXTRA_PLAYER_STATE, -1);
			broadcastManager.sendBroadcast(broadcast);
			player.reset();
			// Log.e("broadcast", "send");
			return true;
		}
	};
	private OnPreparedListener onPlayerPrepared = new OnPreparedListener() {

		public void onPrepared(MediaPlayer mp) {
			isPrepared = true;
			mp.start();
			sendNotification(R.drawable.ic_media_play, "正在播放", playList.get(nowIndex).getTitle(), 1);
			broadcast.setAction(ACTION_PLAYER_STATE_CHANGE);
			broadcast.putExtra(EXTRA_PLAYER_STATE, 0);

			broadcastManager.sendBroadcast(broadcast);
			// Log.e("player", "prepare");
		}
	};
	private OnCompletionListener onPlayComplete = new OnCompletionListener() {

		public void onCompletion(MediaPlayer mp) {
			if (playList.size() - 1 > nowIndex) {
				playSongAtIndex(++nowIndex);
			}
			broadcast.setAction(ACTION_PLAYER_STATE_CHANGE);
			broadcast.putExtra(EXTRA_PLAYER_STATE, 1);
			broadcastManager.sendBroadcast(broadcast);
			notificationManager.cancel(1);
			// Log.e("broadcast", "send");

		}
	};
	private OnBufferingUpdateListener onSongBuffering = new OnBufferingUpdateListener() {

		public void onBufferingUpdate(MediaPlayer mp, int percent) {
			broadcast.setAction(ACTION_PLAYER_STATE_CHANGE);
			broadcast.putExtra(EXTRA_PLAYER_STATE, 2);
			broadcast.putExtra(EXTRA_PLAYER_BUFFERED_PERCENT, percent);
			broadcastManager.sendBroadcast(broadcast);

		}
	};

}
