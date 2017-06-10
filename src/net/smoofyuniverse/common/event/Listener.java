package net.smoofyuniverse.common.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Listener {
	
	public Order order() default Order.DEFAULT;
	public boolean ignoreCancelled() default true;
}
