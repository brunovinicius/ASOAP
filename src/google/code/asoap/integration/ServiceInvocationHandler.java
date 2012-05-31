package google.code.asoap.integration;

import google.code.asoap.Caller;
import google.code.asoap.annotation.SOAPServiceOperation;
import google.code.asoap.util.CacheRepository;
import google.code.asoap.util.CacheRepository.ServiceData;
import google.code.asoap.util.CacheRepository.OperationData;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;

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
