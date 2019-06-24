package com.marand.thinkmed.medications.api.internal.dto;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * User: MihaA
 */

public class DocumentationTherapiesDto extends DataTransferObject
{
  private List<String> therapies = new ArrayList<>();
  private List<String> dischargeTherapies = new ArrayList<>();
  private List<String> admissionTherapies = new ArrayList<>();
  private List<String> taggedTherapiesForPrescription =  new ArrayList<>();

  public DocumentationTherapiesDto(
      final List<String> therapies,
      final List<String> dischargeTherapies,
      final List<String> admissionTherapies,
      final List<String> taggedTherapiesForPrescription)
  {
    this.therapies = therapies;
    this.dischargeTherapies = dischargeTherapies;
    this.admissionTherapies = admissionTherapies;
    this.taggedTherapiesForPrescription = taggedTherapiesForPrescription;
  }

  public DocumentationTherapiesDto()
  {
  }

  public List<String> getTherapies()
  {
    return therapies;
  }

  public List<String> getDischargeTherapies()
  {
    return dischargeTherapies;
  }

  public List<String> getAdmissionTherapies()
  {
    return admissionTherapies;
  }

  public List<String> getTaggedTherapiesForPrescription()
  {
    return taggedTherapiesForPrescription;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("therapies", therapies)
        .append("dischargeTherapies", dischargeTherapies)
        .append("admissionTherapies", admissionTherapies)
        .append("taggedTherapiesForPrescription", taggedTherapiesForPrescription)
        ;
  }
}
