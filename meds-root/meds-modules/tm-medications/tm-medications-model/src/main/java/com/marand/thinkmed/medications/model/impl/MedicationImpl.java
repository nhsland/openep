package com.marand.thinkmed.medications.model.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.marand.thinkmed.medications.MedicationLevelEnum;
import com.marand.thinkmed.medications.model.impl.core.AbstractCatalogEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.SortNatural;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;


/**
 * @author Mitja Lapajne
 * @author Klavdij Lapajne
 */

@SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")
@Entity
@Table(indexes = {
    @Index(name = "xpMedicationVtm", columnList = "vtm_id"),
    @Index(name = "xpMedicationVmp", columnList = "vmp_id"),
    @Index(name = "xpMedicationAmp", columnList = "amp_id"),
    @Index(name = "xpMedicationVmpp", columnList = "vmpp_id"),
    @Index(name = "xpMedicationAmpp", columnList = "ampp_id"),
    @Index(name = "xpMedicationName", columnList = "name"),
    @Index(name = "xpMedicationCode", columnList = "code"),
    @Index(name = "xpMedicationTf", columnList = "tf_id"),
    @Index(name = "xfMedicationMedBase", columnList = "medication_base_id")})
public class MedicationImpl extends AbstractCatalogEntity
{
  private String vtmId; //Virtual Therapeutic Moiety
  private String vmpId; //Virtual Medicinal Product
  private String ampId; //Actual Medicinal Product
  private String vmppId; //Virtual Medicinal Product Pack
  private String amppId; //Actual Medicinal Product Pack
  private String tfId; //Trade Family
  private MedicationLevelEnum medicationLevel;
  private boolean orderable;
  private String orderableNote;
  private String source;
  private DateTime validFrom;
  private DateTime validTo;
  private boolean inpatient;
  private boolean outpatient;
  private String barcode;

  private MedicationImpl previousMedication;

  private MedicationBaseImpl medicationBase;
  private Set<MedicationCustomGroupMemberImpl> customGroupMembers = new HashSet<>();
  private SortedSet<MedicationVersionImpl> versions = new TreeSet<>();
  private Set<MedicationFormularyOrganization> formularyOrganizations = new HashSet<>();
  private Set<MedicationPropertyLinkImpl> properties = new HashSet<>();
  private Set<MedicationExternalImpl> externals = new HashSet<>();
  private Set<MedicationWarningImpl> warnings = new HashSet<>();

  public String getVtmId()
  {
    return vtmId;
  }

  public void setVtmId(final String vtmId)
  {
    this.vtmId = vtmId;
  }

  public String getVmpId()
  {
    return vmpId;
  }

  public void setVmpId(final String vmpId)
  {
    this.vmpId = vmpId;
  }

  public String getAmpId()
  {
    return ampId;
  }

  public void setAmpId(final String ampId)
  {
    this.ampId = ampId;
  }

  public String getVmppId()
  {
    return vmppId;
  }

  public void setVmppId(final String vmppId)
  {
    this.vmppId = vmppId;
  }

  public String getAmppId()
  {
    return amppId;
  }

  public void setAmppId(final String amppId)
  {
    this.amppId = amppId;
  }

  public String getTfId()
  {
    return tfId;
  }

  public void setTfId(final String tfId)
  {
    this.tfId = tfId;
  }

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  public MedicationLevelEnum getMedicationLevel()
  {
    return medicationLevel;
  }

  public void setMedicationLevel(final MedicationLevelEnum medicationLevel)
  {
    this.medicationLevel = medicationLevel;
  }

  @ManyToOne(targetEntity = MedicationImpl.class, fetch = FetchType.LAZY)
  public MedicationImpl getPreviousMedication()
  {
    return previousMedication;
  }

  public void setPreviousMedication(final MedicationImpl previousMedication)
  {
    this.previousMedication = previousMedication;
  }

  /**
   * Optional. Medication at VTM level does not have medication base
   */
  @ManyToOne(targetEntity = MedicationBaseImpl.class, fetch = FetchType.LAZY)
  public MedicationBaseImpl getMedicationBase()
  {
    return medicationBase;
  }

  public void setMedicationBase(final MedicationBaseImpl medicationBase)
  {
    this.medicationBase = medicationBase;
  }

  @ColumnDefault("1")
  public boolean isOrderable()
  {
    return orderable;
  }

  public void setOrderable(final boolean orderable)
  {
    this.orderable = orderable;
  }

  public String getOrderableNote()
  {
    return orderableNote;
  }

  public void setOrderableNote(final String orderableNote)
  {
    this.orderableNote = orderableNote;
  }

  public String getSource()
  {
    return source;
  }

  public void setSource(final String source)
  {
    this.source = source;
  }

  @Type(type = "com.marand.maf.core.hibernate.type.DateTimeType")
  public DateTime getValidFrom()
  {
    return validFrom;
  }

