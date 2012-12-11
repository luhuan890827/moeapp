package fm.moe.luhuan.adapters;

import java.util.List;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class MyViewPagerAdapter extends PagerAdapter {
	private List<LinearLayout> viewList;
	private String[] titles;

	public MyViewPagerAdapter(List<LinearLayout> views, String[] str) {
		viewList = views;
		titles = str;
	}

	public boolean isViewFromObject(View arg0, Object arg1) {
		// TODO Auto-generated method stub
		return arg0 == arg1;
	}

	@Override
	/**  
	 * 要显示的页面的个数  
	 */
	public int getCount() {
		// TODO Auto-generated method stub
		return viewList.size();
	}

	@Override
	/**  
	 * 此方法会将容器中指定页面给移除  
	 * 该方法中的参数container和position跟instantiateItem方法中的内容一致  
	 * @param object 这个object 就是 instantiateItem方法中返回的那个Object  
	 */
	public void destroyItem(ViewGroup container, int position, Object object) {
		// TODO Auto-generated method stub

		((ViewPager) container).removeView(viewList.get(position));

	}

	@Override
	/**  
	 * 获取一个指定页面的title描述  
	 * 如果返回null意味着这个页面没有标题，默认的实现就是返回null  
	 *   
	 * 如果要显示页面上的title则此方法必须实现  
	 */
	public CharSequence getPageTitle(int position) {
		// TODO Auto-generated method stub
		return titles[position];
	}

	@Override
	/**  
	 *  创建指定position的页面。这个适配器会将页面加到容器container中。  
	 * @param container 创建出的实例放到container中，这里的container就是viewPager  
	 * @return 返回一个能表示该页面的对象，不一定要是view，可以其他容器或者页面。  
	 */
	public Object instantiateItem(ViewGroup container, int position) {
		// TODO Auto-generated method stub
		((ViewPager) container).addView(viewList.get(position));

		return viewList.get(position);
	}
}
