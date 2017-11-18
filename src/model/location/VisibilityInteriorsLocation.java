package model.location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cdr.geometry.primitives.Point3D;
import evaluations.VisibilityInteriorsEvaluation;
import jpantry.models.generic.geometry.LayoutGeometry;
import jpantry.models.generic.layout.Layout;
import jpantry.models.projection.location.ProjectionLocation;
import model.path.VisibilityInteriorsPath;
import model.zone.Zone;

public class VisibilityInteriorsLocation extends ProjectionLocation {

	private Layout<LayoutGeometry> layout;
	
	private VisibilityInteriorsEvaluation evaluation;
	
	private Zone zone;
			
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
	
	public VisibilityInteriorsLocation(Point3D point, Layout<LayoutGeometry> layout, boolean isModifiable) {
		super(point);
		
		this.layout = layout;
		this.isModifiable = isModifiable;
	}

	public Layout<LayoutGeometry> getLayout() {
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
		
	public void setZone(Zone zone) {
		this.zone = zone;
	}
	
	public Zone getZone() {
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
