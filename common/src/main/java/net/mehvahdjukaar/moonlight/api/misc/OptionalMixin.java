package net.mehvahdjukaar.moonlight.api.misc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Only load this mixin if the target class is loaded
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface OptionalMixin {

    String value();

    boolean classLoaded() default true;
}
