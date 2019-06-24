package com.marand.thinkmed.elmdoc;

import com.marand.maf.core.JsonUtil;
import com.marand.thinkmed.elmdoc.data.AllergenClassDo;
import com.marand.thinkmed.elmdoc.data.DiseaseDo;
import com.marand.thinkmed.elmdoc.data.GenderEnum;
import com.marand.thinkmed.elmdoc.data.OptionsDo;
import com.marand.thinkmed.elmdoc.data.PatientDo;
import com.marand.thinkmed.elmdoc.data.ScreenRequestDo;
import com.marand.thinkmed.elmdoc.data.ScreenableDrugDo;
import com.marand.thinkmed.elmdoc.data.ScreeningSummaryDo;
import com.marand.thinkmed.elmdoc.data.ScreeningTypesEnum;
import com.marand.thinkmed.elmdoc.data.TokenRequestDo;
import com.marand.thinkmed.elmdoc.data.TokenResponseDo;
import com.marand.thinkmed.elmdoc.rest.ElmdocRestService;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */

public class ElmdocTestTool
{
  public  void elmdocTestTool()
  {

    final HttpClientContext context = HttpClientContext.create();
    final ResteasyClient client = new ResteasyClientBuilder().httpEngine(
        new ApacheHttpClient4Engine(
            HttpClientBuilder.create().setConnectionManager(
                new PoolingHttpClientConnectionManager()).build(), context)).build();
    final ResteasyWebTarget target = client.target("https://testint.drugscreening.ru");

    final ElmdocRestService restService = target.proxy(ElmdocRestService.class);

    final String tokenResponseJson = restService.generateToken(JsonUtil.toJson(new TokenRequestDo("demo102", "miprehehet")));
    final TokenResponseDo tokenResponseDo = JsonUtil.fromJson(tokenResponseJson, TokenResponseDo.class);

    screen(restService);
    //getConcepts(restService, tokenResponseDo);
  }

