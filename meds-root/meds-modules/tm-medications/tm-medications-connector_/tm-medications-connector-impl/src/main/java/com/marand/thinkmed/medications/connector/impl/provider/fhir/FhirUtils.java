package com.marand.thinkmed.medications.connector.impl.provider.fhir;

import java.util.List;
import java.util.stream.Stream;

import ca.uhn.fhir.model.dstu2.composite.HumanNameDt;
import com.google.common.collect.Lists;
import com.marand.maf.core.Opt;

import static java.util.stream.Collectors.joining;

/**
 * @author Mitja Lapajne
 */
@SuppressWarnings("OverlyBroadCatchBlock")
public class FhirUtils
{
  private FhirUtils()
  {
  }

  public static Opt<String> getNameString(final HumanNameDt name)
  {
    return getNameString(Lists.newArrayList(name));
  }

  public static Opt<String> getNameString(final List<HumanNameDt> names)
  {
    if (names.isEmpty())
    {
      return Opt.none();
    }
    final HumanNameDt nameDt = names.get(0);
    if (nameDt.getText() != null)
    {
      return Opt.of(nameDt.getText());
    }

    final String nameString = Stream.of(getGivenName(nameDt), getFamilyName(nameDt))
        .filter(n -> n != null && !n.isEmpty())
        .collect(joining(" "));

    if (nameString.isEmpty())
    {
      return Opt.none();
    }

    return Opt.of(nameString);
  }

  private static String getGivenName(final HumanNameDt name)
  {
    if (!name.getGiven().isEmpty())
    {
      return name.getGiven().get(0).getValue().trim();
    }
    return null;
  }

  private static String getFamilyName(final HumanNameDt name)
  {
    if (!name.getFamily().isEmpty())
    {
      return name.getFamily().get(0).getValue().trim();
    }
    return null;
  }
}
