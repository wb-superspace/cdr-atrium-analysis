package evaluations;

import cdr.geometry.primitives.Point3D;
import cdr.spatialAnalysis.model.isovistModel.locations.IsovistLocation;
import models.visibilityInteriorsModel.VisibilityInteriorsLayout;
import models.visibilityInteriorsModel.VisibilityInteriorsModel;
import templates.Model;

public class VisibilityEvaluationField extends EvaluationField {
		
	String label = "Visibility";
	
	@Override
	public void evaluate(Model m) {

		if (m == null) return;
		
		long startTime = System.nanoTime();	
		this.evaluateVisibility2D((VisibilityInteriorsModel) m);
		long endTime = System.nanoTime();
		float duration = (endTime - startTime) / 1000000000.0f;
		System.out.println("visibility map -> " + duration + "s");
	}
	
	private void evaluateVisibility2D(VisibilityInteriorsModel m) {
		
		for (float key : m.getLayouts().keySet()) {
			
			VisibilityInteriorsLayout layout = m.getLayout(key);
																	
			for (Point3D pt : layout.getGrid(this.resolution)) {
				
				IsovistLocation location = layout.getIsovist(pt);
				
				if (location == null) {
					continue;
				}
				
				float visibleArea =  location.getIsovist().getVisibilityPolygon().area();
																
				this.setValue(label, key, pt, visibleArea, "m2", ValueType.INTERPOLATE);
			}
		}
	}
	
	@Override
	public String getLabel() {
		return this.label;
	}
}
