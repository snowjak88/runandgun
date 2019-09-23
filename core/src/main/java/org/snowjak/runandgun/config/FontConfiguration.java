/**
 * 
 */
package org.snowjak.runandgun.config;

import com.badlogic.gdx.Gdx;

import squidpony.squidgrid.gui.gdx.TextCellFactory;

/**
 * Encapsulates the configuration relating to a particular font (of which we
 * might need to configure multiple for various purposes).
 * 
 * @author snowjak88
 *
 */
public class FontConfiguration {
	
	private String name;
	private String distance;
	private String multiDistance;
	
	private transient TextCellFactory tcf = null;
	
	public FontConfiguration() {
		
	}
	
	FontConfiguration(String name, String distance, String multiDistance) {
		
		this.name = name;
		this.distance = distance;
		this.multiDistance = multiDistance;
	}
	
	public String getName() {
		
		return name;
	}
	
	public void setName(String name) {
		
		this.name = name;
	}
	
	public String getDistance() {
		
		return distance;
	}
	
	public void setDistance(String distance) {
		
		this.distance = distance;
	}
	
	public String getMultiDistance() {
		
		return multiDistance;
	}
	
	public void setMultiDistance(String multiDistance) {
		
		this.multiDistance = multiDistance;
	}
	
	/**
	 * Attempts to get the {@link TextCellFactory} corresponding to this
	 * FontConfiguration.
	 * 
	 * @return an appropriate {@link TextCellFactory}
	 * @throws IllegalStateException
	 *             if GDX has not yet been initialized (which would prevent us from
	 *             loading any resources)
	 */
	public TextCellFactory getTextCellFactory() throws IllegalArgumentException, IllegalStateException {
		
		if(Gdx.files == null)
			throw new IllegalStateException("Cannot get TextCellFactory -- GDX is not yet initialized!");
		
		if (tcf == null)
			synchronized (this) {
				if (tcf == null)
					if (this.multiDistance != null)
						tcf = new TextCellFactory().fontMultiDistanceField(name, multiDistance);
					else if (this.distance != null)
						tcf = new TextCellFactory().fontDistanceField(name, distance);
					else if (this.name != null)
						tcf = new TextCellFactory().font(name);
					else
						tcf = new TextCellFactory().defaultSquareFont();
			}
		
		return tcf;
	}
}
