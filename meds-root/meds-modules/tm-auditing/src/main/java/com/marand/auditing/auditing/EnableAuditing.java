package com.marand.auditing.auditing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.marand.auditing.config.AuditingConfiguration;
import org.springframework.context.annotation.Import;

/**
 * @author Primoz Delopst
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({AuditingConfiguration.class})
public @interface EnableAuditing
{
}
