
package com.marand.thinkmed.medications.ehr.model;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkehr.mapping.annotation.EhrMapped;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvParsable;
import org.openehr.jaxb.rm.DvText;

/**
 * @author Mitja Lapajne
 */
public class MedicationOrder
{
  @EhrMapped("name")
  private DvText name;

  @EhrMapped("activities[at0001]/description[at0002]/items[at0070]/value")
  private DvText medicationItem;

  @EhrMapped("activities[at0001]/description[at0002]/items[openEHR-EHR-CLUSTER.medication.v0]")
  private Medication preparationDetails;

  @EhrMapped("activities[at0001]/description[at0002]/items[at0091]/value")
  private List<DvCodedText> route = new ArrayList<>();

  @EhrMapped("activities[at0001]/description[at0002]/items[at0094]/value")
  private DvText administrationMethod;

  @EhrMapped("activities[at0001]/description[at0002]/items[openEHR-EHR-CLUSTER.device.v1]")
  private MedicalDevice administrationDevice;

  @EhrMapped("activities[at0001]/description[at0002]/items[at0009]/value")
  private DvText overallDirectionsDescription;

  @EhrMapped("activities[at0001]/description[at0002]/items[at0047]/value")
  private DvParsable parsableDirections;

  @EhrMapped("activities[at0001]/description[at0002]/items[at0174]/value")
  private List<DvText> dosageJustification = new ArrayList<>();

  @EhrMapped("activities[at0001]/description[at0002]/items[openEHR-EHR-CLUSTER.therapeutic_direction.v1]")
  private List<TherapeuticDirection> structuredDoseAndTimingDirections = new ArrayList<>();

  @EhrMapped("activities[at0001]/description[at0002]/items[at0062]")
  private MedicationSafety medicationSafety;

  @EhrMapped("activities[at0001]/description[at0002]/items[at0044]/value")
  private List<DvText> additionalInstruction = new ArrayList<>();

  @EhrMapped("activities[at0001]/description[at0002]/items[at0018]/value")
  private DvText clinicalIndication;

  @EhrMapped("activities[at0001]/description[at0002]/items[at0113]")
  private OrderDetails orderDetails;

  @EhrMapped("activities[at0001]/description[at0002]/items[openEHR-EHR-CLUSTER.medication_authorisation-sl.v0]")
  private MedicationAuthorisationSlovenia authorisationDirection;

  @EhrMapped("activities[at0001]/description[at0002]/items[at0129]")
  private DispenseDirections dispenseDirections;

  @EhrMapped("activities[at0001]/description[at0002]/items[openEHR-EHR-CLUSTER.Medication_additional_details.v0]")
  private AdditionalDetails additionalDetails;

  @EhrMapped("activities[at0001]/description[at0002]/items[at0167]/value")
  private DvText comment;

  public DvText getName()
  {
    return name;
  }

  public void setName(final DvText name)
  {
    this.name = name;
  }

  public DvText getMedicationItem()
  {
    return medicationItem;
  }

  public void setMedicationItem(final DvText medicationItem)
  {
    this.medicationItem = medicationItem;
  }

  public Medication getPreparationDetails()
  {
    if (preparationDetails == null)
    {
      preparationDetails = new Medication();
    }
    return preparationDetails;
  }

  public void setPreparationDetails(final Medication preparationDetails)
  {
    this.preparationDetails = preparationDetails;
  }

  public List<DvCodedText> getRoute()
  {
    return route;
  }

  public void setRoute(final List<DvCodedText> route)
  {
    this.route = route;
  }

  public DvText getAdministrationMethod()
  {
    return administrationMethod;
  }

  public void setAdministrationMethod(final DvText administrationMethod)
  {
    this.administrationMethod = administrationMethod;
  }

  public MedicalDevice getAdministrationDevice()
  {
    return administrationDevice;
  }

  public void setAdministrationDevice(final MedicalDevice administrationDevice)
  {
    this.administrationDevice = administrationDevice;
  }

  public DvText getOverallDirectionsDescription()
  {
    return overallDirectionsDescription;
  }

  public void setOverallDirectionsDescription(final DvText overallDirectionsDescription)
  {
    this.overallDirectionsDescription = overallDirectionsDescription;
  }

  public DvParsable getParsableDirections()
  {
    return parsableDirections;
  }

  public void setParsableDirections(final DvParsable parsableDirections)
  {
    this.parsableDirections = parsableDirections;
  }

  public List<DvText> getDosageJustification()
  {
    return dosageJustification;
  }

  public void setDosageJustification(final List<DvText> dosageJustification)
  {
    this.dosageJustification = dosageJustification;
  }

  public MedicationAuthorisationSlovenia getAuthorisationDirection()
  {
    return authorisationDirection;
  }

  public void setAuthorisationDirection(final MedicationAuthorisationSlovenia authorisationDirection)
  {
    this.authorisationDirection = authorisationDirection;
  }

  public DispenseDirections getDispenseDirections()
  {
    return dispenseDirections;
  }

  public void setDispenseDirections(final DispenseDirections dispenseDirections)
  {
    this.dispenseDirections = dispenseDirections;
  }

  public List<TherapeuticDirection> getStructuredDoseAndTimingDirections()
  {
    return structuredDoseAndTimingDirections;
  }

  public void setStructuredDoseAndTimingDirections(final List<TherapeuticDirection> structuredDoseAndTimingDirections)
  {
    this.structuredDoseAndTimingDirections = structuredDoseAndTimingDirections;
  }

  public MedicationSafety getMedicationSafety()
  {
    if (medicationSafety == null)
    {
      medicationSafety = new MedicationSafety();
    }
    return medicationSafety;
  }

  public void setMedicationSafety(final MedicationSafety medicationSafety)
  {
    this.medicationSafety = medicationSafety;
  }

  public List<DvText> getAdditionalInstruction()
  {
    return additionalInstruction;
  }

  public void setAdditionalInstruction(final List<DvText> additionalInstruction)
  {
    this.additionalInstruction = additionalInstruction;
  }

  public DvText getClinicalIndication()
  {
    return clinicalIndication;
  }

  public void setClinicalIndication(final DvText clinicalIndication)
  {
    this.clinicalIndication = clinicalIndication;
  }

  public OrderDetails getOrderDetails()
  {
    if (orderDetails == null)
    {
      orderDetails = new OrderDetails();
    }
    return orderDetails;
  }

  public void setOrderDetails(final OrderDetails orderDetails)
  {
    this.orderDetails = orderDetails;
  }

  public AdditionalDetails getAdditionalDetails()
  {
    if (additionalDetails == null)
    {
      additionalDetails = new AdditionalDetails();
    }
    return additionalDetails;
  }

  public void setAdditionalDetails(final AdditionalDetails additionalDetails)
  {
    this.additionalDetails = additionalDetails;
  }

  public DvText getComment()
  {
    return comment;
  }

  public void setComment(final DvText comment)
  {
    this.comment = comment;
  }
}
