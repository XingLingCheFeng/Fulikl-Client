package missu.epsilon.client.event.impl;

import missu.epsilon.client.event.Priorities;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.RUNTIME)
public @interface EventTarget {
    byte value() default Priorities.MEDIUM;
}
