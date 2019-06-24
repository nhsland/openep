package com.marand.thinkmed.medications.dto;

import com.marand.maf.core.data.object.NamedIdDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
public class DispenseSourceDto extends NamedIdDto
{
  private boolean defaultSource;

  public DispenseSourceDto(final long id, final String name, final boolean defaultSource)
  {
    super(id, name);
    this.defaultSource = defaultSource;
  }

  public boolean isDefaultSource()
  {
    return defaultSource;
  }

  public void setDefaultSource(final boolean defaultSource)
  {
    this.defaultSource = defaultSource;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb.append("defaultSource", defaultSource);
  }
}
