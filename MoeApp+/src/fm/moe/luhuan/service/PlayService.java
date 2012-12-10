package fm.moe.luhuan.service;

import java.util.ArrayList;

import fm.moe.luhuan.beans.data.PlayableData;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class PlayService extends Service{
	public MediaPlayer player = new MediaPlayer();
	public ArrayList<PlayableData> playerList = new ArrayList<PlayableData>();
	private PlayerBinder binder = new PlayerBinder();
	public int nowPlayingIndex =-1;
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
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
		return binder;
	}
	
	public class PlayerBinder extends Binder{
		public PlayService getService(){
			return PlayService.this;
		}
	}

}
