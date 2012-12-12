package fm.moe.luhuan.beans.data;

import java.io.Serializable;

public class SimpleData implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int id;
	private String artist;
	private String title;
	private String description;
	private String mp3Url;
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
	
}
