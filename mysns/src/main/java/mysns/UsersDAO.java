package mysns;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UsersDAO {
	Connection conn = null;
	PreparedStatement pstmt;

	// 데이터베이스 연결을 위한 open 메서드
	public void open() throws SQLException {
		// H2 Database 연결 정보
		String url = "jdbc:h2:tcp://localhost/~/test";
		String user = "tset";
		String password = "123";

		try {
			Class.forName("org.h2.Driver");
			conn = DriverManager.getConnection(url, user, password);
		} catch (ClassNotFoundException e) {
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

    // Sign up (create new user)
    public void signup(Users user) throws SQLException {
        String sql = "INSERT INTO Users (id, password, name, created_at) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getId());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getName());
            pstmt.executeUpdate();
        }
    }

    // Login (authenticate user)
    public Users login(String id, String password) throws SQLException {
        String sql = "SELECT * FROM Users WHERE id = ? AND password = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Users user = new Users();
                user.setAid(rs.getInt("aid"));
                user.setId(rs.getString("id"));
                user.setPassword(rs.getString("password"));
                user.setName(rs.getString("name"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
                return user;
            } else {
                return null;
            }
        }
    }

    // Additional methods (e.g., find user by ID, delete user, etc.)

    // Find user by ID
    public Users findUserById(int aid) throws SQLException {
        String sql = "SELECT * FROM Users WHERE aid = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, aid);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Users user = new Users();
                user.setAid(rs.getInt("aid"));
                user.setId(rs.getString("id"));
                user.setPassword(rs.getString("password"));
                user.setName(rs.getString("name"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
                return user;
            } else {
                return null;
            }
        }
    }

    // Delete user by ID
    public void deleteUser(int aid) throws SQLException {
        String sql = "DELETE FROM Users WHERE aid = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, aid);
            pstmt.executeUpdate();
        }
    }
    
    // 모든 사용자를 읽어오는 메서드
    public List<Users> getAll() throws SQLException {
        open();
        List<Users> userList = new ArrayList<>();
        String sql = "SELECT * FROM Users";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String id = rs.getString("id");
                String password = rs.getString("password");
                String name = rs.getString("name");
                Users user = new Users(id, password, name);
                userList.add(user);
            }
        } finally {
            close();
        }
        return userList;
    }
    
    // 사용자 ID로 사용자 찾기 메서드
    public Users findUserById(String id) throws SQLException {
        open();
        String sql = "SELECT * FROM Users WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Users user = new Users();
                    user.setId(rs.getString("id"));
                    user.setPassword(rs.getString("password"));
                    user.setName(rs.getString("name"));
                    return user;
                }
            }
        } finally {
            close();
        }
        return null;
    }
}

