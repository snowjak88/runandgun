/**
 * 
 */
package org.snowjak.runandgun.config;

/**
 * Holds configuration items relating to input.
 * 
 * @author snowjak88
 *
 */
public class InputConfiguration {
	
	/**
	 * This config's JSON-file will have this name.
	 */
	public static final String CONFIG_FILENAME = "input.json";
	
	private MouseConfig mouse = new MouseConfig();
	
	public MouseConfig mouse() {
		
		return mouse;
	}
	
	public static class MouseConfig {
		
		private int scrollZoneX = 5;
		private int scrollZoneY = 3;
		private float scrollSpeed = 10f;
		
		public int getScrollZoneX() {
			
			return scrollZoneX;
		}
		
		public void setScrollZoneX(int scrollZoneX) {
			
			this.scrollZoneX = scrollZoneX;
		}
		
		public int getScrollZoneY() {
			
			return scrollZoneY;
		}
		
		public void setScrollZoneY(int scrollZoneY) {
			
			this.scrollZoneY = scrollZoneY;
		}
		
		public float getScrollSpeed() {
			
			return scrollSpeed;
		}
		
		public void setScrollSpeed(float scrollSpeed) {
			
			this.scrollSpeed = scrollSpeed;
		}
	}
}
