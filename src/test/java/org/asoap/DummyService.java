package org.asoap;

import org.asoap.annotation.SOAPService;
import org.asoap.annotation.SOAPServiceOperation;

@SOAPService(namespace="http://xomnium.mauell.org", 
			 serverUrl="http://192.168.10.108:8732", 
			 serviceInterface="IWallMasterServiceMobile", 
			 serviceName="mobile/WallMasterService")
public interface DummyService {
	
	@SOAPServiceOperation(name="GetLogicalMachineInfo")
	DummyObject getDummyInfo();
	
}