package box2dLight.p3d;

import shaders.DynamicShadowShader;
import box2dLight.base.BaseLightMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/**
 * Light map for pseudo-3d rendering
 *
 * @author rinold
 */
public class P3dLightMap extends BaseLightMap {
	P3dRayHandler rayHandler;

	FrameBuffer shadowBuffer;
	ShaderProgram shadowShader;

	public P3dLightMap(P3dRayHandler manager, int fboWidth, int fboHeight) {
		super(manager, fboWidth, fboHeight);
		rayHandler = manager;

		shadowShader = DynamicShadowShader.createShadowShader(P3dRayHandler.colorReduction);
		shadowBuffer = new FrameBuffer(Format.RGBA8888, fboWidth,
				fboHeight, false);
	}

	@Override
	public void render () {
		boolean needed = rayHandler.getLightsRenderedLastFrame() > 0;
		if (needed && rayHandler.isBlur()) {
			gaussianBlur(frameBuffer, rayHandler.getBlurNum());
			gaussianBlur(shadowBuffer, rayHandler.shadowBlurPasses);
		}

		if (lightMapDrawingDisabled)
			return;

		frameBuffer.getColorBufferTexture().bind(1);
		shadowBuffer.getColorBufferTexture().bind(0);

		final Color c = lightHandler.getAmbientLight();
		shadowShader.bind();
		{
			lightHandler.diffuseBlendFunc.apply();
			shadowShader.setUniformf("ambient", c.r, c.g, c.b, c.a);
			shadowShader.setUniformi("u_texture", 1);
			shadowShader.setUniformi("u_shadows", 0);
			lightMapMesh.render(shadowShader, GL20.GL_TRIANGLE_FAN);
		}

		Gdx.gl20.glDisable(GL20.GL_BLEND);
	}

	@Override
	public void dispose () {
		super.dispose();
		shadowBuffer.dispose();
		shadowShader.dispose();
	}
}
