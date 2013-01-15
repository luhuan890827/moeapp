package fm.moe.luhuan;

import java.net.SocketTimeoutException;
import java.util.List;

import com.alibaba.fastjson.JSON;

import fm.moe.luhuan.beans.data.SimpleData;
import fm.moe.luhuan.http.CommonHttpHelper;
import fm.moe.luhuan.http.MoeHttp;
import fm.moe.luhuan.service.PlayBackService;
import fm.moe.luhuan.utils.DataStorageHelper;
import fm.moe.luhuan.utils.MoeDbHelper;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Resources.NotFoundException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MusicBrowse extends FragmentActivity {
	private FragmentPagerAdapter mAdapter;
	private ViewPager mViewPager;
	private RelativeLayout controlSet;
	private boolean onbind;
	private IPlaybackService playbackService;
	private ImageView thumb;
	private ImageButton pp;
	private TextView title;
	private TextView artist;
	private BroadcastReceiver receiver =new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			int playerState = intent.getIntExtra(PlayBackService.EXTRA_PLAYER_STATUS, 0);
			if(playerState==PlayBackService.PLAYER_PREPARED){
				if(onbind){
					SimpleData data = null;
					try {
						data = playbackService.getCurrentItem();
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					setCTRLArea(data);
				}
			}
		}
	};
	private ServiceConnection conn = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			onbind = false;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			onbind = true;
			playbackService = IPlaybackService.Stub.asInterface(service);
			try {
				if (playbackService.isPlayerPlaying()) {
					pp.setImageDrawable(getResources().getDrawable(
							android.R.drawable.ic_media_pause));
				} else {
					pp.setImageDrawable(getResources().getDrawable(
							android.R.drawable.ic_media_play));
				}
				SimpleData data = playbackService.getCurrentItem();
				setCTRLArea(data);
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
		pp = (ImageButton) findViewById(R.id.browser_ib_pp);
		thumb = (ImageView) findViewById(R.id.browser_control_thumb);
		title = (TextView) findViewById(R.id.browser_control_title);
		artist = (TextView) findViewById(R.id.browser_control_artist);
		
	}

	@Override
	public void onBackPressed() {
		if (mViewPager.getCurrentItem() < 2) {
			RemoteContentFragment rf = (RemoteContentFragment) getCurrentFragment();
			if (!rf.backView()) {
				super.onBackPressed();
			}
		} else {
			this.finish();
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
		Log.e("", "on start");
		if (isPlaybackServiceRunning()) {
			IntentFilter iFilter = new IntentFilter();
			iFilter.addAction(PlayBackService.ACTION_PLAYER_STATE_CHANGE);
			registerReceiver(receiver, iFilter);
			controlSet.setVisibility(View.VISIBLE);
			Intent bindIntent = new Intent(this, PlayBackService.class);
			bindService(bindIntent, conn, BIND_AUTO_CREATE);
		} else {
			controlSet.setVisibility(View.GONE);
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (onbind) {
			try {
				if (playbackService.isPlayerPlaying()) {
					playbackService.setAsForeGround();
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			unbindService(conn);unregisterReceiver(receiver);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
	}

	private void setCTRLArea(final SimpleData data) {

		if (data.getArtist().equals("")) {
			artist.setText("未知艺术家");
		} else {
			artist.setText(Html.fromHtml(data.getArtist()));
		}
		title.setText(Html.fromHtml(data.getTitle()));
		new Thread() {
			public void run() {
				CommonHttpHelper http = new CommonHttpHelper();
				String thumbUrl = data.getThumbUrl();
				if (thumbUrl == null) {
					MoeHttp mHttp = new MoeHttp(getApplicationContext());
					try {
						String json = mHttp
								.oauthRequest("http://api.moefou.org/song/detail.json?sub_id="
										+ data.getId());
						thumbUrl = JSON.parseObject(json)
								.getJSONObject("response").getJSONObject("sub")
								.getJSONObject("wiki")
								.getJSONObject("wiki_cover").getString("small");
						data.setThumbUrl(thumbUrl);
						MoeDbHelper dh = new MoeDbHelper(
								getApplicationContext());
						SQLiteDatabase db = dh.getWritableDatabase();
						db.execSQL("update " + MoeDbHelper.TABLE_NAME
								+ " set thumb_path='" + thumbUrl
								+ "' where _id=" + data.getId());
						db.close();
					} catch (SocketTimeoutException e) {
						e.printStackTrace();
					}
				}
				final Bitmap bm = http.getBitmap(thumbUrl);
				thumb.post(new Runnable() {

					@Override
					public void run() {
						thumb.setImageBitmap(bm);
					}
				});
			}
		}.start();
	}
	public void addItemToPlayService(SimpleData item) throws RemoteException{
		if(onbind){
			playbackService.addItem(item);
		}
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

	private Fragment getCurrentFragment() {

		return getSupportFragmentManager().findFragmentByTag(
				"android:switcher:" + R.id.view_pager + ":"
						+ mViewPager.getCurrentItem());
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

	public void onCTRLClick(View v) {
		// Toast.makeText(this, v.getId()+"", Toast.LENGTH_SHORT).show();
		if (!onbind) {
			return;
		}
		switch (v.getId()) {
		case R.id.browser_control_info:
			Intent intent = new Intent(this, MusicPlay.class);
			startActivity(intent);
			break;
		case R.id.browser_ib_prev:
			try {
				doChangeSong(false);
			} catch (RemoteException e) {
				Log.e("", "", e);
				e.printStackTrace();
			}
			break;
		case R.id.browser_ib_pp:
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
		case R.id.browser_ib_next:
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

	private void switchPlayPause(View v) throws RemoteException,
			NotFoundException {
		if (playbackService.isPlayerPlaying()) {

			((ImageButton) v).setImageDrawable(getResources().getDrawable(
					android.R.drawable.ic_media_play));
			playbackService.pause();
			playbackService.stopAsForeGround();
		} else if (playbackService.isPlayerPrepared()) {

			((ImageButton) v).setImageDrawable(getResources().getDrawable(
					android.R.drawable.ic_media_pause));
			playbackService.start();
		}

	}

	private void doChangeSong(boolean isNext) throws RemoteException {
		if (isNext) {
			playbackService.playNext();
		} else {
			playbackService.playPrevious();
		}
	}

}
