

package nl.atcomputing.examtrainer.exam;

import android.graphics.Bitmap;

/**
 * @author martijn brekhof
 *
 */

public class Balloon  {

	private int x;
	private int y;
	private Bitmap bitmap;
	
	public Balloon(int xCoordinate, int yCoordinate, Bitmap b) {
		this.x = xCoordinate;
		this.y = yCoordinate;
		this.bitmap = b;
	}
	
	public Balloon(Bitmap b) {
		this.x = 0;
		this.y = 0;
		this.bitmap = b;
	}
	
	protected void setCoords(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	protected void move(int x, int y) {
		this.x += x;
		this.y += y;
	}
	
	protected int getX() {
		return this.x;
	}
	
	protected int getY() {
		return this.y;
	}
	
	protected Bitmap getBitmap() {
		return bitmap;
	}
}
