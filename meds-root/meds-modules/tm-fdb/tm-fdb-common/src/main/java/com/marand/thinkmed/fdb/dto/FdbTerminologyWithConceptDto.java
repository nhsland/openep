package com.marand.thinkmed.fdb.dto;

/**
 * @author Mitja Lapajne
 */
@SuppressWarnings("InstanceVariableNamingConvention")
public class FdbTerminologyWithConceptDto extends FdbTerminologyDto
{
  private final String ConceptType;

  public String getConceptType()
  {
    return ConceptType;
  }

  public FdbTerminologyWithConceptDto(
      final String id,
      final String name,
      final String terminology,
      final String conceptType)
  {
    super(id, name, terminology);
    ConceptType = conceptType;
  }
}
