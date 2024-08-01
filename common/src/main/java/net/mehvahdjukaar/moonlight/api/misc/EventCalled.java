package net.mehvahdjukaar.moonlight.api.misc;

import org.jetbrains.annotations.ApiStatus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Documentation only annotation to place on methods called by events
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface EventCalled {
}
