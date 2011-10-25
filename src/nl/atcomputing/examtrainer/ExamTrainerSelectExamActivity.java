package nl.atcomputing.examtrainer;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author martijn brekhof
 *
 */

//Example code on how to setup nice selectionbox: 
//http://www.codemobiles.com/forum/viewtopic.php?t=876

public class ExamTrainerSelectExamActivity extends ListActivity {
	  private EfficientAdapter adap;
	  private static Cursor cursor;
	  
	  public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    requestWindowFeature(Window.FEATURE_NO_TITLE);
	    setContentView(R.layout.selectexam);
	    
	    ExamTrainerDbAdapter examTrainerDbHelper = new ExamTrainerDbAdapter(this);
		examTrainerDbHelper.open();
		
		cursor = examTrainerDbHelper.getExams();
		
	    adap = new EfficientAdapter(this);
	    setListAdapter(adap);
	    
	    examTrainerDbHelper.close();
	  }

	  protected void startExam(Cursor mCursor) {
		  int index = mCursor.getColumnIndex(ExamTrainer.Exams.COLUMN_NAME_EXAMTITLE);
		  ExamTrainer.examTitle = mCursor.getString(index);
		  index = mCursor.getColumnIndex(ExamTrainer.Exams.COLUMN_NAME_DATE);
		  String examDate = mCursor.getString(index);
		  
    	  Intent intent = new Intent(ExamTrainerSelectExamActivity.this, ExamQuestionsActivity.class);
    	  intent.putExtra("question", 1);
		  intent.putExtra("review", false);
		  intent.putExtra("databaseName", ExamTrainer.examTitle + "-" + examDate);
		  startActivity(intent);
	  }
	  
	  public class EfficientAdapter extends BaseAdapter implements Filterable {
	    private LayoutInflater mInflater;
	    Context context;
	    
	    public EfficientAdapter(Context context) {
	      // Cache the LayoutInflate to avoid asking for a new one each time.
	      mInflater = LayoutInflater.from(context);
	      this.context = context;
	    }

	    /**
	     * Make a view to hold each row.
	     * 
	     * @see android.widget.ListAdapter#getView(int, android.view.View,
	     *      android.view.ViewGroup)
	     */
	    public View getView(final int position, View convertView, ViewGroup parent) {
	      // A ViewHolder keeps references to children views to avoid
	      // unneccessary calls
	      // to findViewById() on each row.
	      ViewHolder holder;

	      // When convertView is not null, we can reuse it directly, there is
	      // no need
	      // to reinflate it. We only inflate a new View when the convertView
	      // supplied
	      // by ListView is null.
	      if (convertView == null) {
	        convertView = (View) mInflater.inflate(R.layout.selectexam_entry, null);

	        convertView.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					int index = cursor.getColumnIndex(ExamTrainer.Exams.COLUMN_NAME_EXAMTITLE);
				    String examTitle = cursor.getString(index);
				    index = cursor.getColumnIndex(ExamTrainer.Exams.COLUMN_NAME_DATE);
				    String examDate = cursor.getString(index);
				    index = cursor.getColumnIndex(ExamTrainer.Exams.COLUMN_NAME_AMOUNTOFITEMS);
				    int examAmountOfItems = cursor.getInt(index);
				    index = cursor.getColumnIndex(ExamTrainer.Exams.COLUMN_NAME_ITEMSNEEDEDTOPASS);
				    int examItemsNeededToPass = cursor.getInt(index);
					Toast.makeText(context, examTitle + "\n" +
							context.getString(R.string.installed_on) + " " + examDate + "\n" +
							context.getString(R.string.questions) + ": " +  examAmountOfItems + "\n" +
							context.getString(R.string.correct_answer_required_to_pass) + ": " +  examItemsNeededToPass + "\n"
							, Toast.LENGTH_LONG).show();
				}
			});
	        
	        // Creates a ViewHolder and store references to the two children
	        // views
	        // we want to bind data to.
	        holder = new ViewHolder();
	        holder.examTitle = (TextView) convertView.findViewById(R.id.selectexamEntryTitle);
	        holder.buttonStartExam = (Button) convertView.findViewById(R.id.selectexamStart);
	        
	        holder.buttonStartExam.setOnClickListener(new View.OnClickListener() {

	          public void onClick(View v) {
	        	  startExam(cursor);
	          }
	        });
	        
	        convertView.setTag(holder);
	      } else {
	        // Get the ViewHolder back to get fast access to the TextView
	        // and the ImageView.
	        holder = (ViewHolder) convertView.getTag();
	      }

	      int index = cursor.getColumnIndex(ExamTrainer.Exams.COLUMN_NAME_EXAMTITLE);
	      holder.examTitle.setText(cursor.getString(index));
	      
	      return convertView;
	    }

	    class ViewHolder {
	      TextView examTitle;
	      TextView examDate;
	      TextView examAmountOfQuestions;
	      Button buttonStartExam;
	    }

	    public android.widget.Filter getFilter() {
	      // TODO Auto-generated method stub
	      return null;
	    }

	    public long getItemId(int position) {
	      // TODO Auto-generated method stub
	      return 0;
	    }

	    public int getCount() {
	      // TODO Auto-generated method stub
	      return cursor.getCount();
	    }

	    public Object getItem(int position) {
	      // TODO Auto-generated method stub
	      return null;
	    }

	  }
}