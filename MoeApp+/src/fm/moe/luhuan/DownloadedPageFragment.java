package fm.moe.luhuan;

import java.util.ArrayList;
import java.util.List;

import fm.moe.luhuan.adapter.MyCursorAdapter;
import fm.moe.luhuan.beans.data.SimpleData;
import fm.moe.luhuan.utils.DataStorageHelper;
import fm.moe.luhuan.utils.MoeDbHelper;
import android.app.AlertDialog;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.Fragment;

import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class DownloadedPageFragment extends Fragment {
	private SQLiteDatabase db;
	private ListView listView;
	private AlertDialog dialog;
	private int pickedItemId;
	private DataStorageHelper dataHelper;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dataHelper = new DataStorageHelper(getActivity());
		listView = new ListView(getActivity());
		MoeDbHelper dbHelper = new MoeDbHelper(getActivity());
		db = dbHelper.getWritableDatabase();
		Cursor cursor = db.rawQuery("select * from " + MoeDbHelper.TABLE_NAME
				+ " order by insert_time", null);
		ListAdapter adapter = new MyCursorAdapter(getActivity(), cursor, false);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(onItemClick);
		listView.setOnItemLongClickListener(onImteLongClick);
		
		dialog = new AlertDialog.Builder(getActivity()).create();
		ArrayAdapter<String> dialogAdapter = new ArrayAdapter<String>(
				getActivity(), R.layout.dialog_item);
		dialogAdapter.add("删除");
		dialogAdapter.add("删除全部");
		dialogAdapter.add("添加到当前播放列表");
		dialog.setTitle("操作");
		dialog.setIcon(android.R.drawable.btn_dialog);
		ListView dialogList = new ListView(getActivity());
		dialogList.setAdapter(dialogAdapter);
		dialogList.setOnItemClickListener(onDialogItemClick);
		dialog.setView(dialogList);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return listView;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		((ViewGroup) listView.getParent()).removeView(listView);
	}

	@Override
	public void onStop() {
		super.onStop();
		db.close();
	}

	private OnItemClickListener onItemClick = new OnItemClickListener() {

		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			Intent playIntent = new Intent(getActivity(), MusicPlay.class);
			Bundle bundle = new Bundle();

			bundle.putSerializable("playList", (ArrayList<SimpleData>) dataHelper.getDownloadedList());
			bundle.putInt("selectedIndex", arg2);
			bundle.putString("playListId", arg0.getTag(R.string.play_list_id)
					+ "");

			playIntent.putExtras(bundle);

			startActivity(playIntent);
		}
	};
	private OnItemLongClickListener onImteLongClick = new OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
				int arg2, long arg3) {
			dialog.show();
			pickedItemId = (Integer) arg1.getTag();
			arg0.getSelectedItem();
			return true;
		}
	};
	private OnItemClickListener onDialogItemClick = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			switch (arg2) {
			case 0:
				if(dataHelper.deleteItemById(pickedItemId)){
					Toast.makeText(getActivity(), "删除成功", Toast.LENGTH_SHORT).show();
					CursorAdapter adapter = (CursorAdapter) listView.getAdapter();
					
					if(!db.isOpen()){
						MoeDbHelper dbHelper = new MoeDbHelper(getActivity());
						db = dbHelper.getWritableDatabase();
					}
					adapter.changeCursor(db.rawQuery("select * from " + MoeDbHelper.TABLE_NAME
							+ " order by insert_time", null));
					
				}
				
				break;
			case 1:

				break;
			case 2:
//				SimpleData item = dataHelper.getItemById(pickedItemId);
//				try {
//					((MusicBrowse)getActivity()).addItemToPlayService(item);
//				} catch (RemoteException e) {
//					Log.e("", "",e);
//					e.printStackTrace();
//				}
				
				break;
			default:
				break;
				
			}
			dialog.dismiss();
		}
	};
}
