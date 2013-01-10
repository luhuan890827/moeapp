package fm.moe.luhuan;

import java.util.List;

import fm.moe.luhuan.beans.data.SimpleData;
import fm.moe.luhuan.service.PlayBackService;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;


public class MusicBrowse extends FragmentActivity {
	private FragmentPagerAdapter mAdapter;
	private ViewPager mViewPager;
	private RelativeLayout controlSet;
	private boolean onbind;
	private IPlaybackService playbackService;
	private ImageView thumb;
	private ImageButton pp;
	private ServiceConnection conn = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			onbind = false;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			onbind = true;
			playbackService =  IPlaybackService.Stub.asInterface(service);
			try {
				if(playbackService.isPlayerPlaying()){
					pp.setImageDrawable(getResources()
							.getDrawable(android.R.drawable.ic_media_pause));
				}else{
					pp.setImageDrawable(getResources()
							.getDrawable(android.R.drawable.ic_media_play));
				}
				SimpleData data = playbackService.getItem();
				data.toString();
			} catch (RemoteException e) {
				Log.e("", "", e);
				e.printStackTrace();
			}
		}
	};
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.music_browse);
		mViewPager = (ViewPager) findViewById(R.id.view_pager);
		mAdapter = new MyPagerAdapter(getSupportFragmentManager(),
				new String[] { "发现音乐", "我的收藏", "已下载音乐" });

		mViewPager.setAdapter(mAdapter);
		controlSet = (RelativeLayout) findViewById(R.id.browser_control);
		pp = (ImageButton) findViewById(R.id.browse_ib_pp);
		//thumb = (ImageView) findViewById(R.id.browser_control_thumb);
	}
	
	@Override
	public void onBackPressed() {
		if(mViewPager.getCurrentItem()<2){
			RemoteContentFragment rf = (RemoteContentFragment) getCurrentFragment();
			if(!rf.backView()){
				super.onBackPressed();
			}
		}else{
			super.onBackPressed();
		}
		
		
	}
	@Override
	public boolean onSearchRequested() {
		startSearch(null, false, null, false);

		return true;
	}
	@Override
	protected void onStart() {
		super.onStart();
		if(isPlaybackServiceRunning()){
			controlSet.setVisibility(View.VISIBLE);
			Intent bindIntent = new Intent(this, PlayBackService.class);
			bindService(bindIntent, conn, BIND_AUTO_CREATE);
		}else{
			controlSet.setVisibility(View.GONE);
		}
		
	}
	@Override
	protected void onStop() {
		super.onStop();
		if(onbind){
			unbindService(conn);
		}
	}
	private boolean isPlaybackServiceRunning(){
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		List<RunningServiceInfo> services = manager.getRunningServices(Integer.MAX_VALUE);
		String mServiceName = PlayBackService.class.getName();
		for(int i = 0;i<services.size();i++){
			if(services.get(i).service.getClassName().equals(mServiceName)){
				return true;	
			}
		}
		
		return false;
	}
	private Fragment getCurrentFragment(){
		
		return getSupportFragmentManager().findFragmentByTag( "android:switcher:"+R.id.view_pager+":"+mViewPager.getCurrentItem());
	}
	class MyPagerAdapter extends FragmentPagerAdapter {
		String[] titles;
		FragmentManager fm;
		public MyPagerAdapter(FragmentManager manager, String[] strs) {
			super(manager);
			titles = strs;
			fm = manager;
		}

		@Override
		public Fragment getItem(int arg0) {
			
			Fragment f = null;
			
			Bundle args = new Bundle();
			switch (arg0) {
			case 0:
				f = new ExplorePageFragment();
				args.putStringArray(RemoteContentFragment.EXTRA_GROUP_TAGS,
						new String[] { "音乐热榜>>>", "精选电台>>>", "魔力播放>>>" });
				break;
			case 1:
				f = new FavPageFragment();
				args.putStringArray(RemoteContentFragment.EXTRA_GROUP_TAGS,
						new String[] { "收藏的专辑>>", "收藏的电台>>", "喜欢的歌曲>>" });
				break;
			case 2:

				f = new DownloadedPageFragment();
				break;
			default:
				break;
			}
			
			f.setArguments(args);
			return f;
		}

		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return titles[position];
		}
	}
	public void onCTRLClick(View v){
		//Toast.makeText(this, v.getId()+"", Toast.LENGTH_SHORT).show();
		if(!onbind){
			return;
		}
		switch (v.getId()) {
		case R.id.browser_control_thumb:
			Intent intent = new Intent(this, MusicPlay.class);
			startActivity(intent);
			break;
		case R.id.browse_ib_prev:
			try {
				doChangeSong(false);
			} catch (RemoteException e) {
				Log.e("", "", e);
				e.printStackTrace();
			}
			break;
		case R.id.browse_ib_pp:
			try {
				switchPlayPause(v);
			} catch (RemoteException e) {
				Log.e("", "", e);
				e.printStackTrace();
			} catch (NotFoundException e) {
				Log.e("", "", e);
				e.printStackTrace();
			}
			break;
		case R.id.browse_ib_next:
			try {
				doChangeSong(true);
			} catch (RemoteException e) {
				Log.e("", "", e);
				e.printStackTrace();
			}
			break;
		default:
			break;
		}
	}

	private void switchPlayPause(View v) throws RemoteException, NotFoundException {
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

	private void doChangeSong(boolean isNext) throws RemoteException {
		if(isNext){
			playbackService.playNext();
		}else{
			playbackService.playPrevious();
		}
	}

}
