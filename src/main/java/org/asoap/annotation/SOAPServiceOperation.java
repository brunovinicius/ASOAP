package org.asoap.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.asoap.parser.NullParser;
import org.asoap.parser.Parser;

@Target (ElementType.METHOD)  
@Retention(RetentionPolicy.RUNTIME)
public @interface SOAPServiceOperation {

	String name() default "";
	Class<? extends Parser> parser() default NullParser.class;
	String[] parameterNames() default {};

}
