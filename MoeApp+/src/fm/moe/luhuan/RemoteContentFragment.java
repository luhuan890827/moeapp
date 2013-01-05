package fm.moe.luhuan;

import java.net.SocketTimeoutException;
import java.util.Stack;
import java.util.concurrent.Executor;

import fm.moe.luhuan.http.MoeHttp;
import android.content.Context;
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
	protected Executor executor;
	protected AsyncTask<Object, Object, Object> asyncTask;
	private final String PLAY_LIST_DATA_URL = "http://moe.fm/listen/playlist?api=json&";
	public static final String EXTRA_GROUP_TAGS = "group tags";

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		netInfo = ((ConnectivityManager) getActivity().getSystemService(
				Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
		inflater = getActivity().getLayoutInflater();
		loadingProgress = (LinearLayout) inflater.inflate(
				R.layout.progress_view, null, false);
		loadMoreBtn = (LinearLayout) inflater.inflate(R.layout.load_more_view,
				null, false);
		listView = new ListView(getActivity());
		Bundle args = getArguments();
		String[] tags = args.getStringArray(EXTRA_GROUP_TAGS);
		ListAdapter adapter = new ArrayAdapter<String>(getActivity(),
				R.layout.big_text_item, tags);
		listView.addFooterView(new LinearLayout(getActivity()));
		listView.setAdapter(adapter);
		http = new MoeHttp(getActivity());

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

		}
	};
	protected OnClickListener onLoadMoreBtnClick = new OnClickListener() {

		public void onClick(View v) {

		}
	};

	protected void sendErrToast(String text) {
		mHandler.post(new ErrToastRunnable(text));
	}

	protected void pushToStack() {
		ListViewDataset dSet = new ListViewDataset();
		// dSet.task = remoteTask;
		dSet.listener = listView.getOnItemClickListener();
		dSet.adapter = ((WrapperListAdapter) listView.getAdapter())
				.getWrappedAdapter();
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
			mHandler.removeCallbacksAndMessages(null);
		}
	}

	class ListViewDataset {
		public OnItemClickListener listener;
		public ListAdapter adapter;
	}

	class LoadWikiContent extends AsyncTask<Object, Object, Object> {

		@Override
		protected Object doInBackground(Object... params) {
			View v = (View) params[0];
			String url = PLAY_LIST_DATA_URL + v.getTag(R.string.item_type)
					+ "=" + v.getTag(R.string.item_id) + "&perpage=20";
			try {
				String json = http.oauthRequest(url);
			} catch (SocketTimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}catch (Exception e) {
				Log.e("", "",e);
				// TODO: handle exception
			}
			
			return null;
		}

	}
}
