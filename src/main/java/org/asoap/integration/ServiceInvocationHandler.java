package org.asoap.integration;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;

import org.asoap.Caller;
import org.asoap.annotation.SOAPServiceOperation;
import org.asoap.util.CacheRepository;
import org.asoap.util.CacheRepository.OperationData;
import org.asoap.util.CacheRepository.ServiceData;

public final class ServiceInvocationHandler implements InvocationHandler {
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		final ServiceData serviceData = CacheRepository.getServiceInterfaceData(method);
		final OperationData methodData = serviceData.getMethodData(method);
		final String[] parameterNames = methodData.getParameterNames();
		final LinkedHashMap<String, Object> params = new LinkedHashMap<String, Object>();
		if (args != null && args.length != parameterNames.length) {
			throw new IllegalArgumentException(String.format("The parameter names array length is different then invocation method arguments. Please, check parameter names array on [%s] annotation.", SOAPServiceOperation.class.getSimpleName()));
		}
		if (parameterNames.length > 0) {
			for (int i = 0; i < args.length; ++i) {
				params.put(parameterNames[i], args[i]);
			}
		}
		return new Caller<Object>()
			.setParams(params)
			.setServerUrl(serviceData.getServerUrl())
			.setNamespace(serviceData.getNamespace())
			.setService(serviceData.getServiceName())
			.setServiceInterface(serviceData.getServiceInterface())			
			.setMethod(methodData.getName())
			.setResponseParser(methodData.getParser()).call();
	}
	
}
