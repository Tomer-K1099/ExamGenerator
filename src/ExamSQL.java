import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ExamSQL {
    private Connection conn;

    public ExamSQL(Connection conn) {
        this.conn = conn;
    }

    public void fetchAndDisplayExam(int test_id) throws SQLException {
        int testId = 1; // Example test ID to fetch

        String sql = "SELECT t.test_id, t.user_id, q.question_id, q.question_description, q.difficulty_level, q.subject "+
                      "FROM tests t "+
                      "JOIN test_questions tq ON t.test_id = tq.test_id "+
                      "JOIN questions q ON tq.question_id = q.question_id "+
                      "WHERE t.test_id = ? "+
                      "ORDER BY q.question_id";


        try(PreparedStatement pstmt = this.conn.prepareStatement(sql)) {
            pstmt.setInt(1, test_id);
            ResultSet rs = pstmt.executeQuery();
            System.out.println("=== Test ID: " + test_id + " ===");
            boolean hasRows = false;
            while (rs.next()) {
                if (!hasRows) {
                    System.out.println("User ID: " + rs.getString("user_id"));
                    System.out.println("Questions:");
                    hasRows = true;
                }
                int qId = rs.getInt("question_id");
                String desc = rs.getString("question_description");
                String diff = rs.getString("difficulty_level");
                String subj = rs.getString("subject");

                System.out.printf("  [%d] (%s, %s) %s%n", qId, subj, diff, desc);
            }
            if (!hasRows) {
                System.out.println("No questions found for this test.");
            }

        }

    }

    public ArrayList<Integer> getExamsIDs() throws SQLException{
        String sql = "SELECT test_id FROM tests ORDER BY test_id ASC ";
        ArrayList<Integer> ids = new ArrayList<Integer>();
        try(PreparedStatement pstmt = this.conn.prepareStatement(sql)){
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                ids.add(rs.getInt("test_id"));
            }

        }
        return ids;
    }

}