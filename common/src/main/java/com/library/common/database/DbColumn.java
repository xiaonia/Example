package com.library.common.database;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description:
 * Author: xuqingqi
 * E-mail: xuqingqi01@gmail.com
 * Date: 2017/4/15
 */

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DbColumn {

    int version() default 0;

    String column();

    boolean primary() default false;

    boolean nullable() default true;

    boolean deprecated() default false;
}
