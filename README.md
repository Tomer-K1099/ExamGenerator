This is a simple Java program for managing an exam system database. The system allows adding, removing, updating, and fetching of questions, answers, exams, and users.
The main purpose of this project is to learn and practice Java OOP prinicipals, SQL querying and Database management using Java's JDBC API.

**Key Features:**
* Automatic Exam generation (Press 8 after choosing a subject repository).
* Exam fetching from database (Press 11 after creating an exam).
* All newly added/removed instances are automatically updated in the database
* Separate Classes that interact with the database (e.g QuestionSQL, AnswerSQL...)
* Triggers: Insert new rows to ‘questions’ table for every new row inserted in ‘multiplechoice’ and in ‘openquestion’ tables, using the corresponding logic.
* Elaborate ERD of tables and relations. 
* Complex SQL queries for fetching, deleting, and counting data from the database.
* Database is hosted on Tomer’s PC as a server, allowing seamless and synchronized connection from authorized accounts using PostgreSQL, without the hassle of creating tables locally.
--- 
**TO RUN LOCALLY**:
1. Clone this repo/download and extract the .zip file.
2. Connect a database. I've used PostgreSQL for this project, and managed it using pgAdmin4.
3. In your database management: Create a new database -> open the Query Tool -> copy the contents of "Table Creation SQL Code.txt" into the query tool -> run the script. You should have a predefined database, if there's no changes, try refreshing the database.
4.  Open the DatabaseManager.java file, and change the credentials to match your databases credentials- The URL typically looks like this : `private static final String URL = "jdbc:postgresql://localhost:5432/MyDatabase"`.
    Change the USER and PASSWORD strings to your postgreSQL user and password.
6. Run the main program.
