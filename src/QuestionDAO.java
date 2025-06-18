//package db_proj;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

//import static db_proj.MultipleChoiceQuestion.MAX_NUM_OF_ANSWERS;

public class QuestionDAO {
    private Connection connection;

    public QuestionDAO(Connection connection) {
        this.connection = connection;
    }

    // Method to save an Answer to the database
    public void save(Question question, String subject) throws SQLException {
        String sql;

        if (question instanceof OpenQuestion) {
            // Insert into OpenQuestions
            sql = "INSERT INTO open_questions (question_description, difficulty_level, subject, right_answer) VALUES (?, ?, ?, ?) RETURNING question_id";
        } else if (question instanceof MultipleChoiceQuestion) {
            // Insert into MultipleChoice
            sql = "INSERT INTO multiple_choice_questions (question_description, difficulty_level, subject, number_of_answers) VALUES (?, ?, ?, ?) RETURNING question_id";
        } else {
            throw new IllegalArgumentException("Invalid question type: " + question.getClass().getName());
        }

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, question.getQuestionDescription());
            pstmt.setString(2, question.getDificultyLevel().name());
            pstmt.setString(3, subject);

            // Set the specific attribute based on question type
            if (question instanceof OpenQuestion) {
                OpenQuestion openQuestion = (OpenQuestion) question;
                int rightAnswerId = openQuestion.getSchoolAnswerID(); // Assuming getRightAnswer() returns an Answer object THIS IS NOT GOOD ID!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                pstmt.setInt(4, rightAnswerId);
            } else if (question instanceof MultipleChoiceQuestion) {
                MultipleChoiceQuestion multipleChoice = (MultipleChoiceQuestion) question;
                pstmt.setInt(4, multipleChoice.getNumOfAnswers());
            }

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int generatedId = rs.getInt(1);
                question.setId(generatedId);
            }
        }
                if(question instanceof MultipleChoiceQuestion){
                    sql = "INSERT INTO Multiple_Choice_Answers (question_id, answer_id, is_correct) VALUES (?, ?, ?)";

                    try (PreparedStatement pstmt2 = connection.prepareStatement(sql)) {
                        MultipleChoiceQuestion multipleChoice = (MultipleChoiceQuestion) question;
                        Answer[] answers = multipleChoice.getAnswers();
                        for (int i = 0;i<multipleChoice.getNumOfAnswers();i++) {
                            pstmt2.setInt(1, multipleChoice.getId());  // The generated question_id from the multiple_choice_questions table
                            pstmt2.setInt(2, answers[i].getId());          // The answer_id from the Answer object
                            pstmt2.setBoolean(3, multipleChoice.answersCorrection[i]);  // Assuming isCorrect() returns a boolean indicating if it's the correct answer
                            pstmt2.executeUpdate();
                        }
                    }
                }

        }


    // Method to fetch an Answer by ID
    public Question findById(int id) throws SQLException {
        Question question = null;

        // Try to find the question in open_questions
        question = findOpenQuestionById(id);
        if (question != null) {
            return question;
        }
       else {
            question = findMultipleChoiceQuestionById(id);
            return question;
        }
    }


    private OpenQuestion findOpenQuestionById(int id) throws SQLException {
        String sql = "SELECT * FROM open_questions WHERE question_id = ?";
        OpenQuestion openQuestion = null;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String Desc = rs.getString("question_description");
                Question.eDifficultyLevel Diff = Question.eDifficultyLevel.valueOf(rs.getString("difficulty_level"));
                int rightAnswerId = rs.getInt("right_answer");
                AnswerSQL ans_db2 = new AnswerSQL(connection);
                Answer rightAnswer = ans_db2.findById(rightAnswerId);
                openQuestion = new OpenQuestion(Desc,Diff,rightAnswer);
                openQuestion.setId(rs.getInt("question_id"));
            }
        }

        return openQuestion;
    }

    private MultipleChoiceQuestion findMultipleChoiceQuestionById(int id) throws SQLException {
        String sql = "SELECT * FROM multiple_choice_questions WHERE question_id = ?";
        MultipleChoiceQuestion multipleChoice = null;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String desc = rs.getString("question_description");
                Question.eDifficultyLevel diff = Question.eDifficultyLevel.valueOf(rs.getString("difficulty_level"));

                multipleChoice = new MultipleChoiceQuestion(desc, diff);
                multipleChoice.setNumOfAnswers(rs.getInt("number_of_answers"));
                multipleChoice.setId(rs.getInt("question_id"));

                // Retrieve associated answers
                String answersSql = "SELECT * FROM Multiple_Choice_Answers WHERE question_id = ?";
                try (PreparedStatement answersPstmt = connection.prepareStatement(answersSql)) {
                    answersPstmt.setInt(1, id);
                    ResultSet answersRs = answersPstmt.executeQuery();

                    Answer[] answers = new Answer[MultipleChoiceQuestion.MAX_NUM_OF_ANSWERS];
                    boolean[] isCorrect = new boolean[MultipleChoiceQuestion.MAX_NUM_OF_ANSWERS];
                    int index = 0;

                    AnswerSQL ans_db3 = new AnswerSQL(connection);

                    while (answersRs.next()) {
                        int answerId = answersRs.getInt("answer_id");
                        Answer answer = ans_db3.findById(answerId);
                        answers[index] = answer;
                        isCorrect[index++] = answersRs.getBoolean("is_correct");
                    }

                    multipleChoice.setAnswers(answers);
                    multipleChoice.setAnswersCorrection(isCorrect);
                }
            }
        }

        return multipleChoice;
    }




}
