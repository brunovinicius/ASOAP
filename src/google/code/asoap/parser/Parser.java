package google.code.asoap.parser;

import org.ksoap2.serialization.AttributeContainer;

public interface Parser {

	Object parse(AttributeContainer container);

}
