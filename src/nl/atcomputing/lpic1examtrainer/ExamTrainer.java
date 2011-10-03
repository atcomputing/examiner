package nl.atcomputing.lpic1examtrainer;

import android.provider.BaseColumns;

public final class ExamTrainer {
	public static final int DIALOG_ENDOFEXAM_ID = 1;
	
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
        
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_TYPE = "type";
        public static final String COLUMN_NAME_QUESTION = "question";
        public static final String COLUMN_NAME_EXHIBIT = "exhibit";
        public static final String COLUMN_NAME_ANSWERS = "answers";
        public static final String COLUMN_NAME_CORRECT_ANSWERS = "correct_answers";
        
    }
    
    public static final class Answers implements BaseColumns {

        // This class cannot be instantiated
        private Answers() {}
        
        public static final String TABLE_NAME = "Answers";

        public static final String COLUMN_NAME_QUESTION_ID = "question_id";
        public static final String COLUMN_NAME_ANSWER = "answer";
    }
}