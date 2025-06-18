//package db_proj;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AnswerSQL {

    private Connection conn;

    public AnswerSQL(Connection connection) {
        this.conn = connection;
    }

    // Method to save an Answer to the database
    public void save(Answer answer,String subject) throws SQLException {
        String sql = "INSERT INTO Answers (answer_description,subject_name) VALUES (?,?) RETURNING answer_id";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, answer.getAnswerDescription());
        pstmt.setString(2,subject);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            int generatedId = rs.getInt(1);
            answer.setId(generatedId);

        }

    }

    // Method to fetch an Answer by ID
    public Answer findById(int id) throws SQLException {
        String sql = "SELECT answer_id, answer_description FROM answers WHERE answer_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String description = rs.getString("answer_description");
                Answer answer = new Answer(description);
                answer.setId(rs.getInt("answer_id"));
                return answer;
            }
        }
        return null;
    }
    
    // Other methods for update, delete, etc. can go here
}
