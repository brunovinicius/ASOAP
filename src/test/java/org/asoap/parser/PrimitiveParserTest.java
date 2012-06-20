package org.asoap.parser;

import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;
import org.kobjects.isodate.IsoDate;
import org.ksoap2.serialization.SoapPrimitive;

public class PrimitiveParserTest {

	@Test
	public void testPrimitiveParserWithDatesAndNoErrors() {
		final Date date = new Date();
		final SoapPrimitive sp = new SoapPrimitive("namespace", "name", IsoDate.dateToString(date, IsoDate.DATE_TIME));
		final PrimitiveParser parser = new PrimitiveParser(Date.class);
		Date result = (Date) parser.parse(sp);
		Assert.assertEquals(result, date);
	}

}
