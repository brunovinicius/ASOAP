package google.code.asoap.serializable;

import google.code.asoap.util.CacheRepository;
import google.code.asoap.util.CacheRepository.ClassData;
import google.code.asoap.util.CacheRepository.FieldData;

import java.util.Hashtable;
import java.util.List;

import org.ksoap2.serialization.KvmSerializable;
import org.ksoap2.serialization.PropertyInfo;

public class Wrapper implements KvmSerializable {

	private final Object wrappedObject;
	private final ClassData classData;
	private final List<FieldData> fieldsData;
	
	public Wrapper(Object wrappedObject) {
		this.wrappedObject = wrappedObject;
		this.classData = CacheRepository.getClassData(wrappedObject.getClass());
		this.fieldsData = classData.getFields();
	}
	
	public Object getProperty(int index) {
		Object result = null;
		try {
			result = fieldsData.get(index).getField().get(wrappedObject);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public int getPropertyCount() {
		return fieldsData.size();
	}

	@SuppressWarnings("rawtypes")
	public void getPropertyInfo(int index, Hashtable properties, PropertyInfo info) {
		FieldData fieldData = fieldsData.get(index);
		info.type = fieldData.getField().getType();
        info.name = fieldData.getName();
	}

	public void setProperty(int index, Object value) {
	}
}
