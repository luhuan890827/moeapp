package fm.moe.luhuan;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONException;

import fm.moe.luhuan.adapter.SimpleDataAdapter;
import fm.moe.luhuan.beans.data.SimpleData;
import fm.moe.luhuan.service.PlayBackService;
import fm.moe.luhuan.service.PlayService;
import fm.moe.luhuan.utils.JSONUtils;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class ExplorePageFragment extends RemoteContentFragment {
	private SparseArray<List<SimpleData>> explorePageData;
	private static final int KEY_HOTMUSICS = 0;
	private static final int KEY_RADIOS = 1;
	private static final int PLAY_RANDOM = 2;
	private static final int KEY_MUSICS = 3;
	
	private static final String REMOTE_DATA_URL = "http://moe.fm/explore?api=json&api_key=420f4049d93b1c64f5e811187ad3364c05016179a&new_musics=1&hot_musics=1&hot_radios=1&musics=1";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		listView.setOnItemClickListener(onGroupClick);
		//Log.e("fragment", "oncreate");
		
	}

	private OnItemClickListener onGroupClick = new OnItemClickListener() {

		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			pushToStack();
			if(arg2==PLAY_RANDOM){
				final AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
				dialog.setTitle("正努力为您加载，请稍候...");
				LinearLayout ll = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.progress_view, null);
				dialog.setView(ll);
				dialog.show();
				new Thread(){
					public void run(){
						try {
							String json = http.oauthRequest(PLAY_LIST_DATA_URL);
							List<SimpleData> playList = JSONUtils.getSimpleDataFromPlayList(json);
							Intent playIntent = new Intent(getActivity(), MusicPlay.class);
							Bundle bundle = new Bundle();

							bundle.putSerializable(PlayService.EXTRA_PLAYLIST,
									(ArrayList<SimpleData>) playList);
							bundle.putInt(PlayService.EXTRA_SELECTED_INDEX, 0);
							bundle.putString(PlayService.EXTRA_PLAYLIST_ID,
									 "random");
							//bundle.putBoolean(PlayService.EXTRA_IF_NEED_NETWORK, true);
							playIntent.putExtras(bundle);
							
							startActivity(playIntent);
							getActivity().startService(playIntent.setClass(getActivity(), PlayBackService.class));

						} catch (Exception e) {
							e.printStackTrace();
							dialog.dismiss();
							Toast.makeText(getActivity(), "无法连接到服务器，请重试", Toast.LENGTH_SHORT).show();
						}
						dialog.dismiss();
					}
				}.start();
			}else
			if (explorePageData == null) {
				loadingProgress.setVisibility(View.VISIBLE);
				listView.addFooterView(loadingProgress, null, false);
				listView.setAdapter(null);
				asyncTask = new AsyncLoadExplorePage();
				asyncTask.execute(arg2);
			} else {
				SimpleDataAdapter adapter = new SimpleDataAdapter(
						getActivity(), explorePageData.get(arg2));
				listView.setAdapter(adapter);
				listView.setOnItemClickListener(onWikiClick);
			}

		}
	};

	private class AsyncLoadExplorePage extends
			AsyncTask<Object, Object, Object> {
		@Override
		protected Object doInBackground(Object... params) {

			try {
				String json = http.oauthRequest(REMOTE_DATA_URL);
				List<SimpleData> newAlbums = JSONUtils.getExpWikiList(json,
						"new_musics");
				List<SimpleData> hotRadios = JSONUtils.getExpWikiList(json,
						"hot_radios");
				List<SimpleData> musics = JSONUtils.getExpWikiList(json,
						"musics");
				List<SimpleData> hotMusics = JSONUtils.getExpWikiList(json,
						"hot_musics");
				explorePageData = new SparseArray<List<SimpleData>>(5);
				explorePageData.append(KEY_HOTMUSICS, hotMusics);
				explorePageData.append(KEY_MUSICS, musics);
				//explorePageData.append(KEY_NEWMUSICS, newAlbums);
				explorePageData.append(KEY_RADIOS, hotRadios);
				
			} catch (SocketTimeoutException e) {
				sendErrToast("网络连接超时");
			} catch (JSONException e) {
				sendErrToast("请求参数出错");
			} catch (Exception e) {
				Log.e("", "", e);
				sendErrToast("无法连接到服务器");
			}
			return params[0];
		}

		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			if (explorePageData != null) {
				SimpleDataAdapter adapter = new SimpleDataAdapter(
						getActivity(), explorePageData.get((Integer) result));
				listView.setAdapter(adapter);
				listView.removeFooterView(loadingProgress);
				listView.setOnItemClickListener(onWikiClick);
			} else {
				backView();
			}

		}
	}

}
