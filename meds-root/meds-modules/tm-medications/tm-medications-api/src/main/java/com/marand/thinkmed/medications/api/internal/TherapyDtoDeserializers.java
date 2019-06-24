package com.marand.thinkmed.medications.api.internal;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.marand.maf.core.JsonUtil;
import com.marand.thinkmed.medications.api.internal.dto.ConstantComplexTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.ConstantSimpleTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationOrderFormType;
import com.marand.thinkmed.medications.api.internal.dto.OxygenTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.PrescriptionLocalDetailsDto;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.VariableComplexTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.VariableSimpleTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.eer.EERPrescriptionLocalDetailsDto;
import com.marand.thinkmed.medications.api.internal.dto.eer.EERPrescriptionPackageDto;
import com.marand.thinkmed.medications.api.internal.dto.eer.EERPrescriptionSystemEnum;
import com.marand.thinkmed.medications.api.internal.dto.prescription.PrescriptionPackageDto;

/**
 * @author Mitja Lapajne
 */

public class TherapyDtoDeserializers
{
  private final List<JsonUtil.TypeAdapterPair> typeAdapters = new ArrayList<>();
  public static final TherapyDtoDeserializers INSTANCE = new TherapyDtoDeserializers();

  public TherapyDtoDeserializers()
  {
    typeAdapters.add(new JsonUtil.TypeAdapterPair(TherapyDto.class, new TherapyDtoDeserializer()));
    typeAdapters.add(new JsonUtil.TypeAdapterPair(PrescriptionLocalDetailsDto.class, new PrescriptionLocalDetailsDeserializer()));
    typeAdapters.add(new JsonUtil.TypeAdapterPair(PrescriptionPackageDto.class, new PrescriptionPackageDeserializer()));
  }

  public List<JsonUtil.TypeAdapterPair> getTypeAdapters()
  {
    return typeAdapters;
  }

  private static class TherapyDtoDeserializer implements JsonDeserializer<TherapyDto>
  {
    @Override
    public TherapyDto deserialize(
        final JsonElement json,
        final Type typeOfT,
        final JsonDeserializationContext context)
        throws JsonParseException
    {
      final String medicationOrderFormType =
          context.deserialize(json.getAsJsonObject().get("medicationOrderFormType"), String.class);
      final boolean variable = context.deserialize(json.getAsJsonObject().get("variable"), Boolean.class);

      if (MedicationOrderFormType.SIMPLE_ORDERS.contains(MedicationOrderFormType.valueOf(medicationOrderFormType)))
      {
        if (variable)
        {
          return context.deserialize(json, VariableSimpleTherapyDto.class);
        }
        return context.deserialize(json, ConstantSimpleTherapyDto.class);
      }

      if (medicationOrderFormType.equals(MedicationOrderFormType.COMPLEX.name()))
      {
        if (variable)
        {
          return context.deserialize(json, VariableComplexTherapyDto.class);
        }
        return context.deserialize(json, ConstantComplexTherapyDto.class);
      }

      if (medicationOrderFormType.equals(MedicationOrderFormType.OXYGEN.name()))
      {
        return context.deserialize(json, OxygenTherapyDto.class);
      }

      return null;
    }
  }

  private static class PrescriptionLocalDetailsDeserializer implements JsonDeserializer<PrescriptionLocalDetailsDto>
  {
    @Override
    public PrescriptionLocalDetailsDto deserialize(
        final JsonElement json,
        final Type typeOfT,
        final JsonDeserializationContext context)
        throws JsonParseException
    {
      final String outpatientPrescribingSystemName =
          context.deserialize(json.getAsJsonObject().get("prescriptionSystem"), String.class);

      if (EERPrescriptionSystemEnum.EER.name().equals(outpatientPrescribingSystemName))
      {
        return context.deserialize(json, EERPrescriptionLocalDetailsDto.class);
      }
      throw new IllegalArgumentException(
          "Prescribing system : " + outpatientPrescribingSystemName + " not supported.");
    }
  }

  private static class PrescriptionPackageDeserializer implements JsonDeserializer<PrescriptionPackageDto>
  {
    @Override
    public PrescriptionPackageDto deserialize(
        final JsonElement json,
        final Type typeOfT,
        final JsonDeserializationContext context)
        throws JsonParseException
    {
      return context.deserialize(json, EERPrescriptionPackageDto.class);
    }
  }
}
