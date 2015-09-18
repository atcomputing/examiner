/**
 * 
 * Copyright 2012 AT Computing BV
 *
 * This file is part of Examiner.
 *
 * Examiner is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Examiner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Examiner.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package nl.atcomputing.examtrainer.scorecalculation;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;

/**
 * @author martijn brekhof
 *
 */

public class Textures {

	private Context context;
	private int[] textures;
	private int[] pixel_widths;
	private int[] pixel_heights;

	private int[] drawables = {
			Balloon.getReferenceDrawable(Balloon.TYPE_BLUE),
			Balloon.getReferenceDrawable(Balloon.TYPE_RED)
	};

	public Textures(Context context) {
		this.context = context;
	}

	public void loadTextures(GL10 gl) {
		InputStream[] inputStreams = getInputStreams();

		this.textures = new int[inputStreams.length];
		this.pixel_widths = new int[inputStreams.length];
		this.pixel_heights = new int[inputStreams.length];

		gl.glGenTextures(inputStreams.length, this.textures, 0);

		for( int i=0; i < inputStreams.length; i++) {
			Bitmap bitmap = null;
			try {
				bitmap = BitmapFactory.decodeStream(inputStreams[i]);
				this.pixel_widths[i] = bitmap.getWidth();
				this.pixel_heights[i] = bitmap.getHeight();
			} finally {
				try {
					inputStreams[i].close();
				} catch (IOException e) {
				}
			}

			gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[i]);

			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
			GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);

			bitmap.recycle();
		}
	}

	/**
	 * @param resource_id Resource identifier of drawable used to generate texture
	 * @return texture number associated with drawable
	 */
	public int getTexture(int resource_id) {
		for(int i = 0; i < this.drawables.length; i++) {
			if( this.drawables[i] == resource_id ) {
				return this.textures[i];
			}
		}
		return -1;
	}

	public int getWidth(int resource_id) {
		for(int i = 0; i < this.drawables.length; i++) {
			if( this.drawables[i] == resource_id ) {
				return this.pixel_widths[i];
			}
		}
		return 1;
	}

	public int getHeight(int resource_id) {
		for(int i = 0; i < this.drawables.length; i++) {
			if( this.drawables[i] == resource_id ) {
				return this.pixel_heights[i];
			}
		}
		return 1;
	}

	private InputStream[] getInputStreams() {
		InputStream[] inputStreams = new InputStream[this.drawables.length];
		for(int i=0; i < this.drawables.length; i++) {
			inputStreams[i] = this.context.getResources().openRawResource(this.drawables[i]);
		}
		return inputStreams;
	}
}