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
	    
	    Button cancel = (Button) this.findViewById(R.id.selectexam_cancel);
	    cancel.setOnClickListener(new View.OnClickListener() {

	          public void onClick(View v) {
	        	  finish();
	          }
	        });
	    
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
		  ExamTrainer.examReview = false;
    	  ExamTrainer.examDatabaseName = ExamTrainer.examTitle + "-" + examDate;
    	  
    	  Intent intent = new Intent(ExamTrainerSelectExamActivity.this, ExamQuestionsActivity.class);
    	  intent.putExtra("question", 1);
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
	    public View getView(final int position, View view, ViewGroup parent) {
	      // A ViewHolder keeps references to children views to avoid
	      // unneccessary calls
	      // to findViewById() on each row.
	    	final ViewHolder holder;

	      // When convertView is not null, we can reuse it directly, there is
	      // no need
	      // to reinflate it. We only inflate a new View when the convertView
	      // supplied
	      // by ListView is null.
	      if (view == null) {
	        view = (View) mInflater.inflate(R.layout.selectexam_entry, null);

	        holder = new ViewHolder();
	        
	        int index = cursor.getColumnIndex(ExamTrainer.Exams.COLUMN_NAME_EXAMTITLE);
	        holder.examTitle = cursor.getString(index);
	        index = cursor.getColumnIndex(ExamTrainer.Exams.COLUMN_NAME_DATE);
		    holder.examDate = cursor.getString(index);
		    index = cursor.getColumnIndex(ExamTrainer.Exams.COLUMN_NAME_AMOUNTOFITEMS);
		    holder.examAmountOfItems = cursor.getInt(index);
		    index = cursor.getColumnIndex(ExamTrainer.Exams.COLUMN_NAME_ITEMSNEEDEDTOPASS);
		    holder.examItemsNeededToPass = cursor.getInt(index);
		    
		    holder.examTitleView = (TextView) view.findViewById(R.id.manageExamsEntryTitle);
	        holder.examStartButton = (Button) view.findViewById(R.id.manageExamsDelete);
	        
		    
	        
	        view.setTag(holder);
	      } else {
	        // Get the ViewHolder back to get fast access to the TextView
	        // and the ImageView.
	        holder = (ViewHolder) view.getTag();
	      }

	      int index = cursor.getColumnIndex(ExamTrainer.Exams.COLUMN_NAME_EXAMTITLE);
	      holder.examTitleView.setText(cursor.getString(index));
	      
	      holder.examStartButton.setOnClickListener(new View.OnClickListener() {
	          public void onClick(View v) {
	        	  
	          }
	        });
		    
		    view.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					Toast.makeText(context, holder.examTitle + "\n" +
							context.getString(R.string.installed_on) + 
							" " + holder.examDate + "\n" +
							context.getString(R.string.questions) + 
							": " +  holder.examAmountOfItems + "\n" +
							context.getString(R.string.correct_answer_required_to_pass) +
							": " +  holder.examItemsNeededToPass + "\n"
							, Toast.LENGTH_LONG).show();
				}
			});
	      return view;
	    }

	    class ViewHolder {
			  String examTitle;
			  long examID;
			  String examDate;
			  String url;
			  int examAmountOfItems;
			  int examItemsNeededToPass;
		      TextView examTitleView;
		      Button examStartButton;
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