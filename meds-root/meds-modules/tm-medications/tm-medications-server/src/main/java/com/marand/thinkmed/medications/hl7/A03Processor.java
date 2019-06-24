package com.marand.thinkmed.medications.hl7;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v251.message.ADT_A03;
import ca.uhn.hl7v2.protocol.ReceivingApplication;
import com.marand.thinkmed.medications.connector.impl.config.FhirProperties;
import com.marand.thinkmed.medications.service.MedicationsService;
import com.marand.thinkmed.request.user.StaticAuth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * @author Mitja Lapajne
 */

@Component
public final class A03Processor implements ReceivingApplication
{
  private MedicationsService medicationsService;
  private FhirProperties fhirProperties;

  @Autowired
  public void setMedicationsService(final MedicationsService medicationsService)
  {
    this.medicationsService = medicationsService;
  }

  @Autowired
  public void setFhirProperties(final FhirProperties fhirProperties)
  {
    this.fhirProperties = fhirProperties;
  }

  @Override
  public Message processMessage(
      final Message message, final Map<String, Object> theMetadata) throws HL7Exception
  {
    try
    {
      final String patientId = getPatientIdentifier((ADT_A03)message);

      SecurityContextHolder.getContext().setAuthentication(new StaticAuth("OPENeP", "OPENeP", "OPENeP"));
      medicationsService.abortAllTherapiesForPatient(patientId, "Discharge");
      return message.generateACK();
    }
    catch (final IOException e)
    {
      throw new HL7Exception(e);
    }
  }

  public String getPatientIdentifier(final ADT_A03 message)
  {
    return Arrays.stream(message.getPID().getPatientIdentifierList())
        .filter(i -> i.getAssigningAuthority().getUniversalID().getValue().equalsIgnoreCase(
            fhirProperties.getPatientIdSystem()))
        .map(i -> i.getIDNumber().getValue())
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException(
            "Patient " + message.getPID().toString() + "has no identifier of type: " + fhirProperties.getPatientIdSystem()));
  }

  @Override
  public boolean canProcess(final Message theMessage)
  {
    return true;
  }
}
