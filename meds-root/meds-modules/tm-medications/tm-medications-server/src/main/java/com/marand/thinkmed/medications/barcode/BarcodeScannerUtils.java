package com.marand.thinkmed.medications.barcode;

import java.math.BigInteger;
import java.nio.charset.Charset;

import lombok.NonNull;

/**
 * @author Vid Kumse
 */

public class BarcodeScannerUtils
{
  private BarcodeScannerUtils()
  {
  }

  public static String convertToNumericRepresentation(final @NonNull String value)
  {
    final String stringInUtf8 = new String(value.getBytes(), Charset.forName("UTF-8"));
    return new BigInteger(stringInUtf8.getBytes()).toString();
  }

  /**
   * Parses String from numericRepresentation. Usage: barcodes.
   *@return numericRepresentation. In case that numericRepresentation is not of numeric type,
   *  it returns null
   */
  public static String parseFromNumericRepresentation(final @NonNull String numericRepresentation)
  {
    try
    {
      final BigInteger numericRepresentationBigInt = new BigInteger(numericRepresentation);
      return new String(numericRepresentationBigInt.toByteArray());
    }
    catch (final NumberFormatException e)
    {
      return null;
    }
  }
}
