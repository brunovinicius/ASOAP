package org.asoap.parser;

import org.ksoap2.serialization.AttributeContainer;
import org.ksoap2.serialization.SoapPrimitive;

public class PrimitiveParser implements Parser {

	private Class<?> type;

	public PrimitiveParser(Class<?> type) {
		this.type = type;
	}
	
	public Object parse(AttributeContainer container) {
		Object value = null;
		
		if(container instanceof SoapPrimitive) { // if instance of soap object return null
			String primitive = container.toString();
			
			if (type == String.class) {
				value = primitive;
			} else if (type == Boolean.class || type == boolean.class) {
				value = Boolean.parseBoolean(primitive);
			} else if (type == Character.class || type == char.class) {
				value = primitive.charAt(0);
			} else if (type == Short.class || type == short.class) {
				value = Short.parseShort(primitive);
			} else if (type == Integer.class || type == int.class) {
				value = Integer.parseInt(primitive);
			} else if (type == Long.class || type == long.class) {
				value = Long.parseLong(primitive);
			} else if (type == Float.class || type == float.class) {
				value = Float.parseFloat(primitive);
			} else if (type == Double.class  || type == double.class) {
				value = Double.parseDouble(primitive);
			}
		}
		
		return value;
	}

}
