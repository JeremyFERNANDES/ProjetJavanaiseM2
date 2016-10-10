package annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

//Is the annotation available at execution time 
@Retention(RetentionPolicy.RUNTIME)

//Annotation associated with a type (Classe, interface)
@Target(ElementType.METHOD)

public @interface Lock {
	public LockType type();
}