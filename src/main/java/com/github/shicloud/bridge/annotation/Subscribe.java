package com.github.shicloud.bridge.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @author sfilo
 *
 */
@Target({ TYPE })
@Retention(RUNTIME)
public @interface Subscribe {
	/**
	 * topic
	 */
	String topic() default "";

	/**
	 * qos
	 */
	int qos() default 0;

	/**
	 * storeType
	 */
	String storeType() default "";

}
