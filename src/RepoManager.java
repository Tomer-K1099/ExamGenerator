//package db_proj;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;


public class RepoManager {
	private QuestionesRepository[] repositories;
	private int numRepositories;
	private final int DEAFULT_SIZE=5;

	public RepoManager() {
		this.repositories = new QuestionesRepository[DEAFULT_SIZE];
	
	}
	
	private static void saveRepositories(QuestionesRepository[] repositories, String filename) throws IOException {
		ObjectOutputStream file = new ObjectOutputStream(new FileOutputStream(filename));
		file.writeObject(repositories);
		file.close();
	}
	
	
	private static QuestionesRepository[] loadRepositories(String filename) throws IOException, ClassNotFoundException {
		ObjectInputStream file = new ObjectInputStream(new FileInputStream(filename));
		QuestionesRepository[] repositories = (QuestionesRepository[])file.readObject();
		file.close();
		return repositories;
	}


	public void initRepositories() throws IOException, ClassNotFoundException {
		
		// HARD CODE BECAUSE IT IS NOT STABLE
		repositories[0] = new QuestionesRepository("Math");
		repositories[0].initHardCode();
		repositories[1] = new QuestionesRepository("Capitals");
		repositories[1].initHardCode();
		repositories[2] = new QuestionesRepository("create new repository");
		numRepositories = 3;
		saveRepositories(repositories, "repository.dat");

//		 read repositories from binary file
		repositories = loadRepositories("repository.dat");
		boolean found = false;
		for (int i = 0; i < DEAFULT_SIZE; i++) {
			if (repositories[i] == null) {
				numRepositories = i;
				found = true;
				break;
			}
		}
		if (!found) {
			numRepositories = DEAFULT_SIZE;
		}

   }

