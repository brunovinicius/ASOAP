package org.asoap.parser;

import java.util.ArrayList;
import java.util.List;

import org.ksoap2.serialization.AttributeContainer;
import org.ksoap2.serialization.SoapObject;

public class ListParser implements Parser {

	private Class<?> type;
	private Parser elementsParser;

	public ListParser(Class<?> type, Parser listElementsParser) {
		this.type = type;
		this.elementsParser = listElementsParser;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object parse(AttributeContainer container) {
		List result = null;
		
		if (container instanceof SoapObject)
		{
			result = createLsitInstance();
			SoapObject soapObject = (SoapObject) container;

			for (int i = 0; i < soapObject.getPropertyCount(); i++) {
				// get running application attributes
				SoapObject elementSoap = (SoapObject)soapObject.getProperty(i);
				Object element = elementsParser.parse(elementSoap);
				
				// adds to the list
				result.add(element);
			}
		}
		
		return result;
	}

	@SuppressWarnings("rawtypes")
	private List createLsitInstance() {
		if (type == List.class) {
			return new ArrayList();
		} else {
			try {
				return (List)type.newInstance();
			} catch (Exception e) {
				throw new RuntimeException("Could not create a instance of " + type.getSimpleName(), e);
			}
		}

	}

}
