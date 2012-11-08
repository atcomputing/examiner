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

	/**
	 * Override in fragment to return the fragment's title
	 * @return null if not set, String otherwise
	 */
	public String getTitle() {
		return null;
	}
	
	/**
	 * Calls onItemClickListener in activity that implemented FragmentListener
	 * @param id identifier of item that was clicked
	 */
	public void onItemClickListener(long id) {
		this.abstractFragmentListener.onItemClickListener(id);
	}
	
	/**
	 * Calls onButtonClickListener in activity that implemented FragmentListener
	 * @param fragment fragment in which button event was generated
	 * @param id identifier of button clicked
	 */
	public void onButtonClickListener(AbstractFragment fragment, long id) {
		this.abstractFragmentListener.onButtonClickListener(fragment, id);
	}
	
	abstract public void updateView();
}
