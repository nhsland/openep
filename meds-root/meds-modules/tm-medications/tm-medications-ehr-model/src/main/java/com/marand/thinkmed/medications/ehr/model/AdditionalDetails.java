package com.marand.thinkmed.medications.ehr.model;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkehr.mapping.annotation.EhrMapped;
import org.openehr.jaxb.rm.DataValue;
import org.openehr.jaxb.rm.DvBoolean;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvDateTime;
import org.openehr.jaxb.rm.DvDuration;
import org.openehr.jaxb.rm.DvProportion;
import org.openehr.jaxb.rm.DvText;

/**
 * @author Nejc Korasa
 */

@SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")
public class AdditionalDetails
{
  @EhrMapped("items[at0012]/value")
  private DvProportion maxDosePercentage;

  @EhrMapped("items[at0008]/value")
  private DvCodedText prescriptionType;

  @EhrMapped("items[at0006]/value")
  private DvBoolean doctorsOrder;

  @EhrMapped("items[at0005]/value")
  private DvCodedText selfAdministrationType;

  @EhrMapped("items[at0019]/value")
  private DvDateTime selfAdministrationStart;

  @EhrMapped("items[at0014]/value")
  private DvProportion minTargetSaturation;

  @EhrMapped("items[at0015]/value")
  private DvProportion maxTargetSaturation;

  @EhrMapped("items[at0020]/value")
  private List<DvCodedText> informationSource = new ArrayList<>();

  @EhrMapped("items[at0021]/value")
  private DvCodedText releaseDetailsType;

  @EhrMapped("items[at0024]/value")
  private DvDuration releaseDetailsInterval;

  @EhrMapped("items[at0011]/value")
  private DataValue targetInr;

  @EhrMapped("items[at0001]/value")
  private DvCodedText heparin;

  @EhrMapped("items[at0007]/value")
  private DvBoolean baselineInfusion;

  @EhrMapped("items[at0002]/value")
  private DvText sourcePrescriptionIdentifier;

  @EhrMapped("items[at0003]/value")
  private DvText administrationGroup;

  @EhrMapped("items[at0009]/value")
  private DvText addToDischargeLetter;

  @EhrMapped("items[at0010]/value")
  private DvText administrationType;

  @EhrMapped("items[at0013]")
  private PlannedAdministration plannedAdministration;

  @EhrMapped("items[at0029]/value")
  private DvDateTime pastTherapyStart;

  public DvProportion getMaxDosePercentage()
  {
    return maxDosePercentage;
  }

  public void setMaxDosePercentage(final DvProportion maxDosePercentage)
  {
    this.maxDosePercentage = maxDosePercentage;
  }

  public DvCodedText getPrescriptionType()
  {
    return prescriptionType;
  }

  public void setPrescriptionType(final DvCodedText prescriptionType)
  {
    this.prescriptionType = prescriptionType;
  }

  public DvBoolean getDoctorsOrder()
  {
    return doctorsOrder;
  }

  public void setDoctorsOrder(final DvBoolean doctorsOrder)
  {
    this.doctorsOrder = doctorsOrder;
  }

  public DvCodedText getSelfAdministrationType()
  {
    return selfAdministrationType;
  }

  public void setSelfAdministrationType(final DvCodedText selfAdministrationType)
  {
    this.selfAdministrationType = selfAdministrationType;
  }

  public DvDateTime getSelfAdministrationStart()
  {
    return selfAdministrationStart;
  }

  public void setSelfAdministrationStart(final DvDateTime selfAdministrationStart)
  {
    this.selfAdministrationStart = selfAdministrationStart;
  }

  public DvProportion getMinTargetSaturation()
  {
    return minTargetSaturation;
  }

  public void setMinTargetSaturation(final DvProportion minTargetSaturation)
  {
    this.minTargetSaturation = minTargetSaturation;
  }

  public DvProportion getMaxTargetSaturation()
  {
    return maxTargetSaturation;
  }

  public void setMaxTargetSaturation(final DvProportion maxTargetSaturation)
  {
    this.maxTargetSaturation = maxTargetSaturation;
  }

  public List<DvCodedText> getInformationSource()
  {
    return informationSource;
  }

  public void setInformationSource(final List<DvCodedText> informationSource)
  {
    this.informationSource = informationSource;
  }

  public DvCodedText getReleaseDetailsType()
  {
    return releaseDetailsType;
  }

  public void setReleaseDetailsType(final DvCodedText releaseDetailsType)
  {
    this.releaseDetailsType = releaseDetailsType;
  }

  public DvDuration getReleaseDetailsInterval()
  {
    return releaseDetailsInterval;
  }

  public void setReleaseDetailsInterval(final DvDuration releaseDetailsInterval)
  {
    this.releaseDetailsInterval = releaseDetailsInterval;
  }

  public DataValue getTargetInr()
  {
    return targetInr;
  }

  public void setTargetInr(final DataValue targetInr)
  {
    this.targetInr = targetInr;
  }

  public DvCodedText getHeparin()
  {
    return heparin;
  }

  public void setHeparin(final DvCodedText heparin)
  {
    this.heparin = heparin;
  }

  public DvBoolean getBaselineInfusion()
  {
    return baselineInfusion;
  }

  public void setBaselineInfusion(final DvBoolean baselineInfusion)
  {
    this.baselineInfusion = baselineInfusion;
  }

  public DvText getSourcePrescriptionIdentifier()
  {
    return sourcePrescriptionIdentifier;
  }

  public void setSourcePrescriptionIdentifier(final DvText sourcePrescriptionIdentifier)
  {
    this.sourcePrescriptionIdentifier = sourcePrescriptionIdentifier;
  }

  public DvText getAdministrationGroup()
  {
    return administrationGroup;
  }

  public void setAdministrationGroup(final DvText administrationGroup)
  {
    this.administrationGroup = administrationGroup;
  }

  public DvText getAddToDischargeLetter()
  {
    return addToDischargeLetter;
  }

  public void setAddToDischargeLetter(final DvText addToDischargeLetter)
  {
    this.addToDischargeLetter = addToDischargeLetter;
  }

  public DvText getAdministrationType()
  {
    return administrationType;
  }

  public void setAdministrationType(final DvText administrationType)
  {
    this.administrationType = administrationType;
  }

  public PlannedAdministration getPlannedAdministration()
  {
    if (plannedAdministration == null)
    {
      plannedAdministration = new PlannedAdministration();
    }

    return plannedAdministration;
  }

  public void setPlannedAdministration(final PlannedAdministration plannedAdministration)
  {
    this.plannedAdministration = plannedAdministration;
  }

  public DvDateTime getPastTherapyStart()
  {
    return pastTherapyStart;
  }

  public void setPastTherapyStart(final DvDateTime pastTherapyStart)
  {
    this.pastTherapyStart = pastTherapyStart;
  }
}
