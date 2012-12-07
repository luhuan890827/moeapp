package fm.moe.luhuan.beans.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;





public class PlayListItem extends MoeItemData {

	private String artist;
	private HashMap<String, String> covers;
	private String sub;
	private String fullTime;
	private String mp3Url;
	private int albumId;
	private String albumTitle;
	
	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public HashMap<String, String> getCovers() {
		return covers;
	}

	public void setCovers(HashMap<String, String> covers) {
		this.covers = covers;
	}

	public String getSub() {
		return sub;
	}

	public void setSub(String sub) {
		this.sub = sub;
	}

	public String getFullTime() {
		return fullTime;
	}

	public void setFullTime(String fullTime) {
		this.fullTime = fullTime;
	}

	public String getMp3Url() {
		return mp3Url;
	}

	public void setMp3Url(String mp3Url) {
		this.mp3Url = mp3Url;
	}

	public int getAlbumId() {
		return albumId;
	}

	public void setAlbumId(int albumId) {
		this.albumId = albumId;
	}

	public String getAlbumTitle() {
		return albumTitle;
	}

	public void setAlbumTitle(String albumTitle) {
		this.albumTitle = albumTitle;
	}
	public static List<PlayListItem> getList(String s){
		JSONObject resp = JSON.parseObject(s).getJSONObject("response");

		ArrayList<PlayListItem> list = new ArrayList<PlayListItem>();
		// Log.e("s2", resp.toJSONString()+"");
		JSONArray ja_playList = resp.getJSONArray("playlist");

		for (int i = 0; i < ja_playList.size(); i++) {
			JSONObject jo_listItem = ja_playList.getJSONObject(i);
			PlayListItem item = getObject(jo_listItem);
			list.add(item);
		}

		return list;
	}

	private static PlayListItem getObject(JSONObject jo) {
		
		JSONObject json_covers = jo.getJSONObject("cover");
		HashMap<String, String> covers = new HashMap<String, String>();
		Set<java.util.Map.Entry<String, Object>> set = json_covers.entrySet();
		for (java.util.Map.Entry<String, Object> entry : set) {
			covers.put(entry.getKey(), (String) entry.getValue());
		}

		PlayListItem item = new PlayListItem();
		
		item.setCovers(covers);
		item.setAlbumId(jo.getIntValue("wiki_id"));
		item.setAlbumTitle(jo.getString("wiki_title"));
		item.setArtist(jo.getString("artist"));
		item.setFullTime(jo.getString("Stream_time"));
		item.setId(jo.getIntValue("sub_id"));
		item.setMp3Url(jo.getString("url"));
		item.setTitle(jo.getString("sub_title"));
		item.setType(jo.getString("sub_type"));
		item.setWebUrl(jo.getString("sub_url"));
		
		return item;
	}
}
