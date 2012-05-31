package google.code.asoap.parser;

import org.kobjects.base64.Base64;
import org.ksoap2.serialization.AttributeContainer;
import org.ksoap2.serialization.SoapPrimitive;

public class ByteArrayParser implements Parser {

	@Override
	public Object parse(AttributeContainer container) {
		Object rawValue = null;
		
		if(container instanceof SoapPrimitive) {
			SoapPrimitive soapPrimitive = (SoapPrimitive) container;
			if(soapPrimitive != null) {
				// decode byte[] data from Base64 string
				rawValue = Base64.decode(soapPrimitive.toString());
			}
		}
		
		return rawValue;
	}

}
