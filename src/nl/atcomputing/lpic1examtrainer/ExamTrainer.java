package nl.atcomputing.lpic1examtrainer;

import android.net.Uri;
import android.provider.BaseColumns;

public final class ExamTrainer {
    public static final String AUTHORITY = "nl.atcomputing.provider.ExamTrainer";
    public static final String SCHEME = "content://";
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
        
        public static final Uri CONTENT_URI =  Uri.parse(SCHEME + AUTHORITY + "/questions");
    }
    
    public static final class Score implements BaseColumns {

        // This class cannot be instantiated
        private Score() {}
        
        public static final String TABLE_NAME = "Scores";
        private static final String PATH_SCORES_ID = "/scores/";
        public static final Uri CONTENT_URI =  Uri.parse(SCHEME + AUTHORITY + "/scores");
        
        /**
         * The content URI base for a single score. Callers must
         * append a numeric question id to this Uri to set or retrieve 
         * the user's answer
         */
        public static final Uri CONTENT_ID_URI_BASE
            = Uri.parse(SCHEME + AUTHORITY + PATH_SCORES_ID);
        public static final String COLUMN_NAME_QUESTION_ID = "question_id";
        public static final String COLUMN_NAME_ANSWER = "answer";
    }
}