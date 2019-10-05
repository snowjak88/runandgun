/**
 * 
 */
package org.snowjak.runandgun.context;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

import org.snowjak.runandgun.util.loaders.Loader;

import com.badlogic.ashley.core.Component;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;

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
	
	private static final Logger LOG = Logger.getLogger(GsonBuilder.class.getName());
	
	/**
	 * @return a newly-configured {@link Gson} instance
	 */
	@SuppressWarnings("unchecked")
	public static Gson get() {
		
		final com.google.gson.GsonBuilder gsonBuilder = new com.google.gson.GsonBuilder();
		
		//@formatter:off
		gsonBuilder.serializeSpecialFloatingPointValues()
		           //.setPrettyPrinting()
				   .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES);
		//@formatter:on
		
		//
		// Add all loader-classes -- those that implement Gson's TypeAdapter.
		//
		
		final ScanResult sr = new ClassGraph().enableClassInfo().enableAnnotationInfo()
				.whitelistPackages("org.snowjak.runandgun").scan();
		
		for (ClassInfo ci : sr.getClassesImplementing(Loader.class.getName())
				.filter((c) -> !c.isAbstract() && !c.isInterfaceOrAnnotation())) {
			
			//
			// Loader has one type-parameter, definined the type which the Loader is capable
			// of de/serializing.
			//
			final String adaptedType = ci.getTypeSignature().getSuperinterfaceSignatures().stream()
					.filter(ts -> ts.getBaseClassName().equals(Loader.class.getName())).findFirst()
					.map(ts -> ts.getTypeArguments().get(0).toString()).orElse(null);
			
			if (adaptedType == null)
				continue;
			
			try {
				final Constructor<?> typeAdapterConstructor = ci.loadClass().getConstructor();
				
				LOG.info("Registering a new Gson Loader for <" + adaptedType + "> -- " + ci.getName());
				gsonBuilder.registerTypeAdapter(Class.forName(adaptedType), typeAdapterConstructor.newInstance());
				
			} catch (NoSuchMethodException e) {
				throw new RuntimeException("Cannot instantiate JSON de/serializer -- Loader implementation ("
						+ ci.getName() + ") doesn't have a 0-argument constructor!", e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException("Cannot instantiate JSON de/serializer -- Loader implementation ("
						+ ci.getName() + ") threw an exception upon construction!", e);
			} catch (InstantiationException e) {
				throw new RuntimeException("Cannot instantiate JSON de/serializer -- Loader implementation ("
						+ ci.getName() + ") cannot be constructed!", e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException("Cannot instantiate JSON de/serializer -- Loader implementation ("
						+ ci.getName() + ") is not accessible!", e);
			} catch (IllegalArgumentException e) {
				throw new RuntimeException("Cannot instantiate JSON de/serializer -- Loader implementation ("
						+ ci.getName() + ") was not fed appropriate arguments!", e);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException("Cannot instantiate JSON de/serializer -- Loader implementation ("
						+ ci.getName() + ") is not targeting an accessible type!", e);
			}
			
		}
		
		final RuntimeTypeAdapterFactory<Component> componentTypeAdapterFactory = RuntimeTypeAdapterFactory
				.of(Component.class);
		
		for (ClassInfo ci : sr.getClassesImplementing(Component.class.getName())
				.filter((c) -> !c.isAbstract() && !c.isInterfaceOrAnnotation())) {
			
			LOG.info("Registering the Component sub-type [" + ci.getName() + "] under the type-name '"
					+ ci.getSimpleName() + "'");
			componentTypeAdapterFactory.registerSubtype((Class<? extends Component>) ci.loadClass());
		}
		
		gsonBuilder.registerTypeAdapterFactory(componentTypeAdapterFactory);
		
		//
		// Finally, build the Gson instance.
		//
		
		return gsonBuilder.create();
		
	}
}
