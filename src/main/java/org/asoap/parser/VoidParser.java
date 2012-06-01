package org.asoap.parser;

import org.ksoap2.serialization.AttributeContainer;

public class VoidParser implements Parser {

	@Override
	public Object parse(AttributeContainer container) {
		return null;
	}

}
