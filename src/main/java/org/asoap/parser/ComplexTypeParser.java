package org.asoap.parser;


import java.lang.reflect.Field;
import java.util.List;

import org.asoap.util.CacheRepository;
import org.asoap.util.Log;
import org.asoap.util.CacheRepository.FieldData;
import org.ksoap2.serialization.AttributeContainer;
import org.ksoap2.serialization.SoapObject;

public class ComplexTypeParser implements Parser {

	Class<?> type;
	
	public ComplexTypeParser(Class<?> type) {
		this.type = type;
	}

	@Override
	public Object parse(AttributeContainer container) {
		Object target = null;
		
		if (container instanceof SoapObject)
		{
			target = createObjectInstance();
			SoapObject soapObject = (SoapObject) container;

			List<FieldData> fields = CacheRepository.getClassData(type).getFields();
			Field field = null;

			try {
				for (FieldData annotatedField : fields) {
					field = annotatedField.getField();
	
					// check if the property is null
					Object propertyValue = soapObject.getProperty(annotatedField.getName());
					if(propertyValue == null) {
						continue;
					}
					
					Object value = annotatedField.getParser().parse((AttributeContainer)propertyValue);
					
					field = annotatedField.getField();
					field.set(target, value);
				}
			} catch (IllegalAccessException e) {
				// Note: this piece of code is probably never reached 
				// as we chance accessibility before setting the field value
				Log.w("parseSoapObjectToAnnotatedDTO", "Field not acessible: "  + target.getClass().getSimpleName() + "." + field.getName());
			}
		}
		
		return target;
	}

	private Object createObjectInstance() {
		try {
			return type.newInstance();
		} catch (Exception e) {
			throw new IllegalArgumentException(type.getSimpleName() + " does not provide public no-args constructor", e);
		}
	}

}
