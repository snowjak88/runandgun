/**
 * 
 */
package org.snowjak.runandgun.context;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;

/**
 * Class encapsulating {@link Gson} instantiation and configuration.
 * 
 * @author snowjak88
 *
 */
public class GsonBuilder {
	
	/**
	 * @return a newly-configured {@link Gson} instance
	 */
	public static Gson get() {
		
		final com.google.gson.GsonBuilder gsonBuilder = new com.google.gson.GsonBuilder();
		
		//@formatter:off
		gsonBuilder.serializeSpecialFloatingPointValues()
		           .setPrettyPrinting()
				   .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES);
		//@formatter:on
		
		//
		// Add all loader-classes -- those that implement Gson's TypeAdapter.
		//
		
		final ScanResult sr = new ClassGraph().enableClassInfo().whitelistPackages("org.snowjak.runandgun").scan();
		for (ClassInfo ci : sr.getSubclasses(TypeAdapter.class.getName())
				.filter((c) -> !c.isAbstract() && !c.isInterfaceOrAnnotation())) {
			
			//
			// TypeAdapter has one type-parameter, definined the type which the TypeAdapter
			// is capable of de/serializing.
			//
			final Class<?> adaptedType = ci.getTypeSignature().getTypeParameters().get(0).getClass();
			
			try {
				final Constructor<?> typeAdapterConstructor = ci.loadClass().getConstructor();
				
				gsonBuilder.registerTypeAdapter(adaptedType, typeAdapterConstructor.newInstance());
				
			} catch (NoSuchMethodException e) {
				throw new RuntimeException("Cannot instantiate JSON de/serializer -- TypeAdapter implementation ("
						+ ci.getName() + ") doesn't have a 0-argument constructor!", e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException("Cannot instantiate JSON de/serializer -- TypeAdapter implementation ("
						+ ci.getName() + ") threw an exception upon construction!", e);
			} catch (InstantiationException e) {
				throw new RuntimeException("Cannot instantiate JSON de/serializer -- TypeAdapter implementation ("
						+ ci.getName() + ") cannot be constructed!", e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException("Cannot instantiate JSON de/serializer -- TypeAdapter implementation ("
						+ ci.getName() + ") is not accessible!", e);
			} catch (IllegalArgumentException e) {
				throw new RuntimeException("Cannot instantiate JSON de/serializer -- TypeAdapter implementation ("
						+ ci.getName() + ") was not fed appropriate arguments!", e);
			}
			
		}
		
		//
		// Finally, build the Gson instance.
		//
		
		return gsonBuilder.create();
		
	}
}
