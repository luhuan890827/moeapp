package fm.moe.luhuan;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;


public class MusicBrowse extends FragmentActivity {
	private FragmentPagerAdapter mAdapter;
	private ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.music_browse);
		mViewPager = (ViewPager) findViewById(R.id.view_pager);
		mAdapter = new MyPagerAdapter(getSupportFragmentManager(),
				new String[] { "发现音乐", "我的收藏", "已下载音乐" });

		mViewPager.setAdapter(mAdapter);
		
		//mViewPager.setOnKeyListener(onKey);
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

}
