package com.marand.auditing.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.retry.annotation.EnableRetry;

/**
 * @author Primoz Delopst
 */

@Configuration
@ComponentScan(basePackages = "com.marand.auditing")
@EnableRetry
@EnableAspectJAutoProxy
public class AuditingConfiguration
{
}
