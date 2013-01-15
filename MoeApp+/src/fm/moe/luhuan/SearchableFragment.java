package fm.moe.luhuan;

import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import fm.moe.luhuan.adapter.SimpleDataAdapter;
import fm.moe.luhuan.beans.data.SimpleData;
import fm.moe.luhuan.utils.JSONUtils;

import android.os.Bundle;
import android.view.View;


public class SearchableFragment extends RemoteContentFragment {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String json = getArguments().getString(SearchableActivity.EXTRA_SEARCH_RESULT_JSON);
		String url = getArguments().getString(SearchableActivity.EXTRA_SEARCH_URL);
		List<SimpleData> list = JSONUtils.getWikiList(json);
		JSONObject  information = JSON.parseObject(json).getJSONObject("response").getJSONObject("information");
		int totalCount = information.getIntValue("count");
		int countPerPage = information.getIntValue("perpage");
		int pageIndex = information.getIntValue("page");
		if(totalCount>countPerPage*pageIndex){
			String nextUrl = url+"&page="+(pageIndex+1);
			loadMoreBtn.setTag(R.string.more_btn_url,nextUrl);
			loadMoreBtn.setVisibility(View.VISIBLE);
			listView.addFooterView(loadMoreBtn);
		}
		SimpleDataAdapter adapter = new SimpleDataAdapter(getActivity(), list);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(onWikiClick);
	}

}
