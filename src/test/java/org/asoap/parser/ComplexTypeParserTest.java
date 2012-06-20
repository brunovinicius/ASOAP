package org.asoap.parser;

import java.util.Date;

import junit.framework.Assert;

import org.asoap.DummyObject;
import org.junit.Test;
import org.kobjects.isodate.IsoDate;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;

public class ComplexTypeParserTest {

	@Test
	public void testComplexTypeWithDates() {
		final SoapObject soapObject = new SoapObject("namespace", "name");
		final ComplexTypeParser parser = new ComplexTypeParser(DummyObject.class);
		final Date date = new Date();

		PropertyInfo info = new PropertyInfo();
		SoapPrimitive soapPrimitive = new SoapPrimitive("namespace", "HorizontalFormation", "1");
		info.namespace = "namespace";
		info.type = int.class;
		info.name = "HorizontalFormation";
		info.setValue(soapPrimitive);
		soapObject.addProperty(info);

		info = new PropertyInfo();
		soapPrimitive = new SoapPrimitive("namespace", "VerticalFormation", "2");
		info.namespace = "namespace";
		info.type = int.class;
		info.name = "VerticalFormation";
		info.setValue(soapPrimitive);
		soapObject.addProperty(info);

		info = new PropertyInfo();
		soapPrimitive = new SoapPrimitive("namespace", "ResolutionHeight", "3");
		info.namespace = "namespace";
		info.type = int.class;
		info.name = "ResolutionHeight";
		info.setValue(soapPrimitive);
		soapObject.addProperty(info);

		info = new PropertyInfo();
		soapPrimitive = new SoapPrimitive("namespace", "ResolutionWidth", "4");
		info.namespace = "namespace";
		info.type = int.class;
		info.name = "ResolutionWidth";
		info.setValue(soapPrimitive);
		soapObject.addProperty(info);

		info = new PropertyInfo();
		soapPrimitive = new SoapPrimitive("namespace", "BirthDate", IsoDate.dateToString(date, IsoDate.DATE_TIME));
		info.namespace = "namespace";
		info.type = Date.class;
		info.name = "BirthDate";
		info.setValue(soapPrimitive);
		soapObject.addProperty(info);

		DummyObject dummyObject = (DummyObject) parser.parse(soapObject);

		Assert.assertEquals(dummyObject.getHorizontalFormation(), 1);
		Assert.assertEquals(dummyObject.getVerticalFormation(), 2);
		Assert.assertEquals(dummyObject.getResolutionHeight(), 3);
		Assert.assertEquals(dummyObject.getResolutionWidth(), 4);
		Assert.assertEquals(dummyObject.getBirthDate(), date);
	}

}
