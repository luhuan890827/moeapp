package fm.moe.luhuan.activities;

import fm.moe.luhuan.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.util.Log;


public class AppPref extends PreferenceActivity {
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		getPreferenceManager().setSharedPreferencesName("app_settings");
		addPreferencesFromResource(R.xml.app_settings);
		getPreferenceScreen().getPreference(2).setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			public boolean onPreferenceClick(Preference preference) {
				// TODO Auto-generated method stub
				//Log.e("!!", "!!");
				AlertDialog.Builder aBuilder = new Builder(AppPref.this);
				AlertDialog aDialog = aBuilder.create();
				aDialog.setTitle("�����Ȩ��Ϣ");
				aDialog.setMessage("ȷ��Ҫ�����Ȩ��Ϣ��");
				aDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "ȡ��", new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.dismiss();
						
					}
				});
				aDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ȷ��", new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.dismiss();
						Editor editor = getSharedPreferences("token", MODE_PRIVATE).edit();
						editor.clear();
						editor.commit();
						Intent intent = new Intent(AppPref.this	,AppInit.class);
						startActivity(intent);
						finish();
						
					}
				});
				aDialog.show();
				return false;
			}
		});
	}
}
