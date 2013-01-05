package fm.moe.luhuan;

import java.util.ArrayList;
import java.util.List;

import fm.moe.luhuan.adapter.MyCursorAdapter;
import fm.moe.luhuan.beans.data.SimpleData;
import fm.moe.luhuan.utils.MoeDbHelper;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class DownloadedPageFragment extends Fragment{
	private SQLiteDatabase db;
	private ListView listView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		listView = new ListView(getActivity());
		MoeDbHelper dbHelper = new MoeDbHelper(getActivity());
		db = dbHelper.getWritableDatabase();
		Cursor cursor = db.rawQuery("select * from " + MoeDbHelper.TABLE_NAME
				+ " order by insert_time", null);

		ListAdapter adapter = new MyCursorAdapter(getActivity(), cursor, false);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(onItemClick);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return listView;
	}
	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
		((ViewGroup)listView.getParent()).removeView(listView);;
	}
	@Override
	public void onStop() {
		super.onStop();
		db.close();
	}
	private OnItemClickListener onItemClick = new OnItemClickListener() {

		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			Cursor c = ((CursorAdapter)arg0.getAdapter()).getCursor();
			c.moveToFirst();
			c.moveToPrevious();
			List<SimpleData> l = new ArrayList<SimpleData>();
			
			while(c.moveToNext()){
				SimpleData data = new SimpleData();
				data.setAlbumnCoverUrl(c.getString(7));
				data.setArtist(c.getString(2));
				if(c.getLong(5)==1){
					data.setFav(true);
				}
				data.setTitle(c.getString(1));
				data.setId(c.getInt(0));
				data.setMp3Url(c.getString(6));
				data.setParentId(c.getInt(3));
				data.setParentTitle(c.getString(4));
				data.setAlbumnCoverUrl(c.getString(7));
				l.add(data);
			}
			Intent playIntent = new Intent(getActivity(), MusicPlay.class);
			Bundle bundle = new Bundle();

			bundle.putSerializable("playList", (ArrayList<SimpleData>) l);
			bundle.putInt("selectedIndex", arg2);
			bundle.putString("playListId", arg0.getTag(R.string.play_list_id)
					+ "");

			playIntent.putExtras(bundle);

			startActivity(playIntent);
		}
	};
}
