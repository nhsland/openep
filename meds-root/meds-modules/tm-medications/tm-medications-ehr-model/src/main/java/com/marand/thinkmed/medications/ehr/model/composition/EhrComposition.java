
package com.marand.thinkmed.medications.ehr.model.composition;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkehr.mapping.annotation.EhrMapped;
import com.marand.thinkmed.medications.ehr.model.Composer;
import org.openehr.jaxb.rm.Link;

/**
 * @author Mitja Lapajne
 */

@SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")
public abstract class EhrComposition
{
  @EhrMapped("uid/value")
  private String uid;

  @EhrMapped("context")
  private Context context;

  @EhrMapped("composer")
  private Composer composer;

  @EhrMapped("links")
  private List<Link> links = new ArrayList<>();

  public abstract String getTemplateId();

  public Context getContext()
  {
    if (context == null)
    {
      context = new Context();
    }

    return context;
  }

  public void setContext(final Context context)
  {
    this.context = context;
  }

  public String getUid()
  {
    return uid;
  }

  public void setUid(final String uid)
  {
    this.uid = uid;
  }

  public Composer getComposer()
  {
    return composer;
  }

  public void setComposer(final Composer composer)
  {
    this.composer = composer;
  }

  public List<Link> getLinks()
  {
    return links;
  }

  public void setLinks(final List<Link> links)
  {
    this.links = links;
  }
}
