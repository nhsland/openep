package com.marand.thinkmed.fdb;

import com.marand.maf.core.JsonUtil;
import com.marand.thinkmed.fdb.dto.FdbConceptTypeEnum;
import com.marand.thinkmed.fdb.dto.FdbConditionAlertSeverityEnum;
import com.marand.thinkmed.fdb.dto.FdbGenderEnum;
import com.marand.thinkmed.fdb.dto.FdbInteractionsSeverityEnums;
import com.marand.thinkmed.fdb.dto.FdbPatientDto;
import com.marand.thinkmed.fdb.dto.FdbScreeningDto;
import com.marand.thinkmed.fdb.dto.FdbScreeningResultDto;
import com.marand.thinkmed.fdb.dto.FdbTerminologyDto;
import com.marand.thinkmed.fdb.dto.FdbTerminologyEnum;
import com.marand.thinkmed.fdb.dto.FdbTerminologyWithConceptDto;
import com.marand.thinkmed.fdb.rest.FdbRestService;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;

/**
 * @author Mitja Lapajne
 */

public class FdbTestTool
{
  private FdbTestTool()
  {
  }

  public static void main(final String[] args)
  {
    final String BaseURL = "http://medispan.marand.si";
    final FdbRestService service;
    final HttpClientContext context = HttpClientContext.create();

    final ResteasyClient client = new ResteasyClientBuilder().httpEngine(
        new ApacheHttpClient4Engine(
            HttpClientBuilder.create().setConnectionManager(
                new PoolingHttpClientConnectionManager()).build(), context)).build();
    final ResteasyWebTarget target = client.target(BaseURL);
    service = target.proxy(FdbRestService.class);

    final FdbScreeningDto screeningDto = buildTestFdbScreeningDto();
    final String json = JsonUtil.toJson(screeningDto);
    final String warningJson = service.scanForWarnings(
        false,
        FdbConditionAlertSeverityEnum.PRECAUTION.getName(),
        FdbInteractionsSeverityEnums.LOW_RISK.getName(),
        json);
    final FdbScreeningResultDto resultDto = JsonUtil.fromJson(warningJson, FdbScreeningResultDto.class);
    System.out.println(warningJson);
  }

