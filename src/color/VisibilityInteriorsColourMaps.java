package color;

import org.jcolorbrewer.ColorBrewer;

import cdr.colour.scale.LinearInterpolationColorBrewerGradientMap;

public class VisibilityInteriorsColourMaps {

	private static LinearInterpolationColorBrewerGradientMap sourceColourMap = new LinearInterpolationColorBrewerGradientMap(ColorBrewer.PuBu, 0, 1, true);
	private static LinearInterpolationColorBrewerGradientMap sinkColourMap = new LinearInterpolationColorBrewerGradientMap(ColorBrewer.YlOrRd, 0, 1, true);
	
	public static LinearInterpolationColorBrewerGradientMap getSourceColourMap() {
		return sourceColourMap;
	}
	
	public static LinearInterpolationColorBrewerGradientMap getSinkColourMap() {
		return sinkColourMap;
	}
}
