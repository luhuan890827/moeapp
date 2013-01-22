package fm.moe.luhuan.beans.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SerializableSimpleData implements Serializable{
	private int parentId = -1;
	private String type;
	private int id;
	private String artist;
	private String title;
	private String description;
	private String mp3Url;
	private boolean isFav;
	private String albumnCoverUrl;
	private String parentTitle;
	private String thumbUrl;
	public int getParentId() {
		return parentId;
	}
	public void setParentId(int parentId) {
		this.parentId = parentId;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getArtist() {
		return artist;
	}
	public void setArtist(String artist) {
		this.artist = artist;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getMp3Url() {
		return mp3Url;
	}
	public void setMp3Url(String mp3Url) {
		this.mp3Url = mp3Url;
	}
	public boolean isFav() {
		return isFav;
	}
	public void setFav(boolean isFav) {
		this.isFav = isFav;
	}
	public String getAlbumnCoverUrl() {
		return albumnCoverUrl;
	}
	public void setAlbumnCoverUrl(String albumnCoverUrl) {
		this.albumnCoverUrl = albumnCoverUrl;
	}
	public String getParentTitle() {
		return parentTitle;
	}
	public void setParentTitle(String parentTitle) {
		this.parentTitle = parentTitle;
	}
	public String getThumbUrl() {
		return thumbUrl;
	}
	public void setThumbUrl(String thumbUrl) {
		this.thumbUrl = thumbUrl;
	}
	public static List<SerializableSimpleData>  fromList(List<SimpleData> list){
		List<SerializableSimpleData> l = new ArrayList<SerializableSimpleData>();
		for(int i =0;i<list.size();i++){
			SimpleData sd = list.get(i);
			SerializableSimpleData ssd = new SerializableSimpleData();
			ssd.setAlbumnCoverUrl(sd.getAlbumnCoverUrl());
			ssd.setArtist(sd.getArtist());
			ssd.setDescription(sd.getDescription());
			ssd.setFav(sd.isFav());
			ssd.setId(sd.getId());
			ssd.setMp3Url(sd.getMp3Url());
			ssd.setParentId(sd.getParentId());
			ssd.setParentTitle(sd.getParentTitle());
			ssd.setThumbUrl(sd.getThumbUrl());
			ssd.setTitle(sd.getTitle());
			ssd.setType(sd.getType());
			l.add(ssd);
		}
		return l;
	}
	public static LinkedList<SimpleData>  toList(List<SerializableSimpleData> list){
		LinkedList<SimpleData> l = new LinkedList<SimpleData>();
		for(int i =0;i<list.size();i++){
			SerializableSimpleData sd = list.get(i);
			SimpleData ssd = new SimpleData();
			ssd.setAlbumnCoverUrl(sd.getAlbumnCoverUrl());
			ssd.setArtist(sd.getArtist());
			ssd.setDescription(sd.getDescription());
			ssd.setFav(sd.isFav());
			ssd.setId(sd.getId());
			ssd.setMp3Url(sd.getMp3Url());
			ssd.setParentId(sd.getParentId());
			ssd.setParentTitle(sd.getParentTitle());
			ssd.setThumbUrl(sd.getThumbUrl());
			ssd.setTitle(sd.getTitle());
			ssd.setType(sd.getType());
			l.add(ssd);
		}
		return l;
	}
}
