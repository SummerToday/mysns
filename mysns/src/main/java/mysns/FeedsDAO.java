package mysns;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


public class FeedsDAO {
	Connection conn = null;
	PreparedStatement pstmt;

	// 데이터베이스 연결을 위한 open 메서드
	public void open() {
		// H2 Database 연결 정보
		String url = "jdbc:h2:tcp://localhost/~/practice1";
		String user = "practice1";
		String password = "1234";

		try {
			Class.forName("org.h2.Driver");
			conn = DriverManager.getConnection(url, user, password);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 데이터베이스 연결을 해제하기 위한 close 메서드
	public void close() throws SQLException {
		try {
			if (pstmt != null && !pstmt.isClosed()) {
				pstmt.close();
			}
			if (conn != null && !conn.isClosed()) {
				conn.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// 게시글을 추가하는 메소드
	public void write(Feeds feed) {
	    String sql = "INSERT INTO Feeds (id, image, content, created_at) VALUES (?, ?, ?, ?)";
	    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        pstmt.setString(1, feed.getId());
	        pstmt.setBlob(2, feed.getImage());
	        pstmt.setString(3, feed.getContent());
	        pstmt.setTimestamp(4, feed.getCreated_at());
	        pstmt.executeUpdate();
	        System.out.println("Feed added successfully.");
	    } catch (SQLException e) {
	        e.printStackTrace();
	        throw new RuntimeException("Failed to add feed.");
	    }
	}


	// 모든 게시물을 가져오는 getAll 메서드
	public List<Feeds> getAll() {
	    open(); // 데이터베이스 연결 열기
	    List<Feeds> feedsList = new ArrayList<>();
	    String sql = "SELECT * FROM Feeds";
	    try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
	        while (rs.next()) {
	        	Feeds feeds = new Feeds();
	        	feeds.setAid(rs.getInt("aid"));
	        	feeds.setId(rs.getString("id"));
	        	feeds.setImage(rs.getBlob("image"));
	        	feeds.setCreated_at(rs.getTimestamp("created_at"));
	        	feeds.setContent(rs.getString("content"));
	            feedsList.add(feeds);
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	        throw new RuntimeException("게시물을 가져오는 데 실패했습니다.");
	    } finally {
	        try {
	            close(); // 데이터베이스 연결 닫기
	        } catch (SQLException e) {
	            e.printStackTrace();
	            throw new RuntimeException("데이터베이스 연결을 닫는 데 실패했습니다.");
	        }
	    }
	    return feedsList;
	}



    
    // 게시글 삭제를 위한 delFeeds 메서드
 	public void delFeeds(int aid) throws SQLException {
 		String sql = "DELETE FROM Feeds WHERE aid = ?";
 		try (PreparedStatement statement = conn.prepareStatement(sql)) {
 			statement.setInt(1, aid);
 			int rowsAffected = statement.executeUpdate();
 			if (rowsAffected == 0) {
 				throw new SQLException("Feeds with aid " + aid + " not found");
 			}
 		}
 	}
 	
 	public Feeds getFeedById(int aid) throws SQLException {
 	    open(); // 데이터베이스 연결 설정
 	    String sql = "SELECT * FROM Feeds WHERE aid = ?";
 	    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
 	        pstmt.setInt(1, aid);
 	        try (ResultSet rs = pstmt.executeQuery()) {
 	            if (rs.next()) {
 	                String id = rs.getString("id");
 	                Blob image = rs.getBlob("image");
 	                String content = rs.getString("content");
 	                Timestamp created_at = rs.getTimestamp("created_at");
 	                return new Feeds(aid, id, image, content, created_at);
 	            } else {
 	                throw new SQLException("Feeds with aid " + aid + " not found");
 	            }
 	        }
 	    } finally {
 	        close(); // 데이터베이스 연결 닫기
 	    }
 	}
 	
 // 게시글을 수정하는 메서드
 	public void updateFeed(Feeds feed) throws SQLException {
 	    open(); // 데이터베이스 연결 열기
 	    String sql = "UPDATE Feeds SET content = ?, image = ? WHERE aid = ?";
 	    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
 	        pstmt.setString(1, feed.getContent());
 	        pstmt.setBlob(2, feed.getImage());
 	        pstmt.setInt(3, feed.getAid());
 	        int rowsAffected = pstmt.executeUpdate();
 	        if (rowsAffected == 0) {
 	            throw new SQLException("Failed to update feed");
 	        }
 	    } finally {
 	        close(); // 데이터베이스 연결 닫기
 	    }
 	}
 	
 	public byte[] getImageById(int id) throws SQLException {
 	    byte[] img = null;
 	    String sql = "SELECT image FROM Feeds WHERE aid = ?";
 	    ResultSet rs = null;
 	    try {
 	        open();
 	        pstmt = conn.prepareStatement(sql);
 	        pstmt.setInt(1, id);
 	        rs = pstmt.executeQuery();
 	        if (rs.next()) {
 	            Blob image = rs.getBlob("image");
 	            img = image.getBytes(1, (int) image.length());
 	            image.free();
 	        } else {
 	            throw new SQLException("No image found with aid " + id);
 	        }
 	    } catch (SQLException e) {
 	        e.printStackTrace();
 	    } finally {
 	        if (rs != null) {
 	            rs.close();
 	        }
 	        if (pstmt != null) {
 	            pstmt.close();
 	        }
 	        close();
 	    }
 	    return img;
 	}
 	
    // 사용자의 글만 가져오는 메서드 추가
 	public List<Feeds> getFeedsByUserId(String userId) throws SQLException {
 	    open();
 	    List<Feeds> feedsList = new ArrayList<>();
 	    String sql = "SELECT * FROM Feeds WHERE id = ?";
 	    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
 	        pstmt.setString(1, userId);
 	        try (ResultSet rs = pstmt.executeQuery()) {
 	            while (rs.next()) {
 	                int aid = rs.getInt("aid");
 	                String id = rs.getString("id");
 	                Blob image = rs.getBlob("image");
 	                String content = rs.getString("content");
 	                Timestamp created_At = rs.getTimestamp("created_at");
 	                Feeds feed = new Feeds(aid, id, image, content, created_At);
 	                feedsList.add(feed);
 	            }
 	        }
 	    } finally {
 	        close();
 	    }
 	    return feedsList;
 	}


}
