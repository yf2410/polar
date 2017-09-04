package org.sprite2d.apps.pp;

import android.graphics.Bitmap;

public interface IPainter {
	
	public Bitmap getLastPicture();
	
	public Bitmap getCurrentDrawing();
	
	public int getRequestedOrientation();
	
}
