package com.marand.auditing.auditing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.marand.auditing.auditing.AuditableType.WITHOUT_RESULT;

/**
 * @author Primoz Delopst
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Auditable
{
  AuditableType[] value() default { WITHOUT_RESULT };
}