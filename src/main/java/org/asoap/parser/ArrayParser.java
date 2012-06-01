package org.asoap.parser;

import java.lang.reflect.Array;

import org.ksoap2.serialization.AttributeContainer;
import org.ksoap2.serialization.SoapObject;

public class ArrayParser implements Parser {

	private Class<?> componentType;
	private Parser componentTypeParser;

	public ArrayParser(Class<?> componentType, Parser parser) {
		this.componentType = componentType;
		this.componentTypeParser = parser;
	}

	@Override
	public Object parse(AttributeContainer container) {
		Object result = null;
		
		if(container instanceof SoapObject) {
			SoapObject propertyValue = (SoapObject) container;
			
			int elementCount = propertyValue.getPropertyCount();
			result = Array.newInstance(componentType, elementCount);
			
			for (int i = 0; i < elementCount; ++i) {
				Array.set(result, i, componentTypeParser.parse((AttributeContainer) propertyValue.getProperty(i)));
			}
		}
		
		return result;
	}

}
