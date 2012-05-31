package google.code.asoap.integration;

import google.code.asoap.util.CacheRepository;

import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Class used to create instance of remote service proxies based on given interfaces.
 * Interfaces passed to its methods SHALL be annotated with @SOAPService and its methods with @SOAPServiceOperation
 * @see SOAPService
 * @see SOAPServiceOperation
 */
public final class ServiceFactory {

	// Singleton Implementation
	private static final ServiceFactory instance = new ServiceFactory();
	private final ServiceInvocationHandler invocationHandler;
	private ServiceFactory() {
		invocationHandler = new ServiceInvocationHandler();
	}
	
	/**
	 * Gets the singleton instance of this factory.
	 * @return the service factory instance.
	 */
	public static ServiceFactory getInstance() {
		return instance;
	}
	
	/**
	 * Creates a new proxy instance of this interface, which can be used to trigger the Web Service operations as a normal method calls.
	 * 
	 * @param serviceInterface the interface which the proxy will be instantiated; the passed interface SHALL be annotated by @SOAPService
	 * @return the serviceInterface proxy instance instance
	 * @throws MalformedURLException If the URL string annotated on the service isn't valid
	 * @throws IllegalArgumentException if any parameter is null or if the serviceInterface is not an actual interface
	 * @see SOAPService
	 * @see SOAPServiceOperation
	 */
	public <T> T newService(Class<T> serviceInterface) throws MalformedURLException {
		// validate parameters; throws appropriate exception if validation fails
		validateInterface(serviceInterface);

		// add service interface to cache for validation of the annotations, URLs, etc
		CacheRepository.addServiceInterface(serviceInterface);
		
		return (T)createProxy(serviceInterface);
	}

	/**
	 * Creates a new proxy instance of this interface, which can be used to trigger the Web Service operations as a normal method calls. 
	 * Use this overloaded method if you have multiple instances of the same service for creating services that point to multiple servers.
	 * 
	 * @param serviceInterface the interface which the proxy will be instantiated; the passed interface SHALL be annotated by @SOAPService
	 * @param serviceUrl the service URL
	 * @return the serviceInterface proxy instance instance
	 * @throws MalformedURLException If the URL string annotated on the service isn't valid
	 * @throws IllegalArgumentException if any parameter is null or if the serviceInterface is not an actual interface
	 * @see SOAPService
	 * @see SOAPServiceOperation
	 */
	public <T> T newService(Class<T> serviceInterface, URL serviceUrl) throws MalformedURLException {
		// validate parameters; throws appropriate exception if validation fails
		validateInterface(serviceInterface);
		validateServiceUrl(serviceUrl);
		
		// add service interface to cache for validation of the annotations, URLs, etc
		CacheRepository.addServiceInterface(serviceInterface, serviceUrl);
		
		return createProxy(serviceInterface);
	}

	@SuppressWarnings("unchecked") // for the cast to (T), which is 100% safe to do at this point
	private <T> T createProxy(Class<T> serviceInterfaceType) {
		return (T)Proxy.newProxyInstance(ServiceFactory.class.getClassLoader(), new Class<?>[] { serviceInterfaceType }, this.invocationHandler);
	}

	private void validateServiceUrl(URL serverUrl) {
		if (serverUrl == null) 
			throw new IllegalArgumentException("The server URL cannot be null.");
	}

	private void validateInterface(Class<?> serviceInterfaceType) {
		if (serviceInterfaceType == null)
			throw new IllegalArgumentException("Service interface cannot be null.");
		
		if (!serviceInterfaceType.isInterface())
			throw new IllegalArgumentException("Proxy service instances are created based on Interfaces only.");
	}
}
