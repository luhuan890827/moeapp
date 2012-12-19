package fm.moe.luhuan.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fm.moe.luhuan.FileStorageHelper;
import fm.moe.luhuan.activities.MusicPlay;
import fm.moe.luhuan.beans.data.SimpleData;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnTimedTextListener;
import android.media.TimedText;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class PlayService extends Service {
	public static final String BUNDLE_KEY_PLAYLIST = "playList";
	public static final String BUNDLE_KEY_PLAYLIST_ID = "playListId";
	public static final String BUNDLE_KEY_SELECTED_INDEX = "selectedIndex";
	public static final String ACTION_PLAYER_PREPARED = "prepared";
	public static final String ACTION_PLAYER_COMPLETION = "complete";
	public static final String ACTION_PLAYER_ERR = "error";
	public static final String ACTION_PLAYER_BUFFERED_UPDATE = "buffer update";

	private LocalBroadcastManager broadcastManager;
	private Intent broadcast = new Intent();
	

	private PlayerBinder binder = new PlayerBinder();

	public ArrayList<SimpleData> playList;
	public String playListId;
	public int nowIndex = -1;
	public MediaPlayer player = new MediaPlayer();
	public FileStorageHelper fileHelper;
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		fileHelper = new FileStorageHelper(this);
		player.setOnBufferingUpdateListener(onSongBuffering);
		player.setOnErrorListener(onPlayerErr);
		player.setOnPreparedListener(onPlayerPrepared);
		player.setOnCompletionListener(onPlayComplete);
		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		broadcastManager = LocalBroadcastManager
				.getInstance(getApplicationContext());

		//Log.e("service", "oncreate");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Log.e("service", "onstartcommand");
		//Log.e("service", "onstartcommand action="+intent.getAction());
		if (intent.getAction().equals(MusicPlay.PLAY_ACT_CREATE)) {
			try {
				onPlayerInit(intent);
			} catch (Exception e) {
				Log.e("play service err", "", e);
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
		playList = (ArrayList<SimpleData>) bundle.get(BUNDLE_KEY_PLAYLIST);
		nowIndex = (Integer) bundle.get(BUNDLE_KEY_SELECTED_INDEX);
		playListId = (String) bundle.get(BUNDLE_KEY_PLAYLIST_ID);
		playSongAtIndex(nowIndex);

	}
	
	private void playSongAtIndex(int n) {
		// TODO Auto-generated method stub
		player.reset();
		
		String url =fileHelper.getItemMp3Url(playList.get(n));
		
		//Uri uri = Uri.parse(url);
		// why block here????
		try {
			
			player.setDataSource(url);
		} catch (Exception e) {
			Log.e("setDataSource err", "", e);
		}
		player.prepareAsync();
		
	}

	public class PlayerBinder extends Binder {
		public PlayService getService() {
			return PlayService.this;
		}
	}

	private OnErrorListener onPlayerErr = new OnErrorListener() {

		public boolean onError(MediaPlayer mp, int what, int extra) {
			// TODO Auto-generated method stub
			broadcast.setAction(ACTION_PLAYER_ERR);

			broadcastManager.sendBroadcast(broadcast);
			// Log.e("broadcast", "send");
			return true;
		}
	};
	private OnPreparedListener onPlayerPrepared = new OnPreparedListener() {

		public void onPrepared(MediaPlayer mp) {

			broadcast.setAction(ACTION_PLAYER_PREPARED);

			broadcastManager.sendBroadcast(broadcast);
			 //Log.e("player", "prepare");
		}
	};
	private OnCompletionListener onPlayComplete = new OnCompletionListener() {

		public void onCompletion(MediaPlayer mp) {
			broadcast.setAction(ACTION_PLAYER_COMPLETION);

			broadcastManager.sendBroadcast(broadcast);
			// Log.e("broadcast", "send");

		}
	};
	private OnBufferingUpdateListener onSongBuffering = new OnBufferingUpdateListener() {

		public void onBufferingUpdate(MediaPlayer mp, int percent) {
			broadcast.setAction(ACTION_PLAYER_BUFFERED_UPDATE);

			broadcastManager.sendBroadcast(broadcast);

		}
	};

}
