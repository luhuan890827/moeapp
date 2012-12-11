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
	 * Ҫ��ʾ��ҳ��ĸ���  
	 */
	public int getCount() {
		// TODO Auto-generated method stub
		return viewList.size();
	}

	@Override
	/**  
	 * �˷����Ὣ������ָ��ҳ����Ƴ�  
	 * �÷����еĲ���container��position��instantiateItem�����е�����һ��  
	 * @param object ���object ���� instantiateItem�����з��ص��Ǹ�Object  
	 */
	public void destroyItem(ViewGroup container, int position, Object object) {
		// TODO Auto-generated method stub

		((ViewPager) container).removeView(viewList.get(position));

	}

	@Override
	/**  
	 * ��ȡһ��ָ��ҳ���title����  
	 * �������null��ζ�����ҳ��û�б��⣬Ĭ�ϵ�ʵ�־��Ƿ���null  
	 *   
	 * ���Ҫ��ʾҳ���ϵ�title��˷�������ʵ��  
	 */
	public CharSequence getPageTitle(int position) {
		// TODO Auto-generated method stub
		return titles[position];
	}

	@Override
	/**  
	 *  ����ָ��position��ҳ�档����������Ὣҳ��ӵ�����container�С�  
	 * @param container ��������ʵ���ŵ�container�У������container����viewPager  
	 * @return ����һ���ܱ�ʾ��ҳ��Ķ��󣬲�һ��Ҫ��view������������������ҳ�档  
	 */
	public Object instantiateItem(ViewGroup container, int position) {
		// TODO Auto-generated method stub
		((ViewPager) container).addView(viewList.get(position));

		return viewList.get(position);
	}
}
