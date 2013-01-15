package fm.moe.luhuan;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.Executor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import fm.moe.luhuan.adapter.SimpleDataAdapter;
import fm.moe.luhuan.beans.data.SimpleData;
import fm.moe.luhuan.http.MoeHttp;
import fm.moe.luhuan.service.PlayService;
import fm.moe.luhuan.utils.JSONUtils;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.WrapperListAdapter;

public abstract class RemoteContentFragment extends Fragment {
	protected NetworkInfo netInfo;
	protected LinearLayout loadingProgress;
	protected LayoutInflater inflater;
	protected MoeHttp http;
	protected LinearLayout loadMoreBtn;
	protected Handler mHandler = new Handler();
	protected ListView listView;
	protected Stack<ListViewDataset> backStack = new Stack<ListViewDataset>();
	protected AsyncTask asyncTask;

	private final String PLAY_LIST_DATA_URL = "http://moe.fm/listen/playlist?api=json&";
	public static final String EXTRA_GROUP_TAGS = "group tags";

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		netInfo = ((ConnectivityManager) getActivity().getSystemService(
				Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
		inflater = getActivity().getLayoutInflater();
		loadingProgress = (LinearLayout) inflater.inflate(
				R.layout.progress_view, null, false);
		loadMoreBtn = (LinearLayout) inflater.inflate(R.layout.load_more_view,
				null, false);

		http = new MoeHttp(getActivity());
		listView = new ListView(getActivity());
		loadMoreBtn.setOnClickListener(onLoadMoreBtnClick);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		Bundle args = getArguments();
		if (args != null) {
			String[] tags = args.getStringArray(EXTRA_GROUP_TAGS);
			if (tags != null) {
				ListAdapter adapter = new ArrayAdapter<String>(getActivity(),
						R.layout.big_text_item, tags);

				listView.setAdapter(adapter);
			}
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		return listView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		// Log.e("frag", "tag="+this.getTag());
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		this.getView().setOnKeyListener(null);
		((ViewGroup) listView.getParent()).removeView(listView);
	}

	protected OnItemClickListener onWikiClick = new OnItemClickListener() {

		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// check for connectivity
			pushToStack();
			listView.addFooterView(loadingProgress);
			listView.setAdapter(null);
			asyncTask = new LoadWikiContent();
			asyncTask.execute(arg1);

		}
	};
	protected OnItemClickListener onPlaylistItemClick = new OnItemClickListener() {

		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			SimpleDataAdapter adapter = null;
			String className = arg0.getAdapter().getClass().getName();
			if (className.indexOf("HeaderViewListAdapter") < 0) {
				adapter = (SimpleDataAdapter) arg0.getAdapter();
			} else {
				HeaderViewListAdapter hAdapter = (HeaderViewListAdapter) arg0
						.getAdapter();
				adapter = (SimpleDataAdapter) hAdapter.getWrappedAdapter();
			}
			List<SimpleData> playList = adapter.getData();
			Intent playIntent = new Intent(getActivity(), MusicPlay.class);
			Bundle bundle = new Bundle();

			bundle.putSerializable(PlayService.EXTRA_PLAYLIST,
					(ArrayList<SimpleData>) playList);
			bundle.putInt(PlayService.EXTRA_SELECTED_INDEX, arg2);
			bundle.putString(PlayService.EXTRA_PLAYLIST_ID,
					arg0.getTag(R.string.play_list_id) + "");
			bundle.putBoolean(PlayService.EXTRA_IF_NEED_NETWORK, true);
			playIntent.putExtras(bundle);

			startActivity(playIntent);

		}
	};
	protected OnClickListener onLoadMoreBtnClick = new OnClickListener() {

		public void onClick(View v) {
			asyncTask = new LoadMoreContent();
			asyncTask.execute(v);
			loadMoreBtn.findViewById(R.id.load_more_progress).setVisibility(
					View.VISIBLE);
		}
	};

	protected void sendErrToast(String text) {
		mHandler.post(new ErrToastRunnable(text));
	}

