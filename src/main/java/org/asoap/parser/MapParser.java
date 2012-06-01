package org.asoap.parser;

import java.util.HashMap;
import java.util.Map;

import org.ksoap2.serialization.AttributeContainer;
import org.ksoap2.serialization.SoapObject;

public class MapParser implements Parser {

	private Class<?> type;
	private Parser keyParser;
	private Parser valueParser;

	public MapParser(Class<?> type, Parser keyParser, Parser valueParser)
	{
		this.type = type;
		this.keyParser = keyParser;
		this.valueParser = valueParser;
	}


	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object parse(AttributeContainer container) {
		Map result = null;
		
		if (container instanceof SoapObject)
		{
			result = createMapInstance();
			SoapObject soapObject = (SoapObject) container;

			for (int i = 0; i < soapObject.getPropertyCount(); i++) {
				
				SoapObject property = (SoapObject) soapObject.getProperty(i);
				SoapObject propertyKey = (SoapObject) property.getProperty("Key");
				Object key = keyParser.parse(propertyKey);
	
				SoapObject propertyValue = (SoapObject) property.getProperty("Value");
				Object value = valueParser.parse(propertyValue);
				
				result.put(key, value);
			}
		}
		
		return result;
	}

	@SuppressWarnings("rawtypes")
	private Map createMapInstance() {
		if (type == Map.class) {
			return new HashMap();
		} else {
			try {
				return (Map)type.newInstance();
			} catch (Exception e) {
				throw new RuntimeException("Could not create a instance of " + type.getSimpleName(), e);
			}
		}
	}

}
