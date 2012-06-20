package org.asoap;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import org.asoap.annotation.SOAPObject;
import org.asoap.annotation.SOAPProperty;
import org.asoap.parser.Parser;
import org.asoap.serializable.Wrapper;
import org.asoap.util.DateMarshal;
import org.asoap.util.Strings;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.AttributeContainer;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

public class Caller<T> {
	private static final char URL_SEPARATOR = '/';
	private static final char INTERFACE_PREFIX = 'I';

	private URL serverUrl;
	private String namespace;
	private String method;
	private String service;
	private String serviceInterface;
	private LinkedList<Mapping> mappings;
	private LinkedHashMap<String, Object> params;
	private Parser responseParser;

	/**
	 * Default constructor for this class.
	 */
	public Caller() {
		this.params = new LinkedHashMap<String, Object>();
		this.mappings = new LinkedList<Mapping>();
	}

	/**
	 * Sets the server URL for accessing the remote method.
	 * 
	 * @param serverUrl
	 *            the server URL
	 */
	public Caller<T> setServerUrl(URL serverUrl) {
		this.serverUrl = serverUrl;
		return this;
	}

	/**
	 * Setup which method will be used when invoking the web service.
	 * 
	 * @param methodName
	 *            the method name
	 */
	public Caller<T> setMethod(String methodName) {
		this.method = methodName;
		return this;
	}

	/**
	 * Set service's name. This specifies which service the method will be
	 * invoked on. If not previously specified, also sets the service interface 
	 * name by adding the "I" prefix to the service name. 
	 * 
	 * @param serviceName
	 *            the service name
	 */
	public Caller<T> setService(String serviceName) {
		this.service = serviceName;
		this.serviceInterface = INTERFACE_PREFIX + serviceName;
		return this;
	}

	/**
	 * Set service's interface name. This specifies which service the method will be 
	 * invoked on. If your service has a interface following the IServiceName pattern, 
	 * calling this method is optional.
	 * 
	 * @param serviceInterface
	 *            the service interface name.
	 */
	public Caller<T> setServiceInterface(String serviceInterface) {
		this.serviceInterface = serviceInterface;
		return this;
	}

	/**
	 * Sets the service namespace. If not explicitly specified, the default
	 * value is used.
	 * 
	 * @param namespace
	 *            the service namespace
	 */
	public Caller<T> setNamespace(String namespace) {
		this.namespace = namespace;
		return this;
	}

	/**
	 * Sets parameters for being passed through to the service method. Primitive
	 * and complex types are supported, although complex types must have
	 * implement the KvmSerializeble interface. Complex types must be annotated
	 * by the SOAPObject annotation in order for the method calling be processed
	 * correctly.
	 * 
	 * @param params
	 *            the parameters for being sent
	 * @see SOAPObject
	 * @see SOAPProperty
	 * @see Wrapper
	 */
	public Caller<T> setParams(LinkedHashMap<String, Object> params) {
		// adds the list of parameters
		for (String key : params.keySet()) {
			Object value = params.get(key);
			addParameter(key, value);
		}

		return this;
	}

	/**
	 * Adds a parameter for being passed through to the service method.
	 * Primitive and complex types are supported, although complex types must
	 * have implement the KvmSerializeble interface. This could be achieved
	 * through extending the Wrapper class. Additionally, complex types must
	 * also be annotated by the SOAPObject annotation in order for the method
	 * calling be processed correctly.
	 * 
	 * @param name
	 *            the parameter name
	 * @param value
	 *            the parameter value
	 * @see SOAPObject
	 * @see SOAPProperty
	 * @see Wrapper
	 */
	public Caller<T> addParameter(String name, Object value) {
		this.params.put(name, value);

		Class<?> type = value.getClass();
		SOAPObject soapObjectAnnotation = type.getAnnotation(SOAPObject.class);

		if (soapObjectAnnotation != null) {
			String typeId = soapObjectAnnotation.typeId();
			String namespace = soapObjectAnnotation.namespace();
			addMapping(type, typeId, namespace);
		}

		return this;
	}

	/**
	 * Sets a parser for handling the service response.
	 */
	public Caller<T> setResponseParser(Parser parser) {
		this.responseParser = parser;
		return this;
	}

	/**
	 * Performs the service method call and grabs its response, passing it to
	 * the IMethodResponseParser if it applies.
	 * 
	 * @return the parsed data if a parser was provided, null otherwise.
	 * @throws IllegalStateException
	 *             if the RemoteServiceMethod isn't properly setup
	 * @throws SoapFault
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	@SuppressWarnings("unchecked")
	public T call() throws IllegalStateException, IOException, XmlPullParserException, SoapFault {
		if (!isReadyForCall()) {
			throw new IllegalStateException(
					"Invalid method configuration: make sure you setup all needed data for calling a SoapMethod");
		}

		String soapMethod = null;

		if (!Strings.isEmpty(serviceInterface)) {
			soapMethod = Strings.removeLastBackslashIfExists(namespace) + URL_SEPARATOR + serviceInterface
					+ URL_SEPARATOR + method;
		} else {
			soapMethod = Strings.removeLastBackslashIfExists(namespace) + URL_SEPARATOR + method;
		}

		SoapObject request = new SoapObject(namespace, method);

		if (params != null) {
			for (String key : params.keySet()) {
				Object value = params.get(key);

				PropertyInfo pi = new PropertyInfo();
				pi.name = key;
				pi.setType(value.getClass());

				// if there's a mapping for this class, it is a mapped complex type. 
				// thus, we shall set it directly into the PropertyInfo, instead of  
				// creating a SoapPrimitive object
				Mapping mapping;
				if ((mapping = hasMapping(value.getClass())) != null) {
					pi.setValue(value);
					pi.setNamespace(mapping.namespace);
				} else { // otherwise, we shall consider this a primitive value
					pi.setValue(new SoapPrimitive(namespace, key, value.toString()));
				}

				request.addProperty(pi);
			}
		}

		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		envelope.dotNet = true;
		envelope.setOutputSoapObject(request);

		// Lets serialize Date objects.
		DateMarshal dateMarshal = new DateMarshal();
		dateMarshal.register(envelope);

		for (Mapping mapping : mappings) {
			envelope.addMapping(mapping.namespace, mapping.typeId, mapping.type);
		}

		HttpTransportSE httpTransport = new HttpTransportSE(serverUrl.toString() + URL_SEPARATOR + service);
		httpTransport.call(soapMethod, envelope);
		AttributeContainer response = (AttributeContainer) envelope.getResponse();

		if (responseParser != null)
			return (T) responseParser.parse(response);

		return null;
	}

	private boolean isReadyForCall() {
		return serverUrl != null && namespace != null && service != null && serviceInterface != null;
	}

	private Mapping hasMapping(Class<?> klass) {
		for (Mapping mapping : mappings) {
			if (mapping.type == klass) {
				return mapping;
			}
		}
		return null;
	}

	private Caller<T> addMapping(Class<?> type, String typeId, String namespace) {
		this.mappings.add(new Mapping(type, typeId, namespace));
		return this;
	}

	private static class Mapping {

		private Class<?> type;
		private String typeId;
		private String namespace;

		public Mapping(Class<?> type, String typeId, String namespace) {
			super();
			this.type = type;
			this.typeId = typeId;
			this.namespace = namespace;
		}
	}
}