package net.mehvahdjukaar.moonlight.api.misc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Documentation only annotation to place on methods called by events
 */
@Target(ElementType.METHOD)
public @interface EventCalled {
}
