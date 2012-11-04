package nl.atcomputing.examtrainer.fragments;

import android.app.Activity;

import com.actionbarsherlock.app.SherlockFragment;
/**
 * @author martijn brekhof
 *
 */

abstract public class AbstractFragment extends SherlockFragment {
	protected FragmentListener abstractFragmentListener;
	
	public interface FragmentListener {
		/**
		 * Called when user selects a score
		 */
		public void onItemClickListener(long id);
		
		/**
		 * Called when user clicks the resume/start exam button
		 * @param fragment
		 * @param id depending on the fragment this can be the question ID or exam ID or ...
		 */
		public void onButtonClickListener(AbstractFragment fragment, long id);
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        // Make sure activity implemented ExamQuestionListener
        try {
            this.abstractFragmentListener = (FragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FragmentListener");
        }
        
        setHasOptionsMenu(true);
    }

	abstract public void updateView();
}
