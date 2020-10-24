package box2dLight.p3d;

import box2dLight.base.BaseLightHandler;
import box2dLight.base.BaseLight;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.physics.box2d.World;

/**
 * Handler that manages everything related to lights updating and rendering
 *
 * @author rinold
 */
public class P3dRayHandler extends BaseLightHandler {

	protected P3dLightMap lightMap;

	/** TODO: This could be made adaptive to ratio of camera sizes * zoom
	 * vs the CircleShape radius - thus will provide smooth radial shadows
	 * while resizing and zooming in and out */
	protected static int CIRCLE_APPROX_POINTS = 32;

	protected static int MAX_SHADOW_VERTICES = 64;

	protected static int colorReduction = 3;

	protected int shadowBlurPasses = 1;

	/**
	 * Class constructor specifying the physics world from where collision
	 * geometry is taken.
	 *
	 * <p>NOTE: FBO size is 1/4 * screen size and used by default.
	 *
	 * <ul>Default setting are:
	 *     <li>culling = true
	 *     <li>shadows = true
	 *     <li>diffuse = false
	 *     <li>blur = true
	 *     <li>blurNum = 1
	 *     <li>ambientLight = 0f
	 * </ul>
	 *
	 * @see #P3dRayHandler(World, int, int)
	 */
	public P3dRayHandler (World world) {
		super(world, Gdx.graphics.getWidth() / 4, Gdx.graphics
				.getHeight() / 4);
	}

	/**
	 * Class constructor specifying the physics world from where collision
	 * geometry is taken, and size of FBO used for intermediate rendering.
	 *
	 * @see #P3dRayHandler(World)
	 */
	public P3dRayHandler (World world, int fboWidth, int fboHeigth) {
		super(world, fboWidth, fboHeigth);
	}

	@Override
	public void resizeFBO (int fboWidth, int fboHeight) {
		if (lightMap != null) {
			lightMap.dispose();
		}

		lightMap = new P3dLightMap(this, fboWidth, fboHeight);
	}

	@Override
	public void render () {
		lightsRenderedLastFrame = 0;

		Gdx.gl.glDepthMask(false);
		Gdx.gl.glEnable(GL20.GL_BLEND);

		lightMap.frameBuffer.begin();
		Gdx.gl.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		simpleBlendFunc.apply();
		lightShader.bind();
		{
			lightShader.setUniformMatrix("u_projTrans", combined);
			for (BaseLight light : lightList) {
				light.render();
			}
		}

		if (customViewport) {
			lightMap.frameBuffer.end(
					viewportX,
					viewportY,
					viewportWidth,
					viewportHeight);
		} else {
			lightMap.frameBuffer.end();
		}

		lightMap.shadowBuffer.begin();
		Gdx.gl.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		lightShader.bind();
		{
			for (BaseLight light : lightList) {
				light.dynamicShadowRender();
			}
		}

		if (customViewport) {
			lightMap.shadowBuffer.end(
					viewportX,
					viewportY,
					viewportWidth,
					viewportHeight);
		} else {
			lightMap.shadowBuffer.end();
		}

		lightMap.render();
	}

	@Override
	public void setBlurNum (int blurNum) {
		super.setBlurNum(blurNum);
		shadowBlurPasses = blurNum;
	}
}
