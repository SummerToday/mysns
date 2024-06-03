package mysns;

import java.sql.Blob;
import java.sql.Timestamp;

public class Feeds {
	private int aid;
	private String id;
	private Blob image;
	private String content;
	private Timestamp created_at;
	
	public int getAid() {
		return aid;
	}

	public void setAid(int aid) {
		this.aid = aid;
	}
	
	public Feeds() {
        // 기본 생성자
    }

	public Feeds(String id, Blob image, String content, Timestamp created_at) {
        this.id = id;
        this.image = image;
        this.content = content;
        this.created_at = created_at;
    }
	
	public Feeds(int aid, String id, Blob image, String content, Timestamp created_at) {
        this.aid = aid;
		this.id = id;
        this.image = image;
        this.content = content;
        this.created_at = created_at;
    }
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Blob getImage() {
		return image;
	}
	public void setImage(Blob img) {
		this.image = img;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public Timestamp getCreated_at() {
		return created_at;
	}
	public void setCreated_at(Timestamp created_at) {
		this.created_at = created_at;
	}

}
