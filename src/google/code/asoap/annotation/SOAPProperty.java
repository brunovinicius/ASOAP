package google.code.asoap.annotation;

import google.code.asoap.parser.NullParser;
import google.code.asoap.parser.Parser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target (ElementType.FIELD)  
@Retention (RetentionPolicy.RUNTIME)  
/**
 * Marker annotation for defining classes that can be deserialized from a SOAPObject
 * @author bruno.silva
 */
public @interface SOAPProperty {
	/**	The SOAP property identifier. */
    String name() default "";

    /** */
    Class<? extends Parser> parser() default NullParser.class;
}  

