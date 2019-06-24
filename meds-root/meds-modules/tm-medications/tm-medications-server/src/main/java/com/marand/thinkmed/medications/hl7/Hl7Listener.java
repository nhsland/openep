package com.marand.thinkmed.medications.hl7;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.app.HL7Service;
import ca.uhn.hl7v2.parser.CanonicalModelClassFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

/**
 * @author Mitja Lapajne
 */

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Configuration
@ConditionalOnProperty(name = "hl7.listener-enabled")
public class Hl7Listener
{
  private static final Logger LOG = LoggerFactory.getLogger(Hl7Listener.class);

  private HapiContext hapiContext;
  private HL7Service hl7Server;

  private A03Processor a03Processor;
  private Hl7Properties hl7Properties;

  @Autowired
  public void setA03Processor(final A03Processor a03Processor)
  {
    this.a03Processor = a03Processor;
  }

  @Autowired
  public void setHl7Properties(final Hl7Properties hl7Properties)
  {
    this.hl7Properties = hl7Properties;
  }

  @SuppressWarnings("unused")
  @EventListener
  public void init(final ContextRefreshedEvent event)
  {
    hapiContext = new DefaultHapiContext();

    //forces message version to 2.5.1, HL7 is backward compatible
    hapiContext.setModelClassFactory(new CanonicalModelClassFactory("2.5.1"));

    hl7Server = hapiContext.newServer(hl7Properties.getPort(), false);
    hl7Server.registerApplication("ADT", "A03", a03Processor);

    try
    {
      hl7Server.startAndWait();
    }
    catch (final InterruptedException e)
    {
      throw new IllegalStateException("Could not initialize Hl7 Listener", e);
    }
    LOG.info("HL7 listener for A03 initialized on port {}", hl7Properties.getPort());
  }
}
