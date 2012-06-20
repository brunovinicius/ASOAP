package org.asoap.util;


import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.asoap.annotation.SOAPObject;
import org.asoap.annotation.SOAPProperty;
import org.asoap.annotation.SOAPService;
import org.asoap.annotation.SOAPServiceOperation;
import org.asoap.parser.ArrayParser;
import org.asoap.parser.ByteArrayParser;
import org.asoap.parser.ComplexTypeParser;
import org.asoap.parser.ListParser;
import org.asoap.parser.MapParser;
import org.asoap.parser.NullParser;
import org.asoap.parser.Parser;
import org.asoap.parser.PrimitiveParser;
import org.asoap.parser.VoidParser;

public class CacheRepository {

	private static final HashMap<Class<?>, ClassData> classDataCache = new HashMap<Class<?>, ClassData>();
	private static final HashMap<Method, ServiceData> serviceInterfaceDataCache = new HashMap<Method, ServiceData>();
	
	public static ClassData getClassData(Class<?> clazz) {
		ClassData classData = _getClassData(clazz);
		if (classData == null) {
			throw new IllegalArgumentException("Class " + clazz.getSimpleName() + " doesn't have a SOAPObject annotation.");
		}

		return classData;
	}
	
	public static ServiceData addServiceInterface(Class<?> declaringClass) throws MalformedURLException {
		return addServiceInterface(declaringClass, null);
	}
	
	public static ServiceData addServiceInterface(Class<?> declaringClass, URL serviceUrl) throws MalformedURLException {
		final SOAPService annotation = declaringClass.getAnnotation(SOAPService.class);
		
		if (annotation == null) {
			throw new IllegalArgumentException("Class " + declaringClass.getSimpleName() + " doesn't have a SOAPServiceInterface annotation.");
		}
		
		final ServiceData serviceInterfaceData = new ServiceData(annotation, serviceUrl);
		
		final Method[] methods = declaringClass.getMethods();
		for (Method m : methods) {
			SOAPServiceOperation methodAnnotation = m.getAnnotation(SOAPServiceOperation.class);
			if (methodAnnotation != null) {					
				OperationData methodData = new OperationData(m, methodAnnotation);
				serviceInterfaceData.methods.put(m, methodData);
				serviceInterfaceDataCache.put(m, serviceInterfaceData);
			}
		}	
		
		return serviceInterfaceData;
	}
	
	public static ServiceData getServiceInterfaceData(Method method) throws MalformedURLException
	{
		ServiceData serviceInterfaceData = serviceInterfaceDataCache.get(method);
		if (serviceInterfaceData == null) {
			serviceInterfaceData = addServiceInterface(method.getDeclaringClass());
		}
		return serviceInterfaceData;
	}

	private static ClassData _getClassData(Class<?> clazz) {
		ClassData classData = classDataCache.get(clazz);

		if (classData == null) {
			SOAPObject annotation = clazz.getAnnotation(SOAPObject.class);

			if (annotation == null) {
				return null;
			}

			classData = new ClassData(clazz, annotation);
			
			Field[] fields = clazz.getDeclaredFields();
			for (Field field : fields) {
				SOAPProperty propertyAnnotation = field.getAnnotation(SOAPProperty.class);
				if (propertyAnnotation != null) {
					field.setAccessible(true);

					FieldData fd = new FieldData(field, propertyAnnotation);
					classData.fields.add(fd);
				}
			}
			
			ClassData superClassData = _getClassData(clazz.getSuperclass());
			if (superClassData != null) {
				for (FieldData f : superClassData.getFields())
					classData.getFields().add(f);
			}
			
			classDataCache.put(clazz, classData);
		}

		return classData;
	}
	
	private static boolean shallDetectParserByType(Class<? extends Parser> parserClass) {
		return parserClass == NullParser.class;
	}
	
	private static Parser createParserInstance(Class<? extends Parser> parserClass) {
		try {
			return parserClass.newInstance();
		} catch (Exception e) {
			throw new IllegalArgumentException("MethodResponseParser " + parserClass + " defines no public, no-args constructor. Could not instantiate.");
		}
	}
	
