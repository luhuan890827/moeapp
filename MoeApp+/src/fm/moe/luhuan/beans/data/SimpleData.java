package fm.moe.luhuan.beans.data;

import java.io.Serializable;

public class SimpleData implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int parentId=-1;
	private int id;
	private String artist;
	private String title;
	private String description;
	private String mp3Url;
	private boolean isFav;
	private String albumnCoverUrl;
	private String parentTitle;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getArtist() {
		return artist;
	}
	public void setArtist(String artist) {
		this.artist = artist;
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
	public int getParentId() {
		return parentId;
	}
	public void setParentId(int parentId) {
		this.parentId = parentId;
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
	
}
