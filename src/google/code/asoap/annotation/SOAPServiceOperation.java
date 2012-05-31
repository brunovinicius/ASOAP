package google.code.asoap.annotation;

import google.code.asoap.parser.NullParser;
import google.code.asoap.parser.Parser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target (ElementType.METHOD)  
@Retention(RetentionPolicy.RUNTIME)
public @interface SOAPServiceOperation {

	String name() default "";
	Class<? extends Parser> parser() default NullParser.class;
	String[] parameterNames() default {};

}
