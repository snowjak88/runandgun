package org.snowjak.runandgun;

import java.time.Duration;
import java.time.Instant;

import org.snowjak.runandgun.concurrent.PerFrameProcess;
import org.snowjak.runandgun.context.Context;
import org.snowjak.runandgun.screen.MyScreen;

import com.badlogic.gdx.ApplicationAdapter;

import squidpony.squidmath.CoordPacker;

/**
 * This is a small, not-overly-simple demo that presents some important features
 * of SquidLib and shows a faster, cleaner, and more recently-introduced way of
 * displaying the map and other text. Features include dungeon map generation,
 * field of view, pathfinding (to the mouse position), continuous noise (used
 * for a wavering torch effect), language generation/ciphering, a colorful glow
 * effect, and ever-present random number generation (with a seed). You can
 * increase the size of the map on most target platforms (but GWT struggles with
 * large... anything) by changing gridHeight and gridWidth to affect the visible
 * area or bigWidth and bigHeight to adjust the size of the dungeon you can move
 * through, with the camera following your '@' symbol. <br>
 * The assets folder of this project, if it was created with SquidSetup, will
 * contain the necessary font files (just one .fnt file and one .png are needed,
 * but many more are included by default). You should move any font files you
 * don't use out of the assets directory when you produce a release JAR, APK, or
 * GWT build.
 */
public class App extends ApplicationAdapter {
	
	private final Context ctx = Context.get();
	
	private Instant lastRender = Instant.now();
	
	private PerFrameProcess worldProcess = new PerFrameProcess() {
		
		@Override
		public void processFrame(float delta) {
			
			try {
				Context.get().engine().update(delta);
			} catch (Throwable t) {
				t.printStackTrace(System.err);
			}
		}
	};
	
	@Override
	public void create() {
		
		CoordPacker.init();
		Context.get().setScreen(new MyScreen());
		
		worldProcess.start();
	}
	
	@Override
	public void render() {
		
		final Instant now = Instant.now();
		final long nanosSince = Duration.between(lastRender, now).toNanos();
		final float secondsSince = (float) nanosSince / 1e9f;
		lastRender = now;
		
		Context.get().clock().update(secondsSince);
		worldProcess.update(secondsSince);
		
		Context.get().screen().render(secondsSince);
	}
	
	@Override
	public void resize(int width, int height) {
		
		super.resize(width, height);
		
		Context.get().screen().resize(width, height);
	}
	
	@Override
	public void dispose() {
		
		worldProcess.stop();
		
		//
		// Dispose of all disposable elements of the current Context.
		//
		ctx.dispose();
	}
}
