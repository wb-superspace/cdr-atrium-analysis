package color;

import org.jcolorbrewer.ColorBrewer;

import cdr.colour.scale.LinearInterpolationColorBrewerGradientMap;
import evaluations.VisibilityInteriorsEvaluation;

public class VisibilityInteriorsColourMaps {

	public static LinearInterpolationColorBrewerGradientMap getSourceColourMap(VisibilityInteriorsEvaluation e) {
		
		return new LinearInterpolationColorBrewerGradientMap(ColorBrewer.PuBu, 0, 1, !e.isValueReversed());
	}
	
	public static LinearInterpolationColorBrewerGradientMap getSinkColourMap(VisibilityInteriorsEvaluation e) {

		return new LinearInterpolationColorBrewerGradientMap(ColorBrewer.YlOrRd, 0, 1, !e.isValueReversed());
	}
}
