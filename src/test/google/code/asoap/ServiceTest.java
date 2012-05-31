package test.google.code.asoap;

import static org.junit.Assert.*;
import google.code.asoap.integration.ServiceFactory;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;

public class ServiceTest {

	private ServiceFactory serviceFactory;
	
	@Before
	public void setUp() throws Exception {
		serviceFactory = ServiceFactory.getInstance();
	}

	@Test
	public void testGetInstance() {
		assertNotNull(serviceFactory);
	}

	@Test
	public void testNewServiceWithoutServerURL() throws MalformedURLException {
		DummyService service = (DummyService) serviceFactory.newService(DummyService.class);
		assertNotNull(service);
	}

	@Test
	public void testNewServiceWithServerURL() throws MalformedURLException {
		DummyService service = (DummyService) serviceFactory.newService(DummyService.class, new URL("http://192.168.10.108:8732"));
		assertNotNull(service);
	}
	
	@Test
	public void testGetDummyInfo() throws MalformedURLException {
		DummyService service = (DummyService) serviceFactory.newService(DummyService.class);
		assertNotNull(service);
		DummyObject dummyObject = service.getDummyInfo();
		assertNotNull(dummyObject);
		assertTrue(dummyObject.getHorizontalFormation() > 0);
		assertTrue(dummyObject.getVerticalFormation() > 0);
		assertTrue(dummyObject.getResolutionWidth() > 0);
		assertTrue(dummyObject.getResolutionHeight() > 0);
	}

}
