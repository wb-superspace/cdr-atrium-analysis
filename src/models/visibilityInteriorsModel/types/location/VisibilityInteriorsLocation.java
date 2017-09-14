package models.visibilityInteriorsModel.types.location;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cdr.geometry.primitives.Point3D;
import cdr.geometry.primitives.Polygon3D;
import evaluations.VisibilityInteriorsEvaluation;
import models.isovistProjectionModel.types.IsovistProjectionFilter;
import models.isovistProjectionModel.types.IsovistProjectionGeometryType;
import models.isovistProjectionModel.types.IsovistProjectionLocation;
import models.isovistProjectionModel.types.IsovistProjectionPolygon;
import models.visibilityInteriorsModel.types.layout.VisibilityInteriorsLayout;
import models.visibilityInteriorsModel.types.path.VisibilityInteriorsPath;
import models.visibilityInteriorsModel.types.zone.VisibilityInteriorsZone;

public class VisibilityInteriorsLocation extends IsovistProjectionLocation {

	private VisibilityInteriorsLayout layout;
	
	private VisibilityInteriorsEvaluation evaluation;
	
	private VisibilityInteriorsZone zone;
			
	private Map<VisibilityInteriorsLocation, VisibilityInteriorsPath> connectivityPaths = new HashMap<>();
	
	private Map<VisibilityInteriorsLocation, VisibilityInteriorsPath> visibilityPaths = new HashMap<>();
	
	private List<VisibilityInteriorsPath> connectivityFlows = new ArrayList<>();
	
	private List<VisibilityInteriorsPath> visibilityFlows = new ArrayList<>();
	
	private Set<LocationType> locationTypes = new HashSet<>();
	
	private boolean isModifiable = false;
	private boolean isValid = true;
	private boolean isActive = false;
		
	public enum LocationType {
		ACCESS,
		ENTRANCE,
		CIRCULATION,
		UNIT,
	}
	
	public VisibilityInteriorsLocation(Point3D point, VisibilityInteriorsLayout layout, boolean isModifiable) {
		super(point);
		
		this.layout = layout;
		this.isModifiable = isModifiable;
	}

	public VisibilityInteriorsLayout getLayout() {
		return this.layout;
	}
	
	public boolean isModifiable() {
		return this.isModifiable;
	}
	
	public void setActive(boolean active) {
		this.isActive = active;
	}
	
	public boolean isActive() {
		return this.isActive;
	}
	
	public void addType(LocationType type) {
		this.locationTypes.add(type);
	}
	
	public Set<LocationType> getTypes() {
		return this.locationTypes;
	}
	
	public void setValidity(boolean isValid) {
		this.isValid = isValid;
	}
	
	public boolean isValid() {
		return this.isValid;
	}
		
	public void setZone(VisibilityInteriorsZone zone) {
		this.zone = zone;
	}
	
	public VisibilityInteriorsZone getZone() {
		return this.zone;
	}
				
	public void setConnectivityPath(VisibilityInteriorsLocation target, VisibilityInteriorsPath path) {
		this.connectivityPaths.put(target, path);
	}
	
	public VisibilityInteriorsPath getConnectivityPath(VisibilityInteriorsLocation target) {
		return this.connectivityPaths.get(target);
	}
	
	public Iterable<VisibilityInteriorsLocation> getConnectivityPathLocations() {
		return this.connectivityPaths.keySet();
	}
	
	public void setVisibilityPath(VisibilityInteriorsLocation target, VisibilityInteriorsPath path) {
		this.visibilityPaths.put(target, path);
	}
	
	public VisibilityInteriorsPath getVisibilityPath(VisibilityInteriorsLocation target) {
		return this.visibilityPaths.get(target);
	}
	
	public Iterable<VisibilityInteriorsLocation> getVisibilityPathLocations() {
		return this.visibilityPaths.keySet();
	}
	
	public void addConnectivityFlow(VisibilityInteriorsPath flow) {
		this.connectivityFlows.add(flow);
	}
	
	public List<VisibilityInteriorsPath> getConnectivityFlows() {
		return this.connectivityFlows;
	}
	
	public void addVisibilityFlow(VisibilityInteriorsPath flow) {
		this.visibilityFlows.add(flow);
	}
	
	public List<VisibilityInteriorsPath> getVisibilityFlows() {
		return this.visibilityFlows;
	}
	
	public void setEvaluation(VisibilityInteriorsEvaluation evaluation) {
		this.evaluation = evaluation;
	}
			
	public VisibilityInteriorsEvaluation getEvaluation() {
		return this.evaluation;
	}
	
}