	private static Parser getParserByType(java.lang.reflect.Type type)
	{
		// validate parameters
		if(type == null)
			throw new IllegalArgumentException("Type cannot be null");
		
		Parser result = null;
		
		// if we got a ParameterizedType
		if (type instanceof ParameterizedType) {
			
			ParameterizedType parameterizedType = (ParameterizedType)type;
			Class<?> clazz = (Class<?>) parameterizedType.getRawType(); 
			
			// we are only supporting parameterizedTypes for Collections
			if (!Collection.class.isAssignableFrom(clazz) && !Map.class.isAssignableFrom(clazz))
				throw new IllegalArgumentException("Only Collections are supported for having generic type arguments (i.e: Map<K, V>, List<T>, etc).");
			
			// get actual generic type arguments
			java.lang.reflect.Type[] genericTypes = parameterizedType.getActualTypeArguments();
			
			// if there's generic arguments declared for this collection, we can determine its type (i.e. if its a collection of dogs or balls)
			if (genericTypes.length > 0) {
				
				if (Map.class.isAssignableFrom(clazz)) {			
					// create map parser, and a parser for the map key and values.
					Parser keyParser = getParserByType(genericTypes[0]);
					Parser valueParser = getParserByType(genericTypes[1]);
					result = new MapParser(clazz, keyParser, valueParser);
				
				} else if(List.class.isAssignableFrom(clazz)) {
					
					// create a list, and its elements parser
					Parser elementsParser = getParserByType(genericTypes[0]);
					result = new ListParser(clazz, elementsParser);
				
				} else {
					// Collection type not yet supported. Throw Exception!
					throw new UnsupportedOperationException("For Collecrtions, only Map<K, V> and List<T> are currently supported.");
				}
				
			} else {
				// if the user has declared a untyped collection, its a scenario we can't handle. Give them an exception!
				throw new IllegalArgumentException("Type " + clazz.getSimpleName() + " is a Collection, but it has no declared generic types. Could not determine the appropriate parser for it.");
			}
			
		} else {
			// we got an ordinary class
			Class<?> clazz =  (Class<?>) type;
			
			if (clazz == Void.class) { // for void types
				result = new VoidParser();
			} else if (clazz.isArray()) { // for arrays
				
				// byte arrays have special parser, as they are sent in Base64 format
				if (clazz.getComponentType() == byte[].class) {
					result = new ByteArrayParser();
				} else { // otherwise, the any other array obeys the same logic, as long as there's a parser for this array component type
					result = new ArrayParser(clazz, getParserByType(clazz.getComponentType())); 
				}
				
			} else if (isPrimitive(clazz)) { 
				result = new PrimitiveParser(clazz);
			} else if (isAnnotatedComplexType(clazz)){
				result = new ComplexTypeParser(clazz);
			} else {
				throw new IllegalArgumentException("Could not determine a propper parser. Perhaps there's an missing @SOAPObjet annotation for the " +  clazz.getSimpleName() + " type.");
			}
		}

		return result;
	}	

	private static boolean isAnnotatedComplexType(Class<?> clazz) {
		try {
			return getClassData(clazz) != null;
		} catch (IllegalArgumentException ex) {
			return false;
		}
	}

	private static boolean isPrimitive(Class<?> clazz) {
		return clazz == String.class 
				|| clazz == boolean.class
				|| clazz == Boolean.class 
				|| clazz == char.class
				|| clazz == Character.class 
				|| clazz == short.class
				|| clazz == Short.class 
				|| clazz == int.class
				|| clazz == Integer.class
				|| clazz == long.class
				|| clazz == Long.class
				|| clazz == float.class
				|| clazz == Float.class
				|| clazz == double.class
				|| clazz == Date.class
				|| clazz == Double.class;
	}

	public static class ClassData {
		private Class<?> clazz;
		private String namespace;
		private String typeId;
		private List<FieldData> fields = new ArrayList<FieldData>();
		
		private ClassData(Class<?> clazz, SOAPObject annotation) {
			this.clazz = clazz;
			this.namespace = annotation.namespace();
			this.typeId = annotation.typeId();
		}

		public Class<?> getClazz() {
			return clazz;
		}

		public String getNamespace() {
			return namespace;
		}
		
		public String getTypeId() {
			return typeId;
		}

		public List<FieldData> getFields() {
			return fields;
		}
	}

	public static class FieldData {
		private final Field field;
		private final Parser parser;
		private final String name;
		
		private FieldData(Field field, SOAPProperty propertyAnnotation) {
			String name = propertyAnnotation.name();
			if ("".equals(name))
				name = field.getName();
			
			this.name = name; 
			this.field = field;
			this.parser = getParserByType(field.getGenericType());
		}

		public Field getField() {
			return field;
		}
		
		public String getName() {
			return name;
		}
		
		public Parser getParser() {
			return parser;
		}
	}
	
	public static class ServiceData {
		private final URL serverUrl;
		private final String namespace;
		private final String serviceName;
		private final String serviceInterface;
		private final HashMap<Method, OperationData> methods = new HashMap<Method, OperationData>();
		
		private ServiceData(SOAPService annotation, URL serviceUrl) throws MalformedURLException {
			
			String serviceName = annotation.serviceName();
			String serviceInterface = annotation.serviceInterface();
			if ("".equals(serviceInterface)) {
				serviceInterface = "I" + serviceName;
			}
			
			if (serviceUrl == null) {
				serviceUrl = new URL(annotation.serverUrl());
			}
			
			this.serverUrl = serviceUrl;
			this.namespace = annotation.namespace();
			this.serviceName = serviceName;			
			this.serviceInterface = serviceInterface; 
		}

		public URL getServerUrl() {
			return serverUrl;
		}
		
		public String getNamespace() {
			return namespace;
		}
		
		public String getServiceName() {
			return serviceName;
		}
		
		public String getServiceInterface() {
			return serviceInterface;
		}
		
		public Collection<OperationData> getMethods()
		{
			return methods.values();
		}

		public OperationData getMethodData(Method method) {
			return methods.get(method);
		}
	}
	
	public static class OperationData {
		private final String name;
		private final Parser parser;
		private final String[] parameterNames;
		
		public OperationData(Method method, SOAPServiceOperation operationAnnotation) {

			Parser parser;
			Class<? extends Parser> parserClass = operationAnnotation.parser();
			if (shallDetectParserByType(parserClass)) {
				parser = getParserByType(method.getGenericReturnType());
			} else {
				parser = createParserInstance(parserClass);
			}

			String name = operationAnnotation.name();
			if ("".equals(name)) {
				name = method.getName();
			} 
			
			this.name = name;
			this.parser = parser;
			this.parameterNames = operationAnnotation.parameterNames();
		}

		public String getName() {
			return name;
		}
		
		public Parser getParser() {
			return parser;
		}
		
		public String[] getParameterNames() {
			return this.parameterNames;
		}
	}
}
