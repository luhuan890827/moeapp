package fm.moe.luhuan.utils;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import fm.moe.luhuan.beans.data.SimpleData;

public class JSONUtils {
	
	private  static SimpleData getWikiBean(JSONObject obj) {
		SimpleData data = new SimpleData();
		data.setTitle(obj.getString("wiki_title"));
		data.setId(obj.getIntValue("wiki_id"));
		JSONArray arr = obj.getJSONArray("wiki_meta");
		String artist = null;
		String description = null;
		
		if(arr!=null){
			for (int i = 0; i < arr.size(); i++) {
				JSONObject t = arr.getJSONObject(i);
				String key = t.getString("meta_key");
				String val = t.getString("meta_value");
				int type = t.getIntValue("meta_type");
				if (key.contains("¼ò½é")) {
					description = val;
				}
				if (key.contains("ÒÕÊõ")||key.contains("ÑÝ³ª")) {
					artist = val;
				}

			}
		}
		
		data.setAlbumnCoverUrl(obj.getJSONObject("wiki_cover").getString("large"));
		data.setArtist(artist);
		data.setDescription(description);
		data.setType(obj.getString("wiki_type"));
		
		return data;
	}

	private static SimpleData getSub(JSONObject obj) {
		//Log.e("fav_sub_raw", obj.toJSONString());
		SimpleData data = new SimpleData();
		data.setId(obj.getIntValue("sub_id"));
		data.setTitle(obj.getString("sub_title"));
		SimpleData wikiData = getWikiBean(obj.getJSONObject("wiki"));
		data.setArtist(wikiData.getArtist());
		data.setDescription(wikiData.getDescription());
		data.setParentId(wikiData.getId());
		data.setAlbumnCoverUrl(wikiData.getAlbumnCoverUrl());
		data.setParentTitle(wikiData.getTitle());
		JSONObject subUpload=null;
		try{
			subUpload = obj.getJSONArray("sub_upload").getJSONObject(0);
		}catch(Exception e){
			//Log.e("", "",e);
		}
		
		if(subUpload!=null){
			data.setMp3Url(subUpload.getString("up_url"));
		}else{
			return null;
		}
		
		return data;

	}
	
	public static List<SimpleData> getExpWikiList(String json, String key) {
		List<SimpleData> l = new ArrayList<SimpleData>();
		JSONArray arr = JSON.parseObject(json).getJSONObject("response")
				.getJSONArray(key);
		for (int i = 0; i < arr.size(); i++) {
			SimpleData data = getWikiBean(arr.getJSONObject(i));
			l.add(data);
		}
		return l;
	}


	public static List<SimpleData> getFavs(String json, String type) {
		List<SimpleData> l = new ArrayList<SimpleData>();
		JSONArray arr = JSON.parseObject(json).getJSONObject("response")
				.getJSONArray("favs");
		//Log.e("fav raw", json);
		if(type.equals("wiki")){
			for(int i = 0;i<arr.size();i++){
				JSONObject obj = arr.getJSONObject(i).getJSONObject("obj");
				//Log.e("jo", obj.toJSONString());
				
				SimpleData data = getWikiBean(obj);
				data.setFav(true);
				//Log.e("id", data.getId()+"");
				l.add(data);
			}
		}else{
			for(int i = 0;i<arr.size();i++){
				JSONObject obj = arr.getJSONObject(i).getJSONObject("obj");
				//Log.e("", obj.toJSONString());
				SimpleData data = getSub(obj);
				data.setFav(true);
				l.add(data);
			}
		}
		return l;
	}
	public static List<SimpleData> getSimpleDataFromPlayList(String json){
		List<SimpleData> l = new ArrayList<SimpleData>();
		JSONArray arr= JSON.parseObject(json).getJSONObject("response").getJSONArray("playlist");
		for(int i = 0 ;i<arr.size();i++){
			JSONObject obj = arr.getJSONObject(i);
			SimpleData data = getSimpleDataFromPlayListItem(obj);
			l.add(data);
		}
		return l;
	}

	private static SimpleData getSimpleDataFromPlayListItem(JSONObject obj) {
		SimpleData data = new SimpleData();
		//Log.e("raw", obj.getString("url"));
		data.setArtist(obj.getString("artist"));
		data.setId(obj.getIntValue("sub_id"));
		data.setTitle(obj.getString("sub_title"));
		data.setMp3Url(obj.getString("url"));
		data.setAlbumnCoverUrl(obj.getJSONObject("cover").getString("large"));
		if(obj.getJSONObject("fav_sub")!=null){
			data.setFav(true);
		}
		data.setParentTitle(obj.getString("wiki_title"));
		data.setParentId(obj.getIntValue("wiki_id"));
		return data;
	}

	public static List<SimpleData> getWikiList(String json) {
		return getExpWikiList(json, "wikis");
	}

	public static List<SimpleData> getSublist(String json) {
		List<SimpleData> l = new ArrayList<SimpleData>();
		
		JSONArray arr = JSON.parseObject(json).getJSONObject("response").getJSONArray("subs");
		if(arr!=null){
			for(int i = 0;i<arr.size();i++){
				SimpleData data = getSub(arr.getJSONObject(i));
				if(data!=null){
					l.add(data);
				}
				
			}
		}
		
		return l;
	}
	
	
}
