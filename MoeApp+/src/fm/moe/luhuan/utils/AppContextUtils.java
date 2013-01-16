package fm.moe.luhuan.utils;

import java.util.List;

import fm.moe.luhuan.R;
import fm.moe.luhuan.service.PlayBackService;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class AppContextUtils {
	public static boolean isPlaybackServiceRunning(Context c) {
		ActivityManager manager = (ActivityManager) c
				.getSystemService(Context.ACTIVITY_SERVICE);
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
	public static AlertDialog createSimpleDialogListMenu(Context c,int iconId,String title,int layoutId,String[] tags,OnItemClickListener onOptionClick){
		AlertDialog dialog = new AlertDialog.Builder(c).create();
		ArrayAdapter<String> dialogAdapter = new ArrayAdapter<String>(
				c, R.layout.dialog_item);
		for(int i = 0;i<tags.length;i++){
			dialogAdapter.add(tags[i]);
		}
		dialog.setTitle(title);
		dialog.setIcon(iconId);
		ListView dialogList = new ListView(c);
		dialogList.setAdapter(dialogAdapter);
		dialogList.setOnItemClickListener(onOptionClick);
		dialog.setView(dialogList);
		return dialog;
	}
}
