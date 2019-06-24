package com.marand.thinkmed.medications.barcode;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Vid Kumse
 */

@RunWith(SpringJUnit4ClassRunner.class)
public class BarcodeScannerUtilsTest
{
  @Test
  public void convertToNumericRepresentationTest()
  {
    final String uuidInput = "78e15b88-a57d-4e59-b9cb-40573f6c0024|Medication instruction";
    final String result = BarcodeScannerUtils.convertToNumericRepresentation(uuidInput);
    assertThat(result).isEqualTo("26303691553252205133895135102829449658519260074711467181668977499186" +
                                     "63703304378333301261485904201509943495500465373416833132087777920964718446");
  }

  @Test
  public void parseFromNumericRepresentation()
  {
    final String numericRepresentation = "26303691553252205133895135102829449658519260074711467181668977499186" +
        "63703304378333301261485904201509943495500465373416833132087777920964718446";
    final String result = BarcodeScannerUtils.parseFromNumericRepresentation(numericRepresentation);
    assertThat(result).isEqualTo("78e15b88-a57d-4e59-b9cb-40573f6c0024|Medication instruction");
  }

  @Test
  public void parseFromNumberRepresentationException()
  {
    final String numericRepresentation = "aaa";
    final String result = BarcodeScannerUtils.parseFromNumericRepresentation(numericRepresentation);
    assertThat(result).isNull();
  }
}