  public void setValidFrom(final DateTime validFrom)
  {
    this.validFrom = validFrom;
  }

  @Type(type = "com.marand.maf.core.hibernate.type.DateTimeType")
  public DateTime getValidTo()
  {
    return validTo;
  }

  public void setValidTo(final DateTime validTo)
  {
    this.validTo = validTo;
  }

  public boolean isInpatient()
  {
    return inpatient;
  }

  public void setInpatient(final boolean inpatient)
  {
    this.inpatient = inpatient;
  }

  public boolean isOutpatient()
  {
    return outpatient;
  }

  public void setOutpatient(final boolean outpatient)
  {
    this.outpatient = outpatient;
  }

  public String getBarcode()
  {
    return barcode;
  }

  public void setBarcode(final String barcode)
  {
    this.barcode = barcode;
  }

  @OneToMany(targetEntity = MedicationCustomGroupMemberImpl.class, mappedBy = "medication", fetch = FetchType.LAZY)
  public Set<MedicationCustomGroupMemberImpl> getCustomGroupMembers()
  {
    return customGroupMembers;
  }

  public void setCustomGroupMembers(final Set<MedicationCustomGroupMemberImpl> customGroupMembers)
  {
    this.customGroupMembers = customGroupMembers;
  }

  public void addCustomGroupMember(final MedicationCustomGroupMemberImpl customGroupMember)
  {
    customGroupMembers.add(customGroupMember);
    customGroupMember.setMedication(this);
  }

  public void removeCustomGroupMember(final MedicationCustomGroupMemberImpl customGroupMember)
  {
    if (customGroupMembers.remove(customGroupMember))
    {
      customGroupMember.setDeleted(true);
    }
  }

  @OneToMany(targetEntity = MedicationWarningImpl.class, mappedBy = "medication", fetch = FetchType.LAZY)
  public Set<MedicationWarningImpl> getWarnings()
  {
    return warnings;
  }

  public void setWarnings(final Set<MedicationWarningImpl> warnings)
  {
    this.warnings = warnings;
  }

  public void addWarning(final MedicationWarningImpl warning)
  {
    warnings.add(warning);
    warning.setMedication(this);
  }

  @OneToMany(targetEntity = MedicationVersionImpl.class, mappedBy = "medication")
  @SortNatural
  public SortedSet<MedicationVersionImpl> getVersions()
  {
    return versions;
  }

  public void setVersions(final SortedSet<MedicationVersionImpl> versions)
  {
    this.versions = versions;
  }

  public void addVersion(final MedicationVersionImpl version)
  {
    versions.add(version);
    version.setMedication(this);
  }

  @OneToMany(targetEntity = MedicationPropertyLinkImpl.class, mappedBy = "medication", fetch = FetchType.LAZY)
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
    propertyLink.setMedication(this);
  }


  @OneToMany(targetEntity = MedicationExternalImpl.class, mappedBy = "medication", fetch = FetchType.LAZY)
  public Set<MedicationExternalImpl> getExternals()
  {
    return externals;
  }

  public void setExternals(final Set<MedicationExternalImpl> externals)
  {
    this.externals = externals;
  }

  public void addExternal(final MedicationExternalImpl external)
  {
    externals.add(external);
    external.setMedication(this);
  }

  public void removeExternal(final MedicationExternalImpl external)
  {
    if (externals.remove(external))
    {
      external.setDeleted(true);
    }
  }

  @OneToMany(targetEntity = MedicationFormularyOrganization.class, mappedBy = "medication", fetch = FetchType.LAZY)
  public Set<MedicationFormularyOrganization> getFormularyOrganizations()
  {
    return formularyOrganizations;
  }

  public void setFormularyOrganizations(final Set<MedicationFormularyOrganization> formularyOrganizations)
  {
    this.formularyOrganizations = formularyOrganizations;
  }

  public void addFormularyOrganization(final MedicationFormularyOrganization organization)
  {
    formularyOrganizations.add(organization);
    organization.setMedication(this);
  }

  public void removeFormularyOrganization(final MedicationFormularyOrganization organization)
  {
    if (formularyOrganizations.remove(organization))
    {
      organization.setDeleted(true);
    }
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb
        .append("vtmId", vtmId)
        .append("vmpId", vmpId)
        .append("ampId", ampId)
        .append("vmppId", vmppId)
        .append("amppId", amppId)
        .append("tfId", tfId)
        .append("medicationLevel", medicationLevel)
        .append("orderable", orderable)
        .append("orderableNote", orderableNote)
        .append("validFrom", validFrom)
        .append("validTo", validTo)
        .append("medicationBase", medicationBase)
        .append("customGroupMembers", customGroupMembers)
        .append("versions", versions)
        .append("properties", properties)
    ;
  }
}