	protected void pushToStack() {
		ListViewDataset dSet = new ListViewDataset();
		// dSet.task = remoteTask;
		dSet.listener = listView.getOnItemClickListener();
		listView.setOnItemClickListener(null);
		String adapterClassName = listView.getAdapter().getClass().getName();
		if (adapterClassName.indexOf("WrapperListAdapter") >= 0) {
			dSet.adapter = ((WrapperListAdapter) listView.getAdapter())
					.getWrappedAdapter();
		} else {
			dSet.adapter = listView.getAdapter();
		}

		dSet.adapter = listView.getAdapter();
		backStack.push(dSet);
	}

	protected boolean backView() {
		if (backStack.isEmpty()) {

			return false;
		} else {
			ListViewDataset set = backStack.pop();
			listView.setAdapter(set.adapter);
			listView.setOnItemClickListener(set.listener);
			asyncTask.cancel(true);
			listView.removeFooterView(loadingProgress);
			listView.removeFooterView(loadMoreBtn);
			return true;
		}

	}

	class ErrToastRunnable implements Runnable {
		private String info;

		public ErrToastRunnable(String text) {
			info = text;
		}

		public void run() {
			Toast.makeText(getActivity(), info, Toast.LENGTH_SHORT).show();
		}
	}

	class ListViewDataset {
		public OnItemClickListener listener;
		public ListAdapter adapter;
	}

	class LoadWikiContent extends AsyncTask<Object, Object, List<SimpleData>> {
		private JSONObject information;

		@Override
		protected List<SimpleData> doInBackground(Object... params) {
			View v = (View) params[0];
			String url = PLAY_LIST_DATA_URL + v.getTag(R.string.item_type)
					+ "=" + v.getTag(R.string.item_id) + "&perpage=20";
			List<SimpleData> playList = null;
			try {
				String json = http.oauthRequest(url);
				information = JSON.parseObject(json).getJSONObject("response")
						.getJSONObject("information");
				playList = JSONUtils.getSimpleDataFromPlayList(json);
			} catch (SocketTimeoutException e) {
				e.printStackTrace();
				sendErrToast("网络连接超时");
			} catch (Exception e) {
				sendErrToast("未知错误");
				Log.e("", "", e);
			}

			return playList;
		}

		@Override
		protected void onPostExecute(List<SimpleData> result) {
			super.onPostExecute(result);
			if (result != null) {
				listView.removeFooterView(loadingProgress);
				boolean hasNext = information.getBooleanValue("may_have_next");
				if (hasNext) {
					String nextUrl = information.getString("next_url");
					loadMoreBtn.setTag(R.string.more_btn_url, nextUrl);
					loadMoreBtn.setVisibility(View.VISIBLE);
					listView.addFooterView(loadMoreBtn);
				}
				SimpleDataAdapter adapter = new SimpleDataAdapter(
						getActivity(), result);
				listView.setAdapter(adapter);
				listView.setOnItemClickListener(onPlaylistItemClick);
			} else {
				backView();
			}
		}

	}

	class LoadMoreContent extends AsyncTask<Object, Object, List<SimpleData>> {
		JSONObject information;

		@Override
		protected List<SimpleData> doInBackground(Object... params) {
			String url = (String) ((View) params[0])
					.getTag(R.string.more_btn_url);
			List<SimpleData> attachedList = null;
			try {
				String json = http.oauthRequest(url);
				information = JSON.parseObject(json).getJSONObject("response")
						.getJSONObject("information");
				attachedList = JSONUtils.getSimpleDataFromPlayList(json);
			} catch (SocketTimeoutException e) {
				sendErrToast("网络连接超时");
				e.printStackTrace();
			} catch (Exception e) {
				Log.e("", "", e);
			}

			return attachedList;
		}

		@Override
		protected void onPostExecute(List<SimpleData> result) {
			super.onPostExecute(result);
			loadMoreBtn.findViewById(R.id.load_more_progress).setVisibility(
					View.GONE);
			if (result != null) {
				((SimpleDataAdapter) ((WrapperListAdapter) listView
						.getAdapter()).getWrappedAdapter()).getData().addAll(
						result);
				listView.invalidate();
				boolean hasNext = information.getBooleanValue("may_have_next");
				if (hasNext) {
					String nextUrl = information.getString("next_url");
					loadMoreBtn.setTag(R.string.more_btn_url, nextUrl);

				} else {
					listView.removeFooterView(loadMoreBtn);
				}

			}
		}

	}
}
