package fm.moe.luhuan.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import fm.moe.luhuan.activities.MusicPlay;
import fm.moe.luhuan.beans.data.SimpleData;

import android.app.Service;
import android.content.Intent;
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
import android.util.Log;

public class PlayService extends Service{
	public static final String BUNDLE_KEY_PLAYLIST = "playList";
	public static final String BUNDLE_KEY_PLAYLIST_ID = "playListId";
	public static final String BUNDLE_KEY_SELECTED_INDEX = "selectedIndex";
	public MediaPlayer player = new MediaPlayer();
	
	private PlayerBinder binder = new PlayerBinder();
	
	public ArrayList<SimpleData> playList;
	public String playListId;
	public int nowIndex = -1;
	
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		player.setOnBufferingUpdateListener(onSongBuffering);
		player.setOnErrorListener(onPlayerErr);
		player.setOnPreparedListener(onPlayerPrepared);
		player.setOnCompletionListener(onPlayComplete);
		Log.e("service", "oncreate");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Log.e("service", "onstartcommand");
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		Log.e("service", "onbind");
		
		if(intent.getAction().equals(MusicPlay.PLAY_ACT_CREATE)){
			try {
				onPlayerInit(intent);
			} catch (Exception e) {
				Log.e("play service err", "",e);
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		return binder;
	}
	
	
	
	private void onPlayerInit(Intent intent) throws Exception {
		Bundle bundle = intent.getExtras();
		playList = (ArrayList<SimpleData>) bundle.get(BUNDLE_KEY_PLAYLIST);
		nowIndex = (Integer) bundle.get(BUNDLE_KEY_SELECTED_INDEX);
		playListId = (String) bundle.get(BUNDLE_KEY_PLAYLIST_ID);
		playSongAtIndex(nowIndex);
	}



	private void playSongAtIndex(int n) throws Exception {
		// TODO Auto-generated method stub
		String url = playList.get(nowIndex).getMp3Url();
		Uri uri = Uri.parse(url);
		player.setDataSource(getApplicationContext(), uri);
		//player. 
	}



	public class PlayerBinder extends Binder{
		public PlayService getService(){
			return PlayService.this;
		}
	}
	private OnErrorListener onPlayerErr= new OnErrorListener() {
		
		public boolean onError(MediaPlayer mp, int what, int extra) {
			// TODO Auto-generated method stub
			return false;
		}
	};
	private OnPreparedListener onPlayerPrepared = new OnPreparedListener() {
		
		public void onPrepared(MediaPlayer mp) {
			// TODO Auto-generated method stub
			
		}
	};
	private OnCompletionListener onPlayComplete = new OnCompletionListener() {
		
		public void onCompletion(MediaPlayer mp) {
			// TODO Auto-generated method stub
			
		}
	};
	private OnBufferingUpdateListener onSongBuffering = new OnBufferingUpdateListener() {
		
		public void onBufferingUpdate(MediaPlayer mp, int percent) {
			// TODO Auto-generated method stub
			
		}
	};
	

}
