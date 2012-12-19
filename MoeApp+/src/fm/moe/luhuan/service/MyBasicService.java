package fm.moe.luhuan.service;

import java.io.File;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.support.v4.content.LocalBroadcastManager;


public abstract class MyBasicService extends Service{
	private LocalBroadcastManager broadcastManager;
	private Intent broadcast = new Intent();
	private File mp3Dir;
	private boolean onWifi = false;
	private boolean onConnect = true;
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		broadcastManager = LocalBroadcastManager
				.getInstance(getApplicationContext());

		mp3Dir = new File(getExternalFilesDir(null), "song");
		if (!mp3Dir.exists()) {
			mp3Dir.mkdir();
		}

		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (!netInfo.isConnected()) {
			onConnect = false;
		} else if (!netInfo.isAvailable()) {
			onConnect = false;
		} else {
			onConnect = true;
		}
		WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		if (wifiManager.isWifiEnabled()) {
			onWifi = true;
		} else {
			onWifi = false;
		}
		
		
		
	}
}
