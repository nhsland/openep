package com.marand.thinkmed.medications;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.marand.maf.core.JsonUtil;
import com.marand.maf.core.Opt;
import com.marand.thinkmed.medications.api.internal.TherapyDtoDeserializers;
import com.marand.thinkmed.medications.dto.administration.AdjustAdministrationSubtype;
import com.marand.thinkmed.medications.dto.administration.AdjustInfusionAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdjustOxygenAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationTypeEnum;
import com.marand.thinkmed.medications.dto.administration.BolusAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.InfusionSetChangeDto;
import com.marand.thinkmed.medications.dto.administration.StartAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.StartAdministrationSubtype;
import com.marand.thinkmed.medications.dto.administration.StartOxygenAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.StopAdministrationDto;
import org.joda.time.DateTime;

public class MedsJsonDeserializer
{
  private final List<JsonUtil.TypeAdapterPair> typeAdapters = new ArrayList<>();
  public static final MedsJsonDeserializer INSTANCE = new MedsJsonDeserializer();

  private MedsJsonDeserializer()
  {
    typeAdapters.addAll(TherapyDtoDeserializers.INSTANCE.getTypeAdapters());
    typeAdapters.add(new JsonUtil.TypeAdapterPair(AdministrationDto.class, new AdministrationDeserializer()));
    typeAdapters.add(new JsonUtil.TypeAdapterPair(DateTime.class, new JsonUtil.DateTimeDeserializer()));
  }

  public List<JsonUtil.TypeAdapterPair> getTypeAdapters()
  {
    return typeAdapters;
  }


  private static class AdministrationDeserializer implements JsonDeserializer<AdministrationDto>
  {
    @Override
    public AdministrationDto deserialize(
        final JsonElement json,
        final Type typeOfT,
        final JsonDeserializationContext context) throws JsonParseException
    {
      final AdministrationTypeEnum administrationTypeEnum = context.deserialize(
          json.getAsJsonObject().get("administrationType"),
          AdministrationTypeEnum.class);

      if (administrationTypeEnum == AdministrationTypeEnum.START)
      {
        return context.deserialize(
            json,
            isOxygenStartAdministration(json, context)
            ? StartOxygenAdministrationDto.class
            : StartAdministrationDto.class);
      }
      if (administrationTypeEnum == AdministrationTypeEnum.BOLUS)
      {
        return context.deserialize(json, BolusAdministrationDto.class);
      }
      if (administrationTypeEnum == AdministrationTypeEnum.ADJUST_INFUSION)
      {
        return context.deserialize(
            json,
            isOxygenAdjustAdministration(json, context)
            ? AdjustOxygenAdministrationDto.class
            : AdjustInfusionAdministrationDto.class);
      }
      if (administrationTypeEnum == AdministrationTypeEnum.STOP)
      {
        return context.deserialize(json, StopAdministrationDto.class);
      }
      if (administrationTypeEnum == AdministrationTypeEnum.INFUSION_SET_CHANGE)
      {
        return context.deserialize(json, InfusionSetChangeDto.class);
      }
      return null;
    }

    private boolean isOxygenAdjustAdministration(final JsonElement json, final JsonDeserializationContext context)
    {
      final JsonElement jsonElement = json.getAsJsonObject().get("adjustAdministrationSubtype");
      return Opt
          .resolve(() -> context.deserialize(jsonElement, AdjustAdministrationSubtype.class))
          .map(o -> o == AdjustAdministrationSubtype.OXYGEN)
          .orElseGet(() -> false);
    }

    private boolean isOxygenStartAdministration(final JsonElement json, final JsonDeserializationContext context)
    {
      final JsonElement jsonElement = json.getAsJsonObject().get("startAdministrationSubtype");
      return Opt
          .resolve(() -> context.deserialize(jsonElement, StartAdministrationSubtype.class))
          .map(o -> o == StartAdministrationSubtype.OXYGEN)
          .orElseGet(() -> false);
    }
  }
}
