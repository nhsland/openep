package com.marand.thinkmed.medications.hl7;

import java.util.HashMap;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v251.datatype.CX;
import ca.uhn.hl7v2.model.v251.message.ACK;
import ca.uhn.hl7v2.model.v251.message.ADT_A03;
import com.marand.thinkmed.medications.connector.impl.config.FhirProperties;
import com.marand.thinkmed.medications.service.MedicationsService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;

/**
 * @author Mitja Lapajne
 */

@RunWith(SpringJUnit4ClassRunner.class)
public class A03ProcessorTest
{
  @InjectMocks
  private A03Processor a03Processor;

  @Mock
  private MedicationsService medicationsService;

  @Mock
  private FhirProperties fhirProperties;

  @Test
  public void testProcessMessage() throws HL7Exception
  {
    final ADT_A03 a03 = new ADT_A03();
    final CX identifier0 = a03.getPID().insertPatientIdentifierList(0);
    identifier0.getIDNumber().setValue("idPAS");
    identifier0.getAssigningAuthority().getUniversalID().setValue("PAS");

    final CX identifier1 = a03.getPID().insertPatientIdentifierList(1);
    identifier1.getIDNumber().setValue("idMRN");
    identifier1.getAssigningAuthority().getUniversalID().setValue("MRN");

    final CX identifier2 = a03.getPID().insertPatientIdentifierList(2);
    identifier2.getIDNumber().setValue("idNHS");
    identifier2.getAssigningAuthority().getUniversalID().setValue("NHS");

    Mockito.when(fhirProperties.getPatientIdSystem()).thenReturn("MRN");

    final Message response = a03Processor.processMessage(a03, new HashMap<>());

    Mockito
        .verify(medicationsService, times(1))
        .abortAllTherapiesForPatient("idMRN", "Discharge");

    assertTrue(response instanceof ACK);
    assertEquals("AA", ((ACK)response).getMSA().getAcknowledgmentCode().getValue());
  }
}