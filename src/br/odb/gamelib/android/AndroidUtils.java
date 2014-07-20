/**
 * 
 */
package br.odb.gamelib.android;

import br.odb.utils.Rect;



/**
 * @author monty
 *
 */
public class AndroidUtils {

	public static android.graphics.Rect toAndroidRect(Rect rect) {
		
		android.graphics.Rect androidRect = new android.graphics.Rect();
		
		androidRect.left = (int) rect.x0;
		androidRect.right = (int) rect.x1;
		androidRect.top = (int) rect.y0;
		androidRect.bottom = (int) rect.y1;
		
		return androidRect;
	}

	public static Rect toGameRect(
			android.graphics.Rect rect) {
		
		Rect gameRect = new Rect();
		
		gameRect.x0 = rect.left;
		gameRect.y0 = rect.top;
		gameRect.x1 = rect.right;
		gameRect.y1 = rect.bottom;
		
		return gameRect;
	}

}
