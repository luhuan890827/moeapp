package fm.moe.luhuan;

import fm.moe.luhuan.R;
import fm.moe.luhuan.service.PlayService;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class PlayerControlFragment extends Fragment{
	
	
	private ImageButton btn_prev;
	private ImageButton btn_pp;
	private ImageButton btn_next;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View v = inflater.inflate(R.layout.player_control, null);
		v.setVisibility(View.GONE);
		return v;
	}
	
}
