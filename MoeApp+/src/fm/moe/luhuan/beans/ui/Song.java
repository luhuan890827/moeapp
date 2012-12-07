package fm.moe.luhuan.beans.ui;

import java.io.Serializable;

import fm.moe.luhuan.beans.data.MoeItemData;

public class Song extends MoeItemData implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String artist;
	private String albumTitle;
	private int albumId=0;
	private boolean isFav;
	private boolean isSave;
	private String totalLength;
	private int order;
	private String mp3Url;
	
	
	public String getArtist() {
		return artist;
	}
	public void setArtist(String artist) {
		this.artist = artist;
	}
	public String getAlbumTitle() {
		return albumTitle;
	}
	public void setAlbumTitle(String albumTitle) {
		this.albumTitle = albumTitle;
	}
	public int getAlbumId() {
		return albumId;
	}
	public void setAlbumId(int albumId) {
		this.albumId = albumId;
	}
	public boolean isFav() {
		return isFav;
	}
	public void setFav(boolean isFav) {
		this.isFav = isFav;
	}
	public boolean isSave() {
		return isSave;
	}
	public void setSave(boolean isSave) {
		this.isSave = isSave;
	}
	public String getTotalLength() {
		return totalLength;
	}
	public void setTotalLength(String totalLength) {
		this.totalLength = totalLength;
	}
	public int getOrder() {
		return order;
	}
	public void setOrder(int order) {
		this.order = order;
	}
	public String getMp3Url() {
		return mp3Url;
	}
	public void setMp3Url(String mp3Url) {
		this.mp3Url = mp3Url;
	}
	
	
}