  private static void screen(final ElmdocRestService restService)
  {
    final ScreenRequestDo screening = new ScreenRequestDo();
    final PatientDo patient = new PatientDo();
    patient.setBirthDate(new DateTime(1884, 4, 4, 0, 0).toLocalDate());
    patient.setGender(GenderEnum.Female);
    patient.setWeight(54.0);

    screening.setScreeningTypes(ScreeningTypesEnum.getAllNames());

    screening.setPatient(patient);

    screening.getAllergies().add(new AllergenClassDo("ALGC0029", "Salicylates", true));
    screening.getAllergies().add(new AllergenClassDo("ALGC0025", "Amoxicillin", true));

    screening.getDiseases().add(new DiseaseDo("J03.9", "Acute tonsillitis, unspecified", true));

    screening.getDrugs().add(new ScreenableDrugDo("111","aaa","DD0000801", "aspirin tablets 500mg.", true));
    screening.getDrugs().add(new ScreenableDrugDo("222","fff","DD0009390", "Varfareks 5mg tab.", true));
    screening.getDrugs().add(new ScreenableDrugDo("333","www","DD0000803", "zuu", true));
    //screening.getDrugs().add(new ScreenableDrugDo("DD0009390", "Varfareks 5mg tab."));
    //screening.getDrugs().add(new ScreenableDrugDo("DD999999", "Incorrect preparation"));
    //
    //screening.getDrugs().add(new ScreenableDrugDo("DD0000500", "Hiconcil 250 mg kaps."));


    screening.setOptions(new OptionsDo(true, true));

    String screeningSummaryJSON = JsonUtil.toJson(screening);
    String myString = "{\"ScreeningTypes\":\"DrugDrugInteractions, DrugFoodInteractions, DrugAlcoholInteractions, AllergicReactions, DuplicateTherapy, AgeContraindications, GenderContraindications, LactationContraindications, PregnancyContraindications, DiseaseContraindications, DopingAlerts, GeneticTesting, Dosing\",\"Patient\":{\"BirthDate\":\"1884-04-04\",\"Gender\":\"Female\",\"Weight\":54.0,\"BodySurfaceArea\":null},\"Drugs\":[{\"Type\":\"DispensableDrug\",\"CustomCode\":null,\"CustomName\":null,\"Code\":\"DD0000801\",\"Name\":\"aspirin tablets 500mg.\"},{\"Type\":\"DispensableDrug\",\"CustomCode\":null,\"CustomName\":null,\"Code\":\"DD0000802\",\"Name\":\"Аспирин Кардио табл. 100мг\"},{\"Type\":\"DispensableDrug\",\"CustomCode\":null,\"CustomName\":null,\"Code\":\"DD0000803\",\"Name\":\"Аспирин Кардио табл. 300мг\"},{\"Type\":\"DispensableDrug\",\"CustomCode\":null,\"CustomName\":null,\"Code\":\"DD0009390\",\"Name\":\"Varfareks 5mg tab.\"},{\"Type\":\"DispensableDrug\",\"CustomCode\":null,\"CustomName\":null,\"Code\":\"DD999999\",\"Name\":\"Incorrect preparation\"},{\"Type\":\"DispensableDrug\",\"CustomCode\":null,\"CustomName\":null,\"Code\":\"DD0000500\",\"Name\":\"Hiconcil 250 mg kaps.\"}],\"Allergies\":[{\"Type\":\"ScreenableIngredient\",\"CustomCode\":null,\"CustomName\":null,\"Code\":\"SI002679\",\"Name\":\"Warfarin\"},{\"Type\":\"AllergenClass\",\"CustomCode\":null,\"CustomName\":null,\"Code\":\"ALGC0029\",\"Name\":\"Salicylates\"},{\"Type\":\"AllergenClass\",\"CustomCode\":null,\"CustomName\":null,\"Code\":\"ALGC0025\",\"Name\":\"Amoxicillin\"}],\"Diseases\":[{\"Type\":\"ICD10CM\",\"CustomCode\":null,\"CustomName\":null,\"Code\":\"J03.9\",\"Name\":\"Acute tonsillitis, unspecified\"}],\"Options\":{\"IncludeInsignificantInactiveIngredients\":true,\"IncludeMonographs\":true}}";
    String myString1 = "{\"ScreeningTypes\":\"DrugDrugInteractions, DrugFoodInteractions, DrugAlcoholInteractions, AllergicReactions, DuplicateTherapy, AgeContraindications, GenderContraindications, LactationContraindications, PregnancyContraindications, DiseaseContraindications, DopingAlerts, GeneticTesting, Dosing\",\"Patient\":{\"BirthDate\":\"2005-01-31\",\"Gender\":\"Male\",\"Weight\":60.0,\"BodySurfaceArea\":5.0},\"Drugs\":[{\"Type\":\"DispensableDrug\",\"CustomCode\":\"1037419\",\"CustomName\":\"Амосин капс. 250мг\",\"Code\":\"DD0000492\",\"Name\":\"Амосин капс. 250мг\"},{\"Type\":\"DispensableDrug\",\"CustomCode\":\"1037419\",\"CustomName\":\"Амосин капс. 250мг\",\"Code\":\"DD0000492\",\"Name\":\"Амосин капс. 250мг\"}],\"Allergies\":[],\"Diseases\":[],\"Options\":null}";
    //String JSON = "{\"ScreeningTypes\":\"DrugDrugInteractions, DrugFoodInteractions, DrugAlcoholInteractions, AllergicReactions, DuplicateTherapy, AgeContraindications, GenderContraindications, LactationContraindications, PregnancyContraindications, DiseaseContraindications, DopingAlerts, GeneticTesting, Dosing\",\"Patient\":{\"BirthDate\":\"1984-04-04\",\"Gender\":\"Female\",\"Weight\":54.0,\"BodySurfaceArea\":null},\"Drugs\":[{\"Type\":\"DispensableDrug\",\"CustomCode\":null,\"CustomName\":null,\"Code\":\"DD0000801\",\"Name\":\"aspirin tablets 500mg.\"},{\"Type\":\"DispensableDrug\",\"CustomCode\":null,\"CustomName\":null,\"Code\":\"DD0000802\",\"Name\":\"Аспирин Кардио табл. 100мг\"},{\"Type\":\"DispensableDrug\",\"CustomCode\":null,\"CustomName\":null,\"Code\":\"DD0000803\",\"Name\":\"Аспирин Кардио табл. 300мг\"},{\"Type\":\"DispensableDrug\",\"CustomCode\":null,\"CustomName\":null,\"Code\":\"DD0009390\",\"Name\":\"Varfareks 5mg tab.\"},{\"Type\":\"DispensableDrug\",\"CustomCode\":null,\"CustomName\":null,\"Code\":\"DD999999\",\"Name\":\"Incorrect preparation\"},{\"Type\":\"DispensableDrug\",\"CustomCode\":null,\"CustomName\":null,\"Code\":\"DD0000500\",\"Name\":\"Hiconcil 250 mg kaps.\"}],\"Allergies\":[{\"Type\":\"ScreenableIngredient\",\"CustomCode\":null,\"CustomName\":null,\"Code\":\"SI002679\",\"Name\":\"Warfarin\"},{\"Type\":\"AllergenClass\",\"CustomCode\":null,\"CustomName\":null,\"Code\":\"ALGC0029\",\"Name\":\"Salicylates\"},{\"Type\":\"AllergenClass\",\"CustomCode\":null,\"CustomName\":null,\"Code\":\"ALGC0025\",\"Name\":\"Amoxicillin\"}],\"Diseases\":[{\"Type\":\"ICD10CM\",\"CustomCode\":null,\"CustomName\":null,\"Code\":\"J03.9\",\"Name\":\"Acute tonsillitis, unspecified\"}],\"Options\":{\"IncludeInsignificantInactiveIngredients\":true,\"IncludeMonographs\":true}}";
    final String screeningSummary = restService.screening("2F032g1s2y3q0F3U1114022U0y3a103a", screeningSummaryJSON);
    final ScreeningSummaryDo screeningSummaryDo = JsonUtil.fromJson(screeningSummary, ScreeningSummaryDo.class);
    System.out.println(screeningSummary);
  }

  private static void getConcepts(final ElmdocRestService restService, final TokenResponseDo tokenResponse)
  {
    String result = "";
    String all = "";
    final int pageSize = 100;
     int start = 0;
    while (!result.equals("[]"))
    {
      result = restService.concepts(
          tokenResponse.getToken(),
          "AllergenClass",
          String.valueOf(start),
          String.valueOf(pageSize));
      all += result;
      start += pageSize;
    }
    System.out.println(all);
  }
}
