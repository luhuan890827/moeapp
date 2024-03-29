package fm.moe.luhuan.service;

import java.util.ArrayList;
import java.util.List;

import android.R;
import android.app.Notification;
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
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.util.Log;
import fm.moe.luhuan.IPlaybackService;
import fm.moe.luhuan.MusicPlay;
import fm.moe.luhuan.beans.data.SimpleData;
import fm.moe.luhuan.utils.DataStorageHelper;

public class PlayBackService extends Service{
	private MediaPlayer mPlayer;
	private NotificationManager ntfManager;
	private ArrayList<SimpleData> playList;
	private String playListId;
	private int nowIndex;
	private DataStorageHelper dataHelper;
	private int sID;
	private NotificationCompat.Builder nBuilder;
	private boolean isPrepared;
	private Notification mNotification;
	private PendingIntent pIntent;
	
	private static final int NOTIFICATION_ID =PlayBackService.class.hashCode();
	public static final String ACTION_START_WITH_ITEM = "with one item";
	public static final String ACTION_PLAYER_STATE_CHANGE = "player state change";
	public static final String EXTRA_PLAY_ITEM = "the item";
	public static final String EXTRA_PLAYLIST = "playList";
	public static final String EXTRA_PLAYLIST_ID = "playListId";
	public static final String EXTRA_SELECTED_INDEX = "selectedIndex";
	public static final String EXTRA_PLAYER_STATUS = "player state";
	public static final String EXTRA_PLAYER_BUFFERING_PERCENT="buffering percent";
	public static final int PLAYER_PREPARED = 1;
	public static final int PLAYER_BURRERING = 2;
	public static final int PLAYER_COMPLETION =3;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		mPlayer = new MediaPlayer();
		setPlayerListeners();
		ntfManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		
		nBuilder = new NotificationCompat.Builder(this);
		Intent intent = new Intent(this,MusicPlay.class);
		pIntent = PendingIntent.getActivity(this, 0, intent, Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		nBuilder.setContentIntent(pIntent);
		dataHelper = new DataStorageHelper(this);
		IntentFilter intentFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
		registerReceiver(receiver, intentFilter);
		
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String action = intent.getAction();
		
		if(action!=null){
			playList = new ArrayList<SimpleData>();
			playList.add((SimpleData) intent.getParcelableExtra(EXTRA_PLAY_ITEM));
			nowIndex=0;
			playSongAtIndex(0);
			
		}else{
			Bundle data = intent.getExtras();
			playList = (ArrayList<SimpleData>) data.get(EXTRA_PLAYLIST);
			nowIndex = data.getInt(EXTRA_SELECTED_INDEX);
			playListId = data.getString(EXTRA_PLAYLIST_ID);
			playSongAtIndex(nowIndex);
		}
		SimpleData item = playList.get(nowIndex);
		mNotification = buildNotification(R.drawable.ic_media_play, item.getTitle(), "���ڲ���", item.getTitle());
		ntfManager.notify(NOTIFICATION_ID, mNotification);
		startForeground(sID, mNotification);
		//ntfManager.cancelAll();
		//
		sID = startId;
		
		return START_NOT_STICKY;
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
		mPlayer.release();
	}
	
	private void setPlayerListeners() {
		mPlayer.setOnBufferingUpdateListener(onSongBuffering);
		mPlayer.setOnErrorListener(onPlayerErr);
		mPlayer.setOnPreparedListener(onPlayerPrepared);
		mPlayer.setOnCompletionListener(onPlayComplete);
		mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
	}
	private void playSongAtIndex(int n){
		
		mPlayer.reset();
		SimpleData item = playList.get(n);
		mNotification = buildNotification(R.drawable.ic_media_play, item.getTitle(), "���ڲ���", item.getTitle());
		startForeground(sID, mNotification);
		isPrepared = false;
		String url = "";
		try{
			url = dataHelper.getItemMp3Url(item);
			mPlayer.setDataSource(url);
			isPrepared = false;
		}catch(Exception e){
			Log.e("", "",e);
		}
		
		mPlayer.prepareAsync();
		nowIndex = n;
	}
	private Notification buildNotification(int drawableId, String tickerText,
			String title, String content) {
		ntfManager.cancelAll();
		nBuilder.setSmallIcon(drawableId);
		if(tickerText!=null){
			nBuilder.setTicker("�ȷ�����:" + Html.fromHtml(tickerText));	
		}
		
		nBuilder.setContentTitle("�ȷ�����:" + Html.fromHtml(title));
		nBuilder.setContentText(Html.fromHtml(content));
		
		return nBuilder.build();
		
		
		
	}
	
	public class PlayBackServiceImpl extends IPlaybackService.Stub {

		@Override
		public int getNowIndex() throws RemoteException {
			return nowIndex;
		}

		@Override
		public int getSongDuration() throws RemoteException {
			
			return mPlayer.getDuration();
		}

		@Override
		public int getSongCurrentPosition() throws RemoteException {
			return mPlayer.getCurrentPosition();
		}

		@Override
		public void playSongAtIndex(int n) throws RemoteException {
			PlayBackService.this.playSongAtIndex(n);
		}

