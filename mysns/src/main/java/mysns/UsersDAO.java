package mysns;

import java.sql.*;

public class UsersDAO {
	Connection conn = null;
	PreparedStatement pstmt;

	// 데이터베이스 연결을 위한 open 메서드
	public void open() throws SQLException {
		// H2 Database 연결 정보
		String url = "jdbc:h2:tcp://localhost/~/practice1";
		String user = "practice1";
		String password = "1234";

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
}

