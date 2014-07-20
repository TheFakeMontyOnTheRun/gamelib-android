/**
 * 
 */
package br.odb.gamelib.android;

import java.util.HashMap;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Region;
import android.graphics.Shader;
import br.odb.gamerendering.rendering.RasterImage;
import br.odb.gamerendering.rendering.RenderingContext;
import br.odb.libsvg.ColoredPolygon;
import br.odb.libsvg.SVGParsingUtils.Gradient;
import br.odb.libsvg.SVGUtils;
import br.odb.utils.Color;
import br.odb.utils.Rect;
import br.odb.utils.math.Vec2;

/**
 * @author monty
 * 
 */
public class AndroidCanvasRenderingContext extends RenderingContext {

	private Canvas canvas;
	Paint paint;


	public void prepareWithCanvasAndPaint(Canvas canvas, Paint paint) {
		this.canvas = canvas;
		this.paint = paint;
		setReadyForRendering(true);
	}

	@Override
	public void fillRect(Color color, Rect rect) {
		
		android.graphics.Rect androidRect = AndroidUtils.toAndroidRect(rect);
		androidRect.top += currentOrigin.y;
		androidRect.bottom += currentOrigin.y;
		androidRect.left += currentOrigin.x;
		androidRect.right += currentOrigin.x;
		
		paint.setStyle(Style.FILL_AND_STROKE);
		paint.setColor(color.getARGBColor());
		paint.setAlpha((int) (currentAlpha * 255));
		canvas.drawRect(androidRect, paint);
	}

	@Override
	public void prepareForRendering() {
		// TODO Auto-generated method stub

	}

	@Override
	public void endRendering() {
		this.paint = null;
		this.canvas = null;
		setReadyForRendering(false);
	}

	@Override
	public void drawColoredPolygon(ColoredPolygon pol, Rect bounds,
			String style, HashMap<String, Gradient> gradients) {
		

		Vec2 origin = bounds.getP0().add(this.currentOrigin);
		paint.setColor(0xFF0000FF);
		paint.setAlpha((int) (currentAlpha * 255));
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
//		paint.setAntiAlias(true);

		float scale;
		float diffX = 0;
		float diffY = 0;

		String gradient = null;

		scale = 1;

		if (pol.xpoints == null || pol.ypoints == null || pol.npoints <= 0)
			return;

		Path path = new Path();
		path.moveTo((origin.x + pol.xpoints[0] ), origin.y
				+ pol.ypoints[0] );

		for (int c = 0; c < pol.npoints; ++c) {
			
			if (((Vec2) pol.controlPoints.get(c)).isValid()) {
				
				Vec2 control = (Vec2) pol.controlPoints.get(c);

				path.cubicTo(origin.x + (pol.xpoints[c] * scale) - diffX,
						origin.y + pol.ypoints[c] * scale - diffY, origin.x
								+ control.x * scale, origin.y + control.y
								* scale - diffY, origin.x
								+ (pol.xpoints[c + 1] * scale) - diffX,
						origin.y + pol.ypoints[c + 1] * scale - diffY);
				++c;
			} else
				path.lineTo( origin.x + pol.xpoints[c],
						origin.y + pol.ypoints[c]);
		}

		path.close();
		paint.setShader(null);
		paint.setAlpha(255);

		if (pol.originalStyle != null) {
			pol.color = SVGUtils.parseColorFromStyle(pol.originalStyle);
			gradient = SVGUtils.parseGradientFromStyle(pol.originalStyle);
		}

		if (gradient != null) {

			Gradient g0 = gradients.get(gradient);
//
//			if (g0.stops == null && g0.link != null) {
//				g1 = gradients.get(g0.link);
//			} else {
//				g1 = g0;
//			}

			Color color1;
			Color color2;

			color1 = SVGUtils.parseColorFromStyle(g0.stops[0].style,
					"stop-color", "stop-opacity");
			color2 = SVGUtils.parseColorFromStyle(g0.stops[1].style,
					"stop-color", "stop-opacity");

			if (pol.color != null) {
				// paint.setAlpha( pol.color.getA() );
				color1.setA((int) ((color1.getA() / 255.0f)
						* (pol.color.getA() / 255.0f) * 255));
				color2.setA((int) ((color2.getA() / 255.0f)
						* (pol.color.getA() / 255.0f) * 255));
			}

			LinearGradient lg = new LinearGradient(g0.x1 * scale,
					g0.y1 * scale, g0.x2 * scale, g0.y2 * scale,
					(int) color1.getARGBColor(), (int) color2.getARGBColor(),
					Shader.TileMode.CLAMP);

			paint.setShader(lg);

		} else if (pol.color != null) {

			paint.setColor((int) pol.color.getARGBColor());

			paint.setAlpha((int) (pol.color.getA() *  currentAlpha ) );
//			paint.setAlpha((int) (pol.color.getA()));			
		} else
			paint.setColor(0xFF000000);

		canvas.drawPath(path, paint);
		paint.setAlpha((int) (currentAlpha * 255));
		
		//show verteces
//		for ( int p3 = 0; p3 < pol.npoints; ++p3 ) {
//			
//			this.fillRect( new Color( 0,0,0 ), new Rect( pol.xpoints[ p3 ] + bounds.x0 - 5, pol.ypoints[ p3 ] + bounds.y0 - 5, 10, 10 ) );
//		}
	}

	@Override
	public void saveClipRect() {
		canvas.save();
	}

	@Override
	public void restoreClipRect() {
		canvas.restore();
	}

	@Override
	public void drawBitmap(RasterImage image, Vec2 p0) {
		AndroidRasterImage ari = (AndroidRasterImage) image;
		canvas.drawBitmap(ari.bitmap, p0.x, p0.y, paint);
	}

	@Override
	public void drawBitmap(RasterImage image, Rect bounds) {

		AndroidRasterImage ari = (AndroidRasterImage) image;
		android.graphics.Rect src = new android.graphics.Rect();
		src.top = 0;
		src.left = 0;
		src.right = (int) bounds.getDX();
		src.bottom = (int) bounds.getDY();
		android.graphics.Rect dst = AndroidUtils.toAndroidRect(bounds);
		dst.left += currentOrigin.x;
		dst.top += currentOrigin.y;
		dst.bottom += currentOrigin.y;
		dst.right += currentOrigin.x;
		canvas.drawBitmap(ari.bitmap, src, dst, paint);
	}

	public void setClipRect(Rect bounds) {
		android.graphics.Rect rect = AndroidUtils.toAndroidRect(bounds);
		canvas.clipRect(rect, Region.Op.REPLACE);
	}

	@Override
	public void setCurrentAlpha(float f) {
		super.setCurrentAlpha(f);

		paint.setAlpha((int) (currentAlpha * 255));
	}

	@Override
	public void drawBitmap(RasterImage image, Vec2 p0, Vec2 scale, int rotation) {
		AndroidRasterImage ari = (AndroidRasterImage) image;

		Matrix rotator = new Matrix();

		rotator.postRotate( rotation, image.getWidth() / 2, image.getHeight() / 2 );
		rotator.postScale( scale.x, scale.y );
		rotator.postTranslate( p0.x, p0.y );
		
		canvas.drawBitmap(ari.bitmap, rotator, paint);

	}

	public void setAntiAlias(boolean b) {
		if ( paint != null ) {
			paint.setAntiAlias( b );
		}		
	}
}
