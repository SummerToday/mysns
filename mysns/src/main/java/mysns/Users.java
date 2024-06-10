package mysns;

import java.sql.Timestamp;

public class Users {
    private int aid;
    private String id;
    private String password;
    private String name;
    private Timestamp created_At;
    
    public Users() {}
    
    public Users(String id, String password, String name) {
        this.id = id;
        this.password = password;
        this.name = name;
    }
    // Getters and Setters
    public int getAid() {
        return aid;
    }

    public void setAid(int aid) {
        this.aid = aid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Timestamp getCreatedAt() {
        return created_At;
    }

    public void setCreatedAt(Timestamp created_At) {
        this.created_At = created_At;
    }
}

