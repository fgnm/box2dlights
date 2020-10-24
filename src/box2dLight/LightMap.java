package box2dLight;

import box2dLight.base.BaseLightHandler;
import box2dLight.base.BaseLightMap;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import shaders.DiffuseShader;
import shaders.ShadowShader;
import shaders.WithoutShadowShader;

public class LightMap extends BaseLightMap {

	private ShaderProgram withoutShadowShader;
	private ShaderProgram shadowShader;
	private ShaderProgram diffuseShader;

	public LightMap (BaseLightHandler rayHandler, int fboWidth, int fboHeight) {
		super(rayHandler, fboWidth, fboHeight);

		shadowShader = ShadowShader.createShadowShader();
		diffuseShader = DiffuseShader.createShadowShader();

		withoutShadowShader = WithoutShadowShader.createShadowShader();
	}
	@Override
	public void render () {
		boolean needed = lightHandler.getLightsRenderedLastFrame() > 0;

		if (needed && lightHandler.isBlur())
			gaussianBlur(frameBuffer, lightHandler.getBlurNum());

		if (lightMapDrawingDisabled)
			return;
		frameBuffer.getColorBufferTexture().bind(0);

		// at last lights are rendered over scene
		if (lightHandler.isShadows()) {
			final Color c = lightHandler.getAmbientLight();
			ShaderProgram shader = shadowShader;
			if (RayHandler.isDiffuseLight()) {
				shader = diffuseShader;
				shader.bind();
				lightHandler.diffuseBlendFunc.apply();
				shader.setUniformf("ambient", c.r, c.g, c.b, c.a);
			} else {
				shader.bind();
				lightHandler.shadowBlendFunc.apply();
				shader.setUniformf("ambient", c.r * c.a, c.g * c.a,
						c.b * c.a, 1f - c.a);
			}
			lightMapMesh.render(shader, GL20.GL_TRIANGLE_FAN);
		} else if (needed) {
			lightHandler.simpleBlendFunc.apply();
			withoutShadowShader.bind();
			lightMapMesh.render(withoutShadowShader, GL20.GL_TRIANGLE_FAN);
		}

		Gdx.gl20.glDisable(GL20.GL_BLEND);
	}

	@Override
	public void dispose () {
		super.dispose();

		withoutShadowShader.dispose();
		shadowShader.dispose();
		diffuseShader.dispose();
	}
}