   public void initRepositoriesFromDB(Connection conn) throws SQLException
   {
	   this.numRepositories = countSubjects(conn);
	   List<String> subjectName = getAllSubjectNames(conn);
	   QuestionSQL q_DAO = new QuestionSQL(conn);
	   for(int i = 0;i<this.numRepositories;i++)
	   {
		this.repositories[i] = new QuestionesRepository(subjectName.get(i));
		this.repositories[i].numOfAllQustiones = countQuestions(conn,subjectName.get(i));
		int j = 0;
		{
			String sql = "SELECT question_id FROM Questions WHERE subject = ?";

			try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
				pstmt.setString(1, subjectName.get(i));
				ResultSet rs = pstmt.executeQuery();
				while (rs.next()) {
					this.repositories[i].allQuestions[j++] = q_DAO.findById(rs.getInt("question_id"));
				}
			}
		}
		this.repositories[i].numOfAllAnswers = countAnswers(conn,this.repositories[i].getSubject());

		   AnswerSQL ans_db = new AnswerSQL(conn);
		   j = 0;
		   {
			   String sql = "SELECT answer_id FROM Answers WHERE subject_name = ?";

			   try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
				   pstmt.setString(1, subjectName.get(i));
				   ResultSet rs = pstmt.executeQuery();
				   while (rs.next()) {
					   this.repositories[i].allAnswers[j++] = ans_db.findById(rs.getInt("answer_id"));
				   }
			   }
		   }

	   }
	   repositories[this.numRepositories++] = new QuestionesRepository("create new repository");




   }
	public int countQuestions(Connection conn, String subjectname) throws SQLException {
		String sql = "SELECT COUNT(q.question_id) " +
				"FROM public.subjects s " +
				"INNER JOIN questions q ON s.subject_name = q.subject " +
				"WHERE s.subject_name = ?"; // Use parameter placeholder
		int count = 0;

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, subjectname); // Set the subjectname parameter
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				count = rs.getInt(1);
			}
		}

		return count;
	}

	public int countAnswersForQuestion(Connection conn, Question question) throws SQLException {
		String sql = "SELECT COUNT(ans.answer_id) " +
				"FROM public.multiple_choice_answers ans "+
				"WHERE question_id = ?";
		int count = 0;
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, question.getId());
			ResultSet rs = pstmt.executeQuery();
			if(rs.next()){
				count = rs.getInt(1);
			}

		} catch(SQLException e) {
			System.out.println("Something went wrong while counting answers for question number " + question.getId() + ':' +  e.getMessage());
		}
		return count;
	}

	public ArrayList<String> getAnswersForQuestion(Connection conn, Question question) throws SQLException {
		String sql = "SELECT ans.answer_description " +
				"FROM public.answers ans "+
				"INNER JOIN public.multiple_choice_answers mult_ans ON ans.question_id = mult_ans.question_id "+
				"WHERE question_id = ?";
		ArrayList<String> answer_descriptions =  new ArrayList<String>();
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, question.getId());
			ResultSet rs = pstmt.executeQuery();
			while(rs.next()){
				String answer_desc = rs.getString(1);
				answer_descriptions.add(answer_desc);
			}

		} catch(SQLException e) {
			System.out.println("Something went wrong while fetching answers for question number " + question.getId() + ':' + e.getMessage());
		}
		return answer_descriptions;


}

	public int countAnswers(Connection conn, String subjectname) throws SQLException {
		String sql = "SELECT COUNT(an.answer_id) " +
				"FROM public.subjects s " +
				"INNER JOIN answers an  ON s.subject_name = an.subject_name " +
				"WHERE s.subject_name = ?"; // Use parameter placeholder
		int count = 0;

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, subjectname); // Set the subjectname parameter
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				count = rs.getInt(1);
			}
		}

		return count;
	}
	public int countSubjects(Connection conn) throws SQLException {
		String sql = "SELECT COUNT(*) FROM Subjects";
		int count = 0;

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				count = rs.getInt(1);
			}
		}

		return count;
	}
	public List<String> getAllSubjectNames(Connection conn) throws SQLException {
		String sql = "SELECT subject_name FROM Subjects";
		List<String> subjectNames = new ArrayList<>();

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				subjectNames.add(rs.getString("subject_name"));
			}
		}

		return subjectNames;
	}
	public QuestionesRepository getRepository(int index) {
		return repositories[index];
	}
	
	public int getNumRepositories() {
		return numRepositories;
	}
	
	
	
	
	public String toString() {
		String ans = "";
		for (int i = 0; i < numRepositories; i++) {
			ans +=  (i+1)+ ") "  + repositories[i].getSubject() + "\n";
		}
		return ans;
		
	}
      public boolean addRepository(Connection conn,String newRepoSubject) {
		for (int i = 0; i < numRepositories; i++) {
			if (repositories[i].getSubject().equals(newRepoSubject)) // this question alredy exist
				return false;
		}

		if (numRepositories == repositories.length)
			repositories = Arrays.copyOf(repositories, numRepositories * 2);


		try {
			// Save the new subject to the database
			String insertSubjectSQL = "INSERT INTO subjects (subject_name) VALUES (?)";
			try (PreparedStatement pstmt = conn.prepareStatement(insertSubjectSQL)) {
				pstmt.setString(1, newRepoSubject);
				pstmt.executeUpdate();
			}

			// Add the new repository to the repositories array
			repositories[numRepositories-1] = new QuestionesRepository(newRepoSubject);
			System.out.println("New subject added successfully");
			repositories[numRepositories++] = new QuestionesRepository("create new repository");

			return true;
		} catch (SQLException e) {
			System.out.println("Error occurred while adding new subject to the database: " + e.getMessage());
			return false;
		}

//		repositories[numRepositories-1] = new QuestionesRepository(newRepoSubject);
//		repositories[numRepositories++] = new QuestionesRepository("create new repository");
//		return true;
	}

	public int WriteSingleRepoToDatabase(Connection conn, QuestionesRepository repo) throws SQLException {
		AnswerSQL ans_db = new AnswerSQL(conn);
		QuestionSQL question_db = new QuestionSQL(conn);
		String insert_subject = "INSERT INTO subjects (subject_name) VALUES (?)";
		try(PreparedStatement pstmt = conn.prepareStatement(insert_subject)) {
			pstmt.setString(1, repo.getSubject());
			pstmt.executeUpdate();
		} catch (SQLException e) {
			System.out.println("Error occurred while adding new subject to the database: " + e.getMessage());
			return 0;

		}
		for(int i=0 ; i<repo.getNumOfAnswers(); i++){
			try{
				ans_db.save(repo.getAllAnswers()[i], repo.getSubject());
			}
			catch (SQLException e) {
				System.out.println("Error occurred while adding new answer to the database: " + e.getMessage());
				return 0;
			}
		}
		for(int i=1 ; i<=repo.getNumOfAllQustiones(); i++){
			try{
				question_db.save(repo.getQuestionByNumber(i), repo.getSubject());
			}
			catch (SQLException e) {
				System.out.println("Error occurred while adding new question to the database: " + e.getMessage());
				return 0;

			}
		}
		return 1;
	}
	public void WriteReposToDatabase(Connection conn) throws SQLException {
		if (countSubjects(conn) == 0) {
			for (int i = 0; i < numRepositories - 1; i++) {
				WriteSingleRepoToDatabase(conn, repositories[i]);
			}
		}
	}


}
