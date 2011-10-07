package nl.atcomputing.lpic1examtrainer;

import android.provider.BaseColumns;

/**
 * @author martijn brekhof
 *
 */
public final class ExamTrainer {
	
    // This class cannot be instantiated
    private ExamTrainer() {
    }

    /**
     * Notes table contract
     */
    public static final class Questions implements BaseColumns {
    	
        // This class cannot be instantiated
        private Questions() {}
        
        public static final String TABLE_NAME = "Questions";
        
        public static final String TYPE_OPEN = "open";
        public static final String TYPE_MULTIPLECHOICE = "multiplechoice";
        
        public static final String COLUMN_NAME_TYPE = "type";
        public static final String COLUMN_NAME_QUESTION = "question";
        public static final String COLUMN_NAME_EXHIBIT = "exhibit";
        
    }
    
    public static final class Choices implements BaseColumns {
    	
    	private Choices() {}
    	
    	public static final String TABLE_NAME = "Choices";
    	public static final String COLUMN_NAME_QUESTION_ID = "question_id";
    	public static final String COLUMN_NAME_CHOICE = "choice";
    }
    
    public static final class CorrectAnswers implements BaseColumns {

        // This class cannot be instantiated
        private CorrectAnswers() {}
        
        public static final String TABLE_NAME = "CorrectAnswers";

        public static final String COLUMN_NAME_QUESTION_ID = "question_id";
        public static final String COLUMN_NAME_ANSWER = "answer";
    }
    
    public static final class Answers implements BaseColumns {

        // This class cannot be instantiated
        private Answers() {}
        
        public static final String TABLE_NAME = "Answers";

        public static final String COLUMN_NAME_QUESTION_ID = "question_id";
        public static final String COLUMN_NAME_ANSWER = "answer";
    }
    
    public static final class Scores implements BaseColumns {

        // This class cannot be instantiated
        private Scores() {}
        
        public static final String TABLE_NAME = "Scores";

        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_SCORE = "score";
    }
    
    public static final class ScoresAnswers implements BaseColumns {

        // This class cannot be instantiated
        private ScoresAnswers() {}
        
        public static final String TABLE_NAME = "ScoresAnswers";

        public static final String COLUMN_NAME_EXAM_ID = "exam_id";
        public static final String COLUMN_NAME_QUESTION_ID = "question_id";
        public static final String COLUMN_NAME_ANSWER = "answer";
    }
}