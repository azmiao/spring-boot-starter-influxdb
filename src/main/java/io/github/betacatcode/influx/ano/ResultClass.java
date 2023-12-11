package io.github.betacatcode.influx.ano;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
@Documented
public @interface ResultClass {

    boolean enable() default true;

}
