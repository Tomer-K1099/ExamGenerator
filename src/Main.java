//package db_proj;

//
//import java.io.File;
//import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
//import java.util.Date;


public class Main {

	public static Scanner s = new Scanner(System.in);

	public static void main(String[] args) throws IOException {
		final String INVALID = "Invalid value\n";
		final int EXIT = 0;
		 UserSQL UserSQL = new UserSQL();
		 User user = null;
		  int choice;
		  boolean userConect = false;
		 try {
			Connection conn = DatabaseManager.getConnection();
			do {
				 
		        System.out.println("Welcome! Please choose an option:");
		        System.out.println("1. Log in");
		        System.out.println("2. Register");
		        choice = s.nextInt();
		        s.nextLine(); // Consume newline
		        
		        
			      

		        if (choice == 1) {
		            // Log in process
		            System.out.println("Enter your username:");
		            String username = s.nextLine();

		            System.out.println("Enter your password:");
		            String password = s.nextLine();
		            
		            
		            if (UserSQL.validateUser(conn, username, password)) {
	                    System.out.println("Login successful!");
	                    user = new User(username, password);
	                    userConect = true;
	                } else {
	                    System.out.println("Invalid username or password. Please try again.");
	                }
		        }else if(choice == 2) {
		        	// Registration process
		            System.out.println("Enter a username:");
		            String username = s.nextLine();
		        	
		        	
		        	
		            if (UserSQL.usernameExists(conn, username)) {
	                    System.out.println("Username already exists. Please choose a different one.");
	                } else {
	                    System.out.println("Enter a password:");
	                    String password = s.nextLine();
	                    User newUser = new User(username, password);
	                    UserSQL.registerUser(conn, newUser);
	                    System.out.println("Registration successful!");
	                    userConect = true;
	                }
		        }
				
			} while(!userConect);
		
	        

	    conn.close();    
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
               
            
		 
		RepoManager repositoriesManager = new RepoManager();
		try {
			repositoriesManager.initRepositories();

		} catch (Exception e) {
			System.out.println("Could not find initialization file..");
		}
		try {
			Connection conn = DatabaseManager.getConnection();
//			repositoriesManager.initRepositoriesFromDB(conn);
			repositoriesManager.WriteReposToDatabase(conn);
			conn.close();
		}catch(SQLException e) {
			System.out.println("Error occured while initializing the database.");
		}

		int index = -1;
		while(true){
			
			System.out.println("Please choose repository subject");
			System.out.println(repositoriesManager.toString());
			System.out.println("To Exit press--> 0");
			index = s.nextInt();
			
			if (index == EXIT) {
				System.out.println("Good bye");
				break;
			}
			
			if (index == repositoriesManager.getNumRepositories()) {
				System.out.println("Please enter subject for the new repsitory");
				String newRepoSubject =  s.next();
				try {
					Connection conn = DatabaseManager.getConnection();
					repositoriesManager.addRepository(conn,newRepoSubject);
					conn.close();
				} catch (Exception e) {
					System.out.println("Could not Enter new subject..");
				}

			}
		
			QuestionesRepository questionRepository = repositoriesManager.getRepository(index-1);
	//		QuestionesRepository questionRepository = new QuestionesRepository();
			final int BACK = 0;
			final int OP1 = 1;
			final int OP2 = 2;
			final int OP3 = 3;
			final int OP4 = 4;
			final int OP5 = 5;
			final int OP6 = 6;
			final int OP7 = 7;
			final int OP8 = 8;
			final int OP9 = 9;
			final int OP10 = 10;
			final int OP11 = 11;
			final int OP12 = 12;
			final int OP13 = 13;
			
			
			do {
				showMenu(questionRepository.getSubject());
				choice = s.nextInt();
				switch (choice) {
					case OP1:
						System.out.println(questionRepository);
						break;

					case OP2:
						s.nextLine();
						System.out.println("Enter Answer Description: ");
						String answerDescription = s.nextLine();
						try {
							Connection conn = DatabaseManager.getConnection();
							AnswerSQL ansDB = new AnswerSQL(conn);
							Answer newAnswer = new Answer(answerDescription);
							ansDB.save(newAnswer,questionRepository.getSubject());
							if (questionRepository.addAnswerToRepository(answerDescription,newAnswer)) {
								System.out.println("Answer added successfully to database and repository\n");
							} else {
								System.out.println("Answer added to database but already exists in repository\n");
							}
							conn.close();
						} catch (SQLException e) {
							System.out.println("Error occurred while saving answer to database: " + e.getMessage());
						}
						break;

					case OP3:
						try {
							System.out.println("Which question would you like to add an answer to?");
							int questienIndex2 = s.nextInt();
							Question q = questionRepository.getQuestionByNumber(questienIndex2);
							if (q == null || q instanceof OpenQuestion) {
								System.out.println(INVALID);
								break;
							}
							System.out.println("Answers that already exist in the qustion: \n"
									+ ((MultipleChoiceQuestion) questionRepository.getQuestionByNumber(questienIndex2)).showeQuestion());
							System.out.println(questionRepository.showAnswersFromRepository((MultipleChoiceQuestion) questionRepository.getQuestionByNumber(questienIndex2)));
							System.out.println("Enter the number of the new answer");
							int ansIndx2 = s.nextInt();
							if (ansIndx2 > 0 && ansIndx2 <= questionRepository.getNumOfAnswers()) {
								System.out.println("Is this answer correct? Write true/false ");
								boolean correctness = s.nextBoolean();
								if (questionRepository.addAnswerFromRepoToQusteion(questienIndex2, ansIndx2, correctness)) {

									String sql = "INSERT INTO Multiple_Choice_Answers (question_id, answer_id, is_correct) VALUES (?, ?, ?)";
									Connection conn = DatabaseManager.getConnection();
									PreparedStatement pstmt2 = conn.prepareStatement(sql);
									Answer ans = questionRepository.allAnswers[ansIndx2-1];
									int answerId = ans.getId();
									int questionId = q.getId();
									pstmt2.setInt(1, questionId);  // The generated question_id from the MultipleChoice table
									pstmt2.setInt(2, answerId);          // The answer_id from the Answer object
									pstmt2.setBoolean(3, correctness);  // Assuming isCorrect() returns a boolean indicating if it's the correct answer
									pstmt2.executeUpdate();
									sql = "UPDATE multiplechoice SET number_of_answers = number_of_answers + 1 WHERE question_id = ?";
									try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
										pstmt.setInt(1, q.getId());
										pstmt.executeUpdate(); // Execute the update operation
									}
									System.out.println("Added successfully\n");
									break;
								}
							}

							System.out.println(INVALID);
							break;


						}
						catch (InputMismatchException | SQLException e) {
							System.out.println(e.getMessage());

						}
						break;

					case OP4:
						System.out.println("Enter new question description");
						s.nextLine();
						String description = s.nextLine();
						System.out.println("Choose the difficulty level of the question");
						Question.eDifficultyLevel[] allLevels = Question.eDifficultyLevel.values();
						for (int i = 0; i < allLevels.length; i++) {
							System.out.println(allLevels[i].ordinal()+1 + ") "+ allLevels[i].name());
						}
						int levelInput = s.nextInt(); // difficulty level input

						// Validate the input
						if (levelInput < 1 || levelInput > allLevels.length) {
							System.out.println("Invalid difficulty level\n");
							break;
						}

						Question.eDifficultyLevel theLevel = allLevels[levelInput - 1];

						System.out.println("What type of question?\n"
								+ "1) Multiple choice question\n"
								+ "2) Open qustion press -->2\n");
						int ans = s.nextInt(); //question type
						try {
							Connection conn = DatabaseManager.getConnection();
							QuestionSQL QuestionSQL = new QuestionSQL(conn);
							AnswerSQL ansDB = new AnswerSQL(conn);

							switch(ans) {
								case 1:
									if (questionRepository.addMultipleChoiceQuestion(description,theLevel)) {
//							saveRepositories(repositoriesManager,"repository.dat");

										// Get the last added question
										Question question = questionRepository.allQuestions[questionRepository.numOfAllQustiones-1];




										// Save question to the database
										QuestionSQL.save(question, questionRepository.getSubject());
										System.out.println("Question successfully added to repository and database\n");
									}

									else
										System.out.println(INVALID);
									break;

								case 2:
									System.out.println("Enter the correct answer description: ");
									s.nextLine();
									String answer = s.nextLine();
									Answer newAns = new Answer(answer);
									ansDB.save(newAns,questionRepository.getSubject());

									if (questionRepository.addOpenQuestion(description,theLevel,answer,newAns)) {
										// Get the last added question
										Question question = questionRepository.allQuestions[questionRepository.numOfAllQustiones-1];

										// Save question to the database
										QuestionSQL.save(question, questionRepository.getSubject());
										System.out.println("Question successfully added to repository and database\n");
									}
									else
										System.out.println(INVALID);
									break;

							}
							conn.close();
						} catch (SQLException e) {
							System.out.println("Error occurred while saving question to database: " + e.getMessage());
						}
						break;




					case OP5:
					    System.out.println("Which question's answer would you like to delete?");
						for(int i=1; i<=questionRepository.numOfAllQustiones; i++) {
							System.out.println(i + "." + questionRepository.getQuestionByNumber(i).getQuestionDescription());
						}
					    int questionIndex6 = s.nextInt();
					    Question q5 = questionRepository.getQuestionByNumber(questionIndex6);
					    boolean outOfBounds = (questionIndex6 < 1 ); // if the question in the array is out of bounds// || questionIndex6 > questionRepository.getNumOfAllQuestions()
					    
					    if (outOfBounds || q5 instanceof OpenQuestion) { // Check if q5 exists and is not an open question
					        System.out.println(INVALID);
					        break;
					    }
					    
					    System.out.println("Which answer to delete?");
						try {
							Connection conn = DatabaseManager.getConnection();
							for (int i = 1; i <= repositoriesManager.countAnswersForQuestion(conn,q5); i++) {
								System.out.println(i + "." + questionRepository.getQuestionByNumber(i).getQuestionDescription());
							}
						}catch(SQLException e){
								System.out.println(e.getMessage());
						}
					    int ansIndex6 = s.nextInt(); // Check if the answer exists


					    int goodID = ((MultipleChoiceQuestion) q5).getAnswers()[ansIndex6 - 1].getId();
					    if (((MultipleChoiceQuestion) questionRepository.getQuestionByNumber(questionIndex6)).deleteAnswerOfQuestion(ansIndex6)) {
					        System.out.println("Answer successfully deleted\n");
					    } else {
					        System.out.println(INVALID);
					    }
					    break;

						
						
						
						
	
				case OP6:
					System.out.println("What is the number of the question you would like to delete?");
					int numOfQuestion = s.nextInt();
					int questionID = questionRepository.allQuestions[numOfQuestion-1].getId();
					if (questionRepository.deleteQuestion(numOfQuestion))// Check q exist + define int
					{
						try {
								String sql = "DELETE FROM questions WHERE question_id = ?";
								Connection conn = DatabaseManager.getConnection();
								try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
									pstmt.setInt(1, questionID);
									pstmt.executeUpdate(); // Execute the delete operation
								}
								System.out.println("Question successfully deleted\n");

						}
						catch (SQLException e) {
							System.out.println(e.getMessage());
						}
					}
					else
						System.out.println(INVALID);
	
					break;
	
				case OP7:// creating a exem manually
					System.out.println("How many questions would you like on the exam? ");
					int amountOfQuestions = s.nextInt();// get the amount of question the user want
					int amountOfExistingQustions = questionRepository.getNumOfAllQustiones();// get the amount of all
																								// qustions in the
					// repository
					if (amountOfQuestions < 1 || amountOfQuestions > amountOfExistingQustions) {
						System.out.println(INVALID);
						break;
					}
	
					System.out.println(questionRepository);// print repository to show the user
					ManuallyExam manuallyExam = new ManuallyExam(amountOfQuestions);
					int generatedTestId = 0;
					try {
						Connection conn = DatabaseManager.getConnection();
						String sql = "INSERT INTO tests (user_id) VALUES (?) RETURNING test_id";
						PreparedStatement pstmt = conn.prepareStatement(sql);
							pstmt.setString(1, user.getUsername());
							ResultSet rs = pstmt.executeQuery();
							if (rs.next()) {
								 generatedTestId = rs.getInt(1);
							}
							if(generatedTestId<=0) {
								System.out.println("Bad TEST ID");
								return;
							}
						manuallyExam.creatExam(questionRepository,generatedTestId,conn);
							conn.close();
					} catch (MoreQuestionsThanAllowedException | NotEnoughAnswersInQuestionException e) {
						System.out.println(e.getMessage());
						break;
						
					} catch (SQLException e) {
						throw new RuntimeException(e);
					}
					//	manuallyExam.deployToFile();
	
					break;
					
					/////// creating auto exam
				case OP8:	
					System.out.println("How many questions would you like in the exam? ");
					 amountOfQuestions = s.nextInt();// get the amount of question the user want
					 amountOfExistingQustions = questionRepository.getNumOfAllQustiones();// get the amount of all
					 
					 if (amountOfQuestions < 1 || amountOfQuestions > amountOfExistingQustions) {
							System.out.println(INVALID);
							break;
						}
					 
						 AutomaticExam AutomaticExam = new AutomaticExam(amountOfQuestions);
						int generatedTestId2 = 0;
						 try {

							 Connection conn = DatabaseManager.getConnection();
							 String sql = "INSERT INTO tests (user_id) VALUES (?) RETURNING test_id";
							 PreparedStatement pstmt = conn.prepareStatement(sql);
							 pstmt.setString(1, user.getUsername());
							 ResultSet rs = pstmt.executeQuery();
							 if (rs.next()) {
								 generatedTestId2 = rs.getInt(1);
							 }
							 if(generatedTestId2<=0) {
								 System.out.println("Bad TEST ID");
								 return;
							 }
							 AutomaticExam.creatExam(questionRepository,generatedTestId2,conn);
							 conn.close();
							 
						 } catch (Exception e) {
						 System.out.println(e.getMessage());

							
						}

						 break;
					 case 9:
						 System.out.println("Get Answers");
						 try {
							 Connection conn2 = DatabaseManager.getConnection();
							 List<Answer> answersList = new ArrayList<>();
								boolean flag = false;
							 do {
								AnswerSQL ans_db2 = new AnswerSQL(conn2);
								for(int i =0;i<21;i++) {
									Answer answer = ans_db2.findById(i + 1);  // Retrieve answer by ID (assuming IDs start at 1)
									if (answer != null) {
										answersList.add(answer);
									}
									else{
										System.out.println("Invalid answer ID");
										break;
									}
								}
								 for (Answer answer : answersList) {
									 System.out.println(answer);
								 }
								flag = true;
								conn2.close();
							 }while(!flag);

						 } catch (SQLException e) {
							 e.printStackTrace();  // Handle the SQL exception
						 }
						 break;
//					case 10:
//						System.out.println("Save Answers");
//						try {
//							Connection conn2 = DatabaseManager.getConnection();
//							boolean flag = false;
//							do {
//								AnswerSQL answer_db = new AnswerSQL(conn2);
//								for(int i =0;i<questionRepository.numOfAllAnswers;i++) {
//									if(questionRepository.allAnswers[i] == null)
//										break;
//									answer_db.save(questionRepository.allAnswers[i],questionRepository.getSubject());
//								}
//								flag = true;
//								conn2.close();
//							}while(!flag);
//
//						} catch (SQLException e) {
//							e.printStackTrace();  // Handle the SQL exception
//						}

//					 break;
//					case 11:
//						System.out.println("Saving Questions i hope");
//						try {
//							Connection conn2 = DatabaseManager.getConnection();
//							boolean flag = false;
//							do {
//								QuestionSQL q_sql = new QuestionSQL(conn2);
//								for(int i =0;i<questionRepository.numOfAllQustiones;i++) {
//									if(questionRepository.allQuestions[i] == null)
//										break;
//									q_sql.save(questionRepository.allQuestions[i],questionRepository.getSubject());
//								}
//								flag = true;
//								conn2.close();
//							}while(!flag);
//
//						} catch (SQLException e) {
//							e.printStackTrace();  // Handle the SQL exception
//						}
//							break;
					case 12:
						System.out.println("Trying to fetch a Question");
						Random r =  new Random();
						try {
							Connection conn2 = DatabaseManager.getConnection();
							boolean flag = false;
							do {
								QuestionSQL q_db = new QuestionSQL(conn2);
								Question qTry = q_db.findById(r.nextInt(1,questionRepository.getNumOfAllQustiones()+1));
								System.out.println(qTry);
								conn2.close();
								flag = true;
							}while(!flag);

						} catch (SQLException e) {
							e.printStackTrace();  // Handle the SQL exception
						}
						break;

				case 13:
					try{
						Connection conn = DatabaseManager.getConnection();
						ExamSQL examdb = new ExamSQL(conn);
						ArrayList<Integer> ids = new ArrayList<Integer>();
						ids = examdb.getExamsIDs();
						if (ids.size() == 0){
							System.out.println("No Exams in Database. Generate an exam first.");
						}
						else{
							System.out.println("Which Exam would you like to fetch?");
							for(int id : ids){
								System.out.println(id);
							}
							int choice_id = s.nextInt();
							examdb.fetchAndDisplayExam(choice_id);
						}

					} catch(SQLException e){
						System.out.println(e.getMessage());
					}

				case BACK:
					break;
	
				default:
					System.out.println("Invalid input");
				}
	
			} while (choice != EXIT);
	
		
		}
	}

	private static void showMenu(String q_repo) {
		q_repo= q_repo.toUpperCase();
		System.out.println("Menu for " + "\u001B[1m" + q_repo+ "\u001B[0m" + " repository");
		System.out.println("1 | SHOW ALL questiones and answers		");
		System.out.println("2 | ADD NEW ANSWER to repository 		");
		System.out.println("3 | ADD an EXISTING ANSWER to question	");
		System.out.println("4 | ADD a NEW QUESTION	 				");
		System.out.println("5 | DELETE an ANSWER to a question		");
		System.out.println("6 | DELETE a QUESTION					");
		System.out.println("7 | CREATE an exam MANUALLY				");
		System.out.println("8 | CREATE an exam AUTOMATICALLY 		");
		System.out.println("9 | GET ALL ANSWERS from Database 		");
		System.out.println("12| FETCH a QUESTION from Database 		");
		System.out.println("13| FETCH EXAM from Database 			");
		System.out.println("0 | GO BACK								");

	}

	

}
