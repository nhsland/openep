package com.marand.thinkmed.medications.ehr.model.pharmacist;

import com.marand.thinkehr.mapping.annotation.EhrMapped;
import org.openehr.jaxb.rm.DvBoolean;
import org.openehr.jaxb.rm.DvEhrUri;
import org.openehr.jaxb.rm.DvText;

/**
 * @author Vid Kumse
 */
public class MedicationItemAssessment
{
  @EhrMapped("items[at0004]/value")
  private DvEhrUri relatedMedicationItem;

  @EhrMapped("items[at0003]/value")
  private DvBoolean noProblemIdentified;

  @EhrMapped("items[at0064]/value")
  private DvText overallRecommendation;

  @EhrMapped("items[at0007]")
  private DrugRelatedProblem drugRelatedProblem;

  @EhrMapped("items[at0029]")
  private PharmacokineticIssue pharmacokineticIssue;

  @EhrMapped("items[at0038]")
  private PatientRelatedProblem patientRelatedProblem;

  public DvEhrUri getRelatedMedicationItem()
  {
    return relatedMedicationItem;
  }

  public void setRelatedMedicationItem(final DvEhrUri relatedMedicationItem)
  {
    this.relatedMedicationItem = relatedMedicationItem;
  }

  public DvBoolean getNoProblemIdentified()
  {
    return noProblemIdentified;
  }

  public void setNoProblemIdentified(final DvBoolean noProblemIdentified)
  {
    this.noProblemIdentified = noProblemIdentified;
  }

  public DvText getOverallRecommendation()
  {
    return overallRecommendation;
  }

  public void setOverallRecommendation(final DvText overallRecommendation)
  {
    this.overallRecommendation = overallRecommendation;
  }

  public DrugRelatedProblem getDrugRelatedProblem()
  {
    return drugRelatedProblem;
  }

  public void setDrugRelatedProblem(final DrugRelatedProblem drugRelatedProblem)
  {
    this.drugRelatedProblem = drugRelatedProblem;
  }

  public PharmacokineticIssue getPharmacokineticIssue()
  {
    return pharmacokineticIssue;
  }

  public void setPharmacokineticIssue(final PharmacokineticIssue pharmacokineticIssue)
  {
    this.pharmacokineticIssue = pharmacokineticIssue;
  }

  public PatientRelatedProblem getPatientRelatedProblem()
  {
    return patientRelatedProblem;
  }

  public void setPatientRelatedProblem(final PatientRelatedProblem patientRelatedProblem)
  {
    this.patientRelatedProblem = patientRelatedProblem;
  }
}
