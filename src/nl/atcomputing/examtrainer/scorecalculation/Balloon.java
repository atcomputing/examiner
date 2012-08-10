package nl.atcomputing.examtrainer.scorecalculation;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import nl.atcomputing.examtrainer.R;
import android.content.Context;

/**
 * Based on code from http://insanitydesign.com/wp/projects/nehe-android-ports/
 * @author martijn brekhof
 * 
 */

public class Balloon  {

	public static final int AMOUNT_OF_TYPES = 2;
	public static final int TYPE_BLUE = 0;
	public static final int TYPE_RED = 1;

	public float x;
	public float y;
	public int pixel_width;
	public int pixel_height;

	public float liftPercentage; 

	public boolean popped = false; 

	private static double ratio = 2.0;
	
	private int texture;
	private int type;

	/** The buffer holding the vertices */
	private FloatBuffer vertexBuffer;
	/** The buffer holding the texture coordinates */
	private FloatBuffer textureBuffer;

	/** The initial vertex definition */
	private float vertices[] = {
			-1.0f, -1.0f, 0.0f, 	//Bottom Left
			1.0f, -1.0f, 0.0f, 		//Bottom Right
			-1.0f, 1.0f, 0.0f,	 	//Top Left
			1.0f, 1.0f, 0.0f 		//Top Right
	};

	/** The initial texture coordinates (u, v) */	
	private float textureCoords[] = {
			0.0f, 0.0f, 
			1.0f, 0.0f, 
			0.0f, 1.0f, 
			1.0f, 1.0f,
	};


	public Balloon(Context context, int balloon_type, int texture) {

		this.type = balloon_type;

		this.texture = texture;

		vertices[1] *= ratio; 
		vertices[4] *= ratio;
		vertices[7] *= ratio; 
		vertices[10] *= ratio;

		ByteBuffer byteBuf = ByteBuffer.allocateDirect(vertices.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		vertexBuffer = byteBuf.asFloatBuffer();
		vertexBuffer.put(vertices);
		vertexBuffer.position(0);

		byteBuf = ByteBuffer.allocateDirect(textureCoords.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		textureBuffer = byteBuf.asFloatBuffer();
		textureBuffer.put(textureCoords);
		textureBuffer.position(0);

		this.liftPercentage = 1.0f;
	}

	public void draw(GL10 gl) {
		//Enable the vertex, texture and normal state
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

		gl.glBindTexture(GL10.GL_TEXTURE_2D, this.texture);

		//Point to our buffers
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);

		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);

		//Draw the vertices as triangle strip
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, vertices.length / 3);

		//Disable the client state before leaving
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	}

	/**
	 * @return the balloon type
	 */
	public int getType() {
		return this.type;
	}

	/**
	 * Sets the upward lift in percentages
	 * @param percentage from 0.0 to 1.0
	 */
	public void setLift(float percentage) {
		this.liftPercentage = percentage;
	}


	/**
	 * @return the upward lift in percentages from 0.0 to 1.0
	 */
	public float getLift() {
		return this.liftPercentage;
	}

	public static int getReferenceDrawable(int t) {
		switch(t) {
		case TYPE_BLUE:
			return R.drawable.aj_balloon_blue;
		case TYPE_RED:
			return R.drawable.aj_balloon_red;
		}
		return -1;
	}
}
