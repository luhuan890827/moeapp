package fm.moe.luhuan.asynctask;

import android.os.AsyncTask;
import android.widget.ListAdapter;

public class AsyncReadExploreGroup extends AsyncTask<Object, Object, ListAdapter>{

	@Override
	protected ListAdapter doInBackground(Object... params) {
		String url = "http://moe.fm/explore?api=json&api_key=420f4049d93b1c64f5e811187ad3364c05016179a&new_musics=1&hot_musics=1&hot_radios=1&musics=1";
		
		return null;
	}

}
