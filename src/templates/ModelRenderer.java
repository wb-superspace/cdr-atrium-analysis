package templates;

import javax.media.opengl.GL2;

import cdr.joglFramework.camera.GLCamera;

public interface ModelRenderer {

	public void renderFill(GL2 gl, Model model);
	
	public void renderLines(GL2 gl, Model model);
	
	public void renderGUI(GL2 gl, int width, int height, GLCamera cam, Model model);
}