  private static FdbScreeningDto buildTestFdbScreeningDto()
  {
    final FdbScreeningDto screeningDto = new FdbScreeningDto();

    screeningDto.getScreeningModules().add(4);
    screeningDto.getScreeningModules().add(64);
    screeningDto.getScreeningModules().add(16);
    screeningDto.getScreeningModules().add(2);
    screeningDto.getScreeningModules().add(1);
    screeningDto.getScreeningModules().add(32);
    screeningDto.getScreeningModules().add(128);

    final FdbPatientDto patientInformation = new FdbPatientDto();
    patientInformation.setGender(FdbGenderEnum.MALE.getKey());
    patientInformation.setAge(90*365L);
    patientInformation.setConditionListComplete(true);
    screeningDto.setPatientInformation(patientInformation);

    //final FdbDrugDto currentDrug1 = new FdbDrugDto();
    //currentDrug1.setId("2002914");
    //currentDrug1.setName("Salbutamol 100 microgram");
    //currentDrug1.setTerminology(FdbEnums.MDDF_TERMINOLOGY.getNameValue());
    //currentDrug1.setConceptType(FdbEnums.PRODUCT_CONCEPT_TYPE.getNameValue());
    //screeningDto.getCurrentDrugs().add(currentDrug1);
    //
    //final FdbDrugDto currentDrug2 = new FdbDrugDto();
    //currentDrug2.setId("1013679");
    //currentDrug2.setName("Serovent Evohaler");
    //currentDrug2.setTerminology(FdbEnums.MDDF_TERMINOLOGY.getNameValue());
    //currentDrug2.setConceptType(FdbEnums.PRODUCT_CONCEPT_TYPE.getNameValue());
    //screeningDto.getCurrentDrugs().add(currentDrug2);
    //
    //final FdbDrugDto currentDrug3 = new FdbDrugDto();
    //currentDrug3.setId("1014947");
    //currentDrug3.setName("Spiriva 18microgram");
    //currentDrug3.setTerminology(FdbEnums.MDDF_TERMINOLOGY.getNameValue());
    //currentDrug3.setConceptType(FdbEnums.PRODUCT_CONCEPT_TYPE.getNameValue());
    //screeningDto.getCurrentDrugs().add(currentDrug3);
    //
    //final FdbDrugDto currentDrug4 = new FdbDrugDto();
    //currentDrug4.setId("1001191");
    //currentDrug4.setName("Nuelin 250");
    //currentDrug4.setTerminology(FdbEnums.MDDF_TERMINOLOGY.getNameValue());
    //currentDrug4.setConceptType(FdbEnums.PRODUCT_CONCEPT_TYPE.getNameValue());
    //screeningDto.getCurrentDrugs().add(currentDrug4);



    //final FdbDrugDto prospectiveDrug1 = new FdbDrugDto();
    //prospectiveDrug1.setId("38268001");
    //prospectiveDrug1.setName("Ibuprofen");
    //prospectiveDrug1.setTerminology(FdbEnums.SNOMED_TERMINOLOGY.getNameValue());
    //prospectiveDrug1.setConceptType(FdbEnums.DRUG_CONCEPT_TYPE.getNameValue());
    //screeningDto.getProspectiveDrugs().add(prospectiveDrug1);
    //
    //final FdbDrugDto prospectiveDrug2 = new FdbDrugDto();
    //prospectiveDrug2.setId("48603004");
    //prospectiveDrug2.setName("Warfarine");
    //prospectiveDrug2.setTerminology(FdbEnums.SNOMED_TERMINOLOGY.getNameValue());
    //prospectiveDrug2.setConceptType(FdbEnums.DRUG_CONCEPT_TYPE.getNameValue());
    //screeningDto.getProspectiveDrugs().add(prospectiveDrug2);




    //final FdbDrugDto prospectiveDrug1 = new FdbDrugDto();
    //prospectiveDrug1.setId("329708004");
    //prospectiveDrug1.setName("Ibuprofen 800mg tablets");
    //prospectiveDrug1.setTerminology(FdbEnums.SNOMED_TERMINOLOGY.getNameValue());
    //prospectiveDrug1.setConceptType(FdbEnums.PRODUCT_CONCEPT_TYPE.getNameValue());
    //screeningDto.getProspectiveDrugs().add(prospectiveDrug1);
    //
    //final FdbDrugDto prospectiveDrug2 = new FdbDrugDto();
    //prospectiveDrug2.setId("319735007");
    //prospectiveDrug2.setName("Warfarin 5mg tablets");
    //prospectiveDrug2.setTerminology(FdbEnums.SNOMED_TERMINOLOGY.getNameValue());
    //prospectiveDrug2.setConceptType(FdbEnums.PRODUCT_CONCEPT_TYPE.getNameValue());
    //screeningDto.getProspectiveDrugs().add(prospectiveDrug2);

    //screeningDto.getProspectiveDrugs().add(
    //    new FdbTerminologyWithConceptDto(
    //        "96195007", "lamotrigine",
    //        FdbTerminologyEnum.SNOMED.getName(),
    //        FdbConceptTypeEnum.DRUG.getName()));
    //
    //screeningDto.getProspectiveDrugs().add(
    //    new FdbTerminologyWithConceptDto(
    //        "10049011000001109", "sodium valproate",
    //        FdbTerminologyEnum.SNOMED.getName(),
    //        FdbConceptTypeEnum.DRUG.getName()));

    screeningDto.getProspectiveDrugs().add(
        new FdbTerminologyWithConceptDto(
            "15411211000001106", "Metformin",
            FdbTerminologyEnum.SNOMED.getName(),
            FdbConceptTypeEnum.PRODUCT.getName()));

    screeningDto.getConditions().add(
        new FdbTerminologyDto(
            "709044004", "Chronic kidney disease stage",
            FdbTerminologyEnum.SNOMED.getName()));

    //final FdbTerminologyWithConceptDto allergen = new FdbTerminologyWithConceptDto(
    //    "91936005", "Penicillin",
    //    FdbTerminologyEnum.SNOMED.getName(),
    //    FdbConceptTypeEnum.DRUG.getName());
    //screeningDto.getAllergens().add(allergen);



    //final FdbTerminologyDto condition = new FdbTerminologyDto(
    //    "279039007",
    //    "Low back pain",
    //    FdbTerminologyEnum.SNOMED.getName());
    //screeningDto.getConditions().add(condition);

    return screeningDto;
  }
}
