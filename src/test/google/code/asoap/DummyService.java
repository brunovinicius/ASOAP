package test.google.code.asoap;

import google.code.asoap.annotation.SOAPService;
import google.code.asoap.annotation.SOAPServiceOperation;

@SOAPService(namespace="http://xomnium.mauell.org", 
			 serverUrl="http://192.168.10.108:8732", 
			 serviceInterface="IWallMasterServiceMobile", 
			 serviceName="mobile/WallMasterService")
public interface DummyService {
	
	@SOAPServiceOperation(name="GetLogicalMachineInfo")
	DummyObject getDummyInfo();
	
}