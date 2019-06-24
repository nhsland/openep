package com.marand.thinkmed.medications.therapy.util;

import java.util.UUID;

import com.marand.maf.core.Pair;
import com.marand.openehr.util.OpenEhrRefUtils;
import com.marand.thinkmed.medications.ehr.model.InpatientPrescription;
import lombok.NonNull;
import org.openehr.jaxb.rm.Link;

/**
 * @author Mitja Lapajne
 */
public final class TherapyIdUtils
{
  //therapyId is compositionUid without version | instructionName
  private TherapyIdUtils()
  {
  }

  public static String createTherapyId(final InpatientPrescription prescription)
  {
    return createTherapyId(
        prescription.getUid(),
        prescription.getMedicationOrder().getName().getValue());
  }

  public static String createTherapyId(final String compositionUid, final String instructionName)
  {
    return getCompositionUidWithoutVersion(compositionUid) + '|' + instructionName;
  }

  public static String createTherapyId(final @NonNull String compositionUid)
  {
    return createTherapyId(compositionUid, "Medication order");
  }

  public static Pair<String, String> parseTherapyId(final String therapyId)
  {
    final int delimiterIndex = therapyId.indexOf('|');
    final String compositionUid = therapyId.substring(0, delimiterIndex);
    final String ehrOrderName = therapyId.substring(delimiterIndex + 1, therapyId.length());
    return Pair.of(compositionUid, ehrOrderName);
  }

  public static String extractCompositionUid(final String therapyId)
  {
    final int delimiterIndex = therapyId.indexOf('|');
    return therapyId.substring(0, delimiterIndex);
  }

  public static String extractCompositionUidWithoutVersion(final String therapyId)
  {
    final int delimiterIndex = therapyId.indexOf('|');
    return getCompositionUidWithoutVersion(therapyId.substring(0, delimiterIndex));
  }

  public static String extractEhrOrderName(final String therapyId)
  {
    final int delimiterIndex = therapyId.indexOf('|');
    return therapyId.substring(delimiterIndex + 1, therapyId.length());
  }

  public static String getCompositionUidWithoutVersion(final String uid)
  {
    return hasVersion(uid)
           ? uid.substring(0, uid.indexOf("::"))
           : uid;
  }

  public static String getCompositionUidForPreviousVersion(final String uidWithVersion)
  {
    if (hasVersionNumber(uidWithVersion))
    {
      final Long version = getCompositionVersion(uidWithVersion);
      //noinspection ConstantConditions
      return version == 1 ? uidWithVersion : buildCompositionUid(uidWithVersion, version - 1);
    }
    throw new IllegalArgumentException("composition uid " + uidWithVersion + " has no version number!");
  }

  public static String getCompositionUidForFirstVersion(final String uidWithVersion)
  {
    if (hasVersionNumber(uidWithVersion))
    {
      return buildCompositionUid(uidWithVersion, 1L);
    }
    throw new IllegalArgumentException("composition uid " + uidWithVersion + " has no version number!");
  }

  public static Long getCompositionVersion(final String uid)
  {
    return hasVersion(uid)
           ? Long.valueOf(uid.substring(uid.lastIndexOf("::") + 2, uid.length()))
           : null;
  }

  private static boolean hasVersion(final String uid)
  {
    return uid != null && uid.contains("::");
  }

  private static boolean hasVersionNumber(final String uid)
  {
    if (uid != null)
    {
      final int first = uid.indexOf("::");
      final int last = uid.lastIndexOf("::");
      return first > -1 && first != last;
    }
    return false;
  }

  public static String buildCompositionUid(final String uid, final Long version)
  {
    return uid.substring(0, uid.lastIndexOf("::") + 2) + version;
  }

  public static String getTherapyIdFromLink(final Link link)
  {
    final OpenEhrRefUtils.EhrUriComponents ehrUri = OpenEhrRefUtils.parseEhrUri(link.getTarget().getValue());
    final String compositionId = getCompositionUidWithoutVersion(ehrUri.getCompositionId());
    return createTherapyId(compositionId, "Medication order");
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  public static boolean isValidTherapyId(final String therapyId)
  {
    if (therapyId == null || !therapyId.contains("|"))
    {
      return false;
    }
    try
    {
      final String uid = therapyId.substring(0, therapyId.indexOf('|'));
      UUID.fromString(uid);
    }
    catch (final IllegalArgumentException exception)
    {
      return false;
    }
    return true;
  }
}
