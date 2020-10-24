package box2dLight;

import box2dLight.base.BaseLight;
import box2dLight.base.BaseLightHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.physics.box2d.World;

public class RayHandler extends BaseLightHandler {

	protected static boolean diffuse = false;

	/**
	 * Enables/disables usage of diffuse algorithm
	 *
	 * <p>If set to true lights are blended using the diffuse shader. This is
	 * more realistic model than normally used as it preserve colors but might
	 * look bit darker and also it might improve performance slightly.
	 */
	public static void useDiffuseLight (boolean flag) {
		diffuse = flag;
	}

	/**
	 * @return if the usage of diffuse algorithm is enabled
	 *
	 * <p>If set to true lights are blended using the diffuse shader. This is
	 * more realistic model than normally used as it preserve colors but might
	 * look bit darker and also it might improve performance slightly.
	 */
	public static boolean isDiffuseLight() {
		return diffuse;
	}

	public RayHandler (World world) {
		super(world, Gdx.graphics.getWidth() / 4, Gdx.graphics
				.getHeight() / 4);
	}

	/**
	 * Class constructor specifying the physics world from where collision
	 * geometry is taken, and size of FBO used for intermediate rendering.
	 *
	 * @see #RayHandler(World)
	 */
	public RayHandler (World world, int fboWidth, int fboHeigth) {
		super(world, fboWidth, fboHeigth);
	}

	@Override
	public void resizeFBO (int fboWidth, int fboHeight) {
		if (lightMap != null) {
			lightMap.dispose();
		}

		lightMap = new LightMap(this, fboWidth, fboHeight);
	}

	@Override
	public void render () {
		prepareRender();
		lightMap.render();
	}

	/**
	 * Prepare all lights for rendering.
	 *
	 * <p>You should need to use this method only if you want to render lights
	 * on a frame buffer object. Use {@link #render()} otherwise.
	 *
	 * <p><b>NOTE!</b> Don't call this inside of any begin/end statements.
	 *
	 * @see #renderOnly()
	 * @see #render()
	 */
	public void prepareRender() {
		lightsRenderedLastFrame = 0;

		Gdx.gl.glDepthMask(false);
		Gdx.gl.glEnable(GL20.GL_BLEND);
		simpleBlendFunc.apply();

		boolean useLightMap = (shadows || blur);
		if (useLightMap) {
			lightMap.frameBuffer.begin();
			Gdx.gl.glClearColor(0f, 0f, 0f, 0f);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		}

		ShaderProgram shader = customLightShader != null ? customLightShader : lightShader;
		shader.bind();
		{
			shader.setUniformMatrix("u_projTrans", combined);
			if (customLightShader != null) updateLightShader();
			for (BaseLight light : lightList) {
				if (customLightShader != null) updateLightShaderPerLight(light);
				light.render();
			}
		}

		if (useLightMap) {
			if (customViewport) {
				lightMap.frameBuffer.end(
						viewportX,
						viewportY,
						viewportWidth,
						viewportHeight);
			} else {
				lightMap.frameBuffer.end();
			}
		}
	}

	/**
	 * Manual rendering method for all lights tha can be used inside of
	 * begin/end statements
	 *
	 * <p>Use this method if you want to render lights in a frame buffer
	 * object. You must call {@link #prepareRender()} before calling this
	 * method. Also, {@link #prepareRender()} must not be inside of any
	 * begin/end statements
	 *
	 * @see #prepareRender()
	 */
	public void renderOnly() {
		lightMap.render();
	}
}
