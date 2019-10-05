/**
 * 
 */
package org.snowjak.runandgun.util.loaders;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;

/**
 * @author snowjak88
 *
 */
public interface Loader<T> extends JsonSerializer<T>, JsonDeserializer<T> {
	
}
