//package db_proj;

import java.io.Serializable;

public class Answer implements Serializable {
	private static final long serialVersionUID = 1L;
	private int id;
	private String answerStr;
	
	public Answer(String answerDescription) {
		this.answerStr = answerDescription;
	}
    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

	public String getAnswerDescription() {
		return answerStr;
	}

	public void setAnswerDescription(String answerDescription) {
		if(answerDescription != null && !answerDescription.isEmpty()) 
			this.answerStr = answerDescription;
	}
	




	@Override
	public String toString() {
		return   answerStr ;
	}

}
