package com.marand.thinkmed.medications.model.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

import com.marand.thinkmed.medications.model.impl.core.AbstractPermanentEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.SortNatural;

/**
 * @author Nejc Korasa
 */

@SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")
@Entity
public class MedicationBaseImpl extends AbstractPermanentEntity
{
  private Set<MedicationIngredientLinkImpl> ingredients = new HashSet<>();
  private Set<MedicationIndicationLinkImpl> indications = new HashSet<>();
  private Set<MedicationRouteLinkImpl> routes = new HashSet<>();
  private Set<MedicationPropertyLinkImpl> properties = new HashSet<>();
  private Set<MedicationWarningImpl> warnings = new HashSet<>();
  private SortedSet<MedicationBaseVersionImpl> versions = new TreeSet<>();
  private Set<MedicationTypeImpl> types = new HashSet<>();

  @OneToMany(targetEntity = MedicationIngredientLinkImpl.class, mappedBy = "medicationBase", fetch = FetchType.LAZY)
  public Set<MedicationIngredientLinkImpl> getIngredients()
  {
    return ingredients;
  }

  public void setIngredients(final Set<MedicationIngredientLinkImpl> ingredients)
  {
    this.ingredients = ingredients;
  }

  public void addIngredientLink(final MedicationIngredientLinkImpl ingredientLink)
  {
    ingredients.add(ingredientLink);
    ingredientLink.setMedicationBase(this);
  }

  @OneToMany(targetEntity = MedicationIndicationLinkImpl.class, mappedBy = "medicationBase", fetch = FetchType.LAZY)
  public Set<MedicationIndicationLinkImpl> getIndications()
  {
    return indications;
  }

  public void setIndications(final Set<MedicationIndicationLinkImpl> indications)
  {
    this.indications = indications;
  }

  public void addIndicationLink(final MedicationIndicationLinkImpl indicationLink)
  {
    indications.add(indicationLink);
    indicationLink.setMedicationBase(this);
  }

  @OneToMany(targetEntity = MedicationRouteLinkImpl.class, mappedBy = "medicationBase", fetch = FetchType.LAZY)
  public Set<MedicationRouteLinkImpl> getRoutes()
  {
    return routes;
  }

  public void setRoutes(final Set<MedicationRouteLinkImpl> routes)
  {
    this.routes = routes;
  }

  public void addRouteLink(final MedicationRouteLinkImpl routeLink)
  {
    routes.add(routeLink);
    routeLink.setMedicationBase(this);
  }

  @OneToMany(targetEntity = MedicationPropertyLinkImpl.class, mappedBy = "medicationBase", fetch = FetchType.LAZY)
  public Set<MedicationPropertyLinkImpl> getProperties()
  {
    return properties;
  }

  public void setProperties(final Set<MedicationPropertyLinkImpl> properties)
  {
    this.properties = properties;
  }

  public void addProperty(final MedicationPropertyLinkImpl propertyLink)
  {
    properties.add(propertyLink);
    propertyLink.setMedicationBase(this);
  }

  @OneToMany(targetEntity = MedicationWarningImpl.class, mappedBy = "medicationBase", fetch = FetchType.LAZY)
  public Set<MedicationWarningImpl> getWarnings()
  {
    return warnings;
  }

  public void setWarnings(final Set<MedicationWarningImpl> warnings)
  {
    this.warnings = warnings;
  }

  @OneToMany(targetEntity = MedicationBaseVersionImpl.class, mappedBy = "medicationBase", fetch = FetchType.LAZY)
  @SortNatural
  public SortedSet<MedicationBaseVersionImpl> getVersions()
  {
    return versions;
  }

  public void setVersions(final SortedSet<MedicationBaseVersionImpl> versions)
  {
    this.versions = versions;
  }

  public void addVersion(final MedicationBaseVersionImpl version)
  {
    versions.add(version);
    version.setMedicationBase(this);
  }

  public void addWarning(final MedicationWarningImpl warning)
  {
    warnings.add(warning);
    warning.setMedicationBase(this);
  }

  @OneToMany(targetEntity = MedicationTypeImpl.class, mappedBy = "medicationBase", fetch = FetchType.LAZY)
  public Set<MedicationTypeImpl> getTypes()
  {
    return types;
  }

  public void setTypes(final Set<MedicationTypeImpl> types)
  {
    this.types = types;
  }

  public void addType(final MedicationTypeImpl type)
  {
    types.add(type);
    type.setMedicationBase(this);
  }

  public void removeType(final MedicationTypeImpl type)
  {
    if (types.remove(type))
    {
      type.setDeleted(true);
    }
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb
        .append("ingredients", ingredients)
        .append("indications", indications)
        .append("routes", routes)
        .append("properties", properties)
        .append("versions", versions)
        .append("types", types)
    ;
  }
}