		@Override
		public void pause() throws RemoteException {
			mPlayer.pause();
			stopForeground(true);
		}

		@Override
		public void start() throws RemoteException {
			mPlayer.start();
			SimpleData item = playList.get(nowIndex);
			mNotification = buildNotification(R.drawable.ic_media_play, null, "���ڲ���", item.getTitle());
			startForeground(sID, mNotification);
		}

		@Override
		public boolean isPlayerPrepared() throws RemoteException {
			
			return isPrepared;
		}

		@Override
		public boolean isPlayerPlaying() throws RemoteException {
			return mPlayer.isPlaying();
		}


		@Override
		public void seekTo(int n) throws RemoteException {
			mPlayer.seekTo(n)	;
		}

//		@Override
//		public void setAsForeGround() throws RemoteException {
//			PlayBackService.this.setAsForeGround();
//		}
//
//		@Override
//		public void stopAsForeGround() throws RemoteException {
//			PlayBackService.this.stopForeground(true);
//		}

		@Override
		public List<SimpleData> getList() throws RemoteException {
			return playList;
		}

		@Override
		public void playNext() throws RemoteException {
			if(nowIndex<playList.size()-1){
				PlayBackService.this.playSongAtIndex(nowIndex+1);
			}else{
				PlayBackService.this.playSongAtIndex(0);
			}
		}

		@Override
		public void playPrevious() throws RemoteException {
			if(nowIndex==0){
				PlayBackService.this.playSongAtIndex(playList.size()-1);
			}else{
				PlayBackService.this.playSongAtIndex(nowIndex-1);
			}
		}

		@Override
		public SimpleData getCurrentItem() throws RemoteException {
			return playList.get(nowIndex);
		}

		@Override
		public int getListSize() throws RemoteException {
			return playList.size();
		}

		@Override
		public void addItem(SimpleData item) throws RemoteException {
			playList.add(item);
		}

		@Override
		public void removeItem(int index) throws RemoteException {
			playList.remove(index);
		}

		@Override
		public void clearList() throws RemoteException {
			playList.clear();
		}

		@Override
		public void randPlay() throws RemoteException {
			int size = playList.size();
			
			ArrayList<SimpleData > newList = new ArrayList<SimpleData>(size);
			for(int i = 0;i<playList.size();i++){
				int temp=0;
				SimpleData item = null;
				while(item==null){
					temp = (int) (size*Math.random());
					item = playList.get(temp);
				}
				playList.set(temp, null);
				newList.add(item);
			}
			playList = newList;
			PlayBackService.this.playSongAtIndex(0);
		}
		
		
	}

	@Override
	public IBinder onBind(Intent intent) {
		
		
		return new PlayBackServiceImpl();
	}
	@Override
	public boolean onUnbind(Intent intent) {
		
		return super.onUnbind(intent);
	}
	private OnErrorListener onPlayerErr = new OnErrorListener() {

		public boolean onError(MediaPlayer mp, int what, int extra) {
			
			Log.e("playbackservice", "what="+what+",extra="+extra);
			return false;
		}
	};
	private OnPreparedListener onPlayerPrepared = new OnPreparedListener() {

		public void onPrepared(MediaPlayer mp) {
			isPrepared = true;
			mp.start();
			
			Intent broadcast = new Intent(ACTION_PLAYER_STATE_CHANGE);
			broadcast.putExtra(EXTRA_PLAYER_STATUS, PLAYER_PREPARED);
			sendBroadcast(broadcast);
			// Log.e("player", "prepare");
		}
	};
	
	private OnCompletionListener onPlayComplete = new OnCompletionListener() {

		public void onCompletion(MediaPlayer mp) {
			if (playList.size() - 1 > nowIndex) {
				playSongAtIndex(++nowIndex);
			}else{
				stopForeground(true);
			}
			Intent broadcast = new Intent(ACTION_PLAYER_STATE_CHANGE);
			broadcast.putExtra(EXTRA_PLAYER_STATUS, PLAYER_COMPLETION);
			sendBroadcast(broadcast);

			
			// Log.e("broadcast", "send");

		}
	};
	private OnBufferingUpdateListener onSongBuffering = new OnBufferingUpdateListener() {

		public void onBufferingUpdate(MediaPlayer mp, int percent) {
			Intent broadcast = new Intent(ACTION_PLAYER_STATE_CHANGE);
			broadcast.putExtra(EXTRA_PLAYER_STATUS, PLAYER_BURRERING);
			broadcast.putExtra(EXTRA_PLAYER_BUFFERING_PERCENT, percent);
			sendBroadcast(broadcast);
			//Log.e("buffered update", percent + "");

		}
	};
	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			//Intent.ACTION_HEADSET_PLUG
			if (intent.getIntExtra("state", 1) == 0) {
				if(mPlayer.isPlaying()){
					mPlayer.pause();
					SimpleData item = playList.get(nowIndex);
					mNotification = buildNotification(R.drawable.ic_media_pause, null, "��������ͣ", item.getTitle());
					startForeground(sID, mNotification);
					
				}
			}

		}
	};

	

	
	
}
