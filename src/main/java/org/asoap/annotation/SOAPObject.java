package org.asoap.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target (ElementType.TYPE)  
@Retention (RetentionPolicy.RUNTIME)
/**
 * Annotation for classes that may be sent and/or received through SOAP method calls.
 */
public @interface SOAPObject {
	/**
	 * The SOAP namespace that defines this class. May be omitted if the class
	 * is only received. Attempts to pass classes that have not defined this
	 * value as parameters to a SOAP method call will miserably fail.
	 */
	String namespace() default "";

	/** The SOAP type identification. If omitted class name is used instead. */
	String typeId() default "";
}
