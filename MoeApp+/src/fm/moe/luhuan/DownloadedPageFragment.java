package fm.moe.luhuan;

import java.util.ArrayList;
import java.util.List;

import fm.moe.luhuan.adapter.MyCursorAdapter;
import fm.moe.luhuan.beans.data.SimpleData;
import fm.moe.luhuan.service.PlayBackService;
import fm.moe.luhuan.utils.AppContextUtils;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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

		dialog = AppContextUtils.createSimpleDialogListMenu(getActivity(),
				android.R.drawable.btn_dialog, "操作", R.layout.dialog_item,
				new String[] { "删除", "全部删除", "添加到当前列表" }, onDialogItemClick);
		Animation anim = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in);
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

		public void onItemClick(final AdapterView<?> arg0, View arg1,
				final int arg2, long arg3) {
			new Thread() {
				public void run() {
					Intent playIntent = new Intent(getActivity(),
							MusicPlay.class);
					Bundle bundle = new Bundle();

					bundle.putSerializable("playList",
							(ArrayList<SimpleData>) dataHelper
									.getDownloadedList());
					bundle.putInt("selectedIndex", arg2);
					bundle.putString("playListId",
							arg0.getTag(R.string.play_list_id) + "");

					playIntent.putExtras(bundle);

					startActivity(playIntent);
					getActivity().startService(
							playIntent.setClass(getActivity(),
									PlayBackService.class));
				}
			}.start();

		}
	};
	private OnItemLongClickListener onImteLongClick = new OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
				int arg2, long arg3) {
			dialog.show();
			pickedItemId = (Integer) arg1.getTag();

			return true;
		}
	};
	private OnItemClickListener onDialogItemClick = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			switch (arg2) {
			case 0:
				new Thread() {
					public void run() {
						if (dataHelper.deleteItemById(pickedItemId)) {

							final CursorAdapter adapter = (CursorAdapter) listView
									.getAdapter();

							if (!db.isOpen()) {
								MoeDbHelper dbHelper = new MoeDbHelper(
										getActivity());
								db = dbHelper.getWritableDatabase();
							}
							
							listView.post(new Runnable() {

								@Override
								public void run() {
									adapter.changeCursor(db.rawQuery("select * from "
											+ MoeDbHelper.TABLE_NAME
											+ " order by insert_time", null));
									Toast.makeText(getActivity(), "删除成功",
											Toast.LENGTH_SHORT).show();

								}
							});
						}
					}
				}.start();

				break;
			case 1:

				break;
			case 2:
				new Thread() {
					public void run() {
						final SimpleData item = dataHelper.getItemById(pickedItemId);
						listView.post( new Runnable() {
							public void run() {
								try {
									((MusicBrowse) getActivity())
											.addItemToPlayService(item);
								} catch (RemoteException e) {
									Log.e("", "", e);
									e.printStackTrace();
								}
							}
						});
						
					}
				}.start();

				break;
			default:
				break;

			}
			dialog.dismiss();
		}
	};
}
