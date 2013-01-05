package fm.moe.luhuan;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;



public class FavPageFragment extends RemoteContentFragment{
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onViewCreated(view, savedInstanceState);
		listView.setOnItemClickListener(onGroupClick);
		
	}
	private OnItemClickListener onGroupClick = new OnItemClickListener() {

		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			Toast.makeText(getActivity(), "fav frag", Toast.LENGTH_SHORT).show();
		}
	};
}
