package fm.moe.luhuan;

import java.net.SocketTimeoutException;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import fm.moe.luhuan.adapter.SimpleDataAdapter;
import fm.moe.luhuan.beans.data.SimpleData;
import fm.moe.luhuan.utils.JSONUtils;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import android.widget.AdapterView.OnItemClickListener;

public class FavPageFragment extends RemoteContentFragment {
	private final String MY_FAV_ALBUMS_URL="http://api.moefou.org/user/favs/wiki.json?obj_type=music&fav_type=1&perpage=25";
	private final String MY_FAV_RADIOS_URL="http://api.moefou.org/user/favs/wiki.json?obj_type=radio&fav_type=1&perpage=25";
	private final String MY_FAV_SONGS_URL="http://api.moefou.org/user/favs/sub.json?obj_type=song&fav_type=1&perpage=25";
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onViewCreated(view, savedInstanceState);
		listView.setOnItemClickListener(onGroupClick);

	}

	private OnItemClickListener onGroupClick = new OnItemClickListener() {

		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			pushToStack();
			listView.addFooterView(loadingProgress, null, false);
			listView.setAdapter(null);
			asyncTask = new LoadMyFavs();
			asyncTask.execute(arg2);
		
		}
	};
	class LoadMyFavs extends AsyncTask<Object, Object, List<SimpleData>>{
		int clickedLineNum ;
		JSONObject information;
		String url;
		@Override
		protected List<SimpleData> doInBackground(Object... params) {
			clickedLineNum = (Integer) params[0];
			
			String type = null;
			String json = null;
			List<SimpleData> list = null;
			try{
				if(clickedLineNum==0){
					url = MY_FAV_ALBUMS_URL;
					type="wiki";
				}else if(clickedLineNum==1){
					url=MY_FAV_RADIOS_URL;
					type="wiki";
				}else{
					type="sub";
					url=MY_FAV_SONGS_URL;
				}
				json = http.oauthRequest(url);
				list = JSONUtils.getFavs(json, type);
				information = JSON.parseObject(json).getJSONObject("response").getJSONObject("information");
				
			}catch(SocketTimeoutException e){
				sendErrToast("网络连接超时");
			}catch (Exception e) {
				Log.e("", "",e);
				sendErrToast("未知错误");
			}
			return list;
		}
	@Override
	protected void onPostExecute(List<SimpleData> result) {
		super.onPostExecute(result);
		if(result!=null){
			listView.removeFooterView(loadingProgress);
			int totalCount = information.getIntValue("count");
			int countPerPage = information.getIntValue("perpage");
			int pageIndex = information.getIntValue("page");
			if(totalCount>countPerPage*pageIndex){
				String nextUrl = url+"&page="+(pageIndex+1);
				loadMoreBtn.setTag(R.string.more_btn_url,nextUrl);
				loadMoreBtn.setVisibility(View.VISIBLE);
				listView.addFooterView(loadMoreBtn);
			}
			SimpleDataAdapter adapter = new SimpleDataAdapter(getActivity(), result);
			listView.setAdapter(adapter);
			if(clickedLineNum<2){
				listView.setOnItemClickListener(onWikiClick);
			}else{
				listView.setOnItemClickListener(onPlaylistItemClick);
			}
		}else{
			backView();
		}
	}	
	
	}
}
