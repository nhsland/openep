package com.marand.thinkmed.medications.ehr.utils;

import java.util.List;
import java.util.stream.Collectors;

import com.marand.ispek.ehr.common.EhrLinkType;
import com.marand.openehr.rm.RmPath;
import com.marand.openehr.util.DataValueUtils;
import com.marand.openehr.util.OpenEhrRefUtils;
import com.marand.thinkmed.medications.ehr.model.composition.EhrComposition;
import com.marand.thinkmed.medications.therapy.util.TherapyIdUtils;
import lombok.NonNull;
import org.openehr.jaxb.rm.DvEhrUri;
import org.openehr.jaxb.rm.Link;

/**
 * @author Nejc Korasa
 */

public final class LinksEhrUtils
{
  private LinksEhrUtils() { }

  public static List<Link> getLinksOfType(final @NonNull EhrComposition composition, final @NonNull EhrLinkType linkType)
  {
    return composition.getLinks()
        .stream()
        .filter(instructionLink -> instructionLink.getType().getValue().equals(linkType.getName()))
        .collect(Collectors.toList());
  }

  public static List<Link> getLinksOfType(final @NonNull List<Link> links, final @NonNull EhrLinkType linkType)
  {
    return links
        .stream()
        .filter(l -> l.getType().getValue().equals(linkType.getName()))
        .collect(Collectors.toList());
  }

  public static String getLinkTarget(final String compositionUid)
  {
    return DataValueUtils.getEhrUri(TherapyIdUtils.getCompositionUidWithoutVersion(compositionUid), (RmPath)null).getValue();
  }

  public static boolean isLinkToComposition(
      final @NonNull String compositionUid,
      final @NonNull Link link,
      final @NonNull EhrLinkType linkType)
  {
    final boolean typeMatches = link.getType().getValue().equals(linkType.getName());
    final boolean targetMatches = link.getTarget().getValue().equals(getLinkTarget(compositionUid));
    return typeMatches && targetMatches;
  }

  public static Link createLink(
      final @NonNull String compositionUid,
      final @NonNull String meaning,
      final @NonNull EhrLinkType linkType)
  {
    final Link link = new Link();
    link.setMeaning(DataValueUtils.getText(meaning));
    link.setType(DataValueUtils.getText(linkType.getName()));
    link.setTarget(DataValueUtils.getEhrUri(TherapyIdUtils.getCompositionUidWithoutVersion(compositionUid), (RmPath)null));
    return link;
  }

  public static String getPreviousLinkName(final @NonNull String linkName)
  {
    final String prefix = linkName.substring(0, 1);
    final Integer linkNumber = Integer.valueOf(linkName.substring(1));
    return prefix + (linkNumber - 1);
  }

  public static String getTargetCompositionIdFromLink(final @NonNull Link link)
  {
    final DvEhrUri target = link.getTarget();
    final OpenEhrRefUtils.EhrUriComponents ehrUri = OpenEhrRefUtils.parseEhrUri(target.getValue());
    return ehrUri.getCompositionId();
  }

  public static String getLinkedCompositionUid(final @NonNull List<Link> links, final @NonNull EhrLinkType ehrLinkType)
  {
    final List<Link> linksOfType = getLinksOfType(links, ehrLinkType);
    return linksOfType.isEmpty()
           ? null
           : TherapyIdUtils.getCompositionUidWithoutVersion(getTargetCompositionIdFromLink(linksOfType.get(0)));
  }
}
