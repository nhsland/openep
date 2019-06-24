package com.marand.thinkmed.medispan;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.marand.maf.core.Pair;
import com.marand.maf.core.export.excel.ExcelExportUtils;
import com.marand.maf.core.export.excel.column.ExcelExportColumn;
import com.marand.maf.core.export.excel.column.StringExcelExportColumn;
import com.marand.maf.core.export.excel.preset.DefaultExcelExportCellRenderer;
import com.marand.maf.core.export.excel.preset.ListColumnExcelExportModel;
import jxl.biff.DisplayFormat;
import jxl.format.CellFormat;
import jxl.write.WritableCellFormat;
import medispan.allergicreactions.AllergenClass;
import medispan.allergicreactions.commonallergens.Allergen;
import medispan.allergicreactions.commonallergens.AllergenManager;
import medispan.business.Filter;
import medispan.business.query.QueryManager;
import medispan.business.query.ValuesOperatorExpression;
import medispan.concepts.IPatientDrug;
import medispan.concepts.PackagedDrug;
import medispan.concepts.ingredients.ScreenableIngredient;
import medispan.concepts.namebasedclassification.DrugName;
import medispan.concepts.therapeuticclassification.GenericProduct;
import medispan.duplicatetherapy.DuplicateTherapyAtClass;
import medispan.duplicatetherapy.Result;
import medispan.foundation.TypeInfo;
import medispan.foundation.exceptions.InvalidObjectException;
import medispan.foundation.exceptions.InvalidParameterException;
import medispan.interactions.InteractionClass;
import medispan.screening.PatientDrug;
import medispan.screening.PatientProfile;

/**
 * @author Mitja Lapajne
 */
public class MedispanTestTool
{
  private MedispanTestTool()
  {
  }

  public static void main(final String[] args) throws IOException, InvalidParameterException, InvalidObjectException
  {
    getCommonAllergens();

    //saveGPIsAndDrugNamesToTxt();
    //GetListOfIngredients();

    ////demo data
    //final List<Pair<String, String>> dataList = new ArrayList<>();
    //dataList.add(Pair.of("123", "demoena"));
    //dataList.add(Pair.of("456", "demodva"));
    //dataList.add(Pair.of("789", "demotri"));
    //dataList.add(Pair.of("147", "demostiri"));
    //
    //
    //final AllergenExcelExportModel model = new AllergenExcelExportModel(dataListdataList);
    //ExcelExportUtils.export("demoAllergensExportRendered.xls", model, new AllergenExcelExportCellRenderer());

    //doDuplicateTherapyDrugToDrugScreening("00071-0155-23", "Lipitor", "98-80-00-10-00-01-00");
    //long start = CurrentTime.get().getMillis();
    //List<Long> list = new ArrayList<>();
    //DoDIInteractionScreen("64-10-00-10-00-03-30", "36-99-18-02-70-03-30", "83-20-00-30-20-03-11");
    //DoDIInteractionScreen("83-20-00-30-20-03-11", "64-10-00-10-00-03-30", "36-99-18-02-70-03-30");
    //list.add(CurrentTime.get().getMillis() - start);
    //start = CurrentTime.get().getMillis();

    //DoDIInteractionScreen("01-10-00-20-00-18-10", "34-00-00-16-00-03-20", "35-40-00-05-00-03-03");
    //list.add(CurrentTime.get().getMillis() - start);
    //start = CurrentTime.get().getMillis();
    //DoDIInteractionScreen("64-99-10-02-12-03-20", "64-99-10-02-12-03-20", "64-99-10-02-12-03-20");
    //list.add(CurrentTime.get().getMillis() - start);

    //for (Long aLong : list)
    //{
    //  System.out.println(aLong);
    //}
    //Filter filter = new Filter();
    //
    //try
    //{
    //  final IPatientDrug drugByGpi1 = getDrugByGpi(filter, "64-10-00-10-00-03-30");
    //final IPatientDrug drugByGpi2 = getDrugByGpi(filter, "36-99-18-02-70-03-30");
    //final IPatientDrug drugByGpi3 = getDrugByGpi(filter, "83-20-00-30-20-03-11");
    //final IPatientDrug drugByGpi4 = getDrugByGpi(filter, "27-60-70-60-10-03-20");
    //final IPatientDrug drugByGpi5 = getDrugByGpi(filter, "85-15-80-20-10-03-20");
    //final IPatientDrug drugByGpi6 = getDrugByGpi(filter, "66-10-00-20-00-01-05");
    //final IPatientDrug drugByGpi7 = getDrugByGpi(filter, "21-30-00-50-10-03-20");

    //final IPatientDrug drugByGpi1 = getDrugByGpi(filter, "36-99-18-02-25-03-05");
    //final IPatientDrug drugByGpi2 = getDrugByGpi(filter, "36-99-18-02-25-03-40");
    //final IPatientDrug drugByGpi3 = getDrugByGpi(filter, "34-00-00-16-00-03-20");
    //final IPatientDrug drugByGpi4 = getDrugByGpi(filter, "34-00-00-16-00-03-30");
    //final IPatientDrug drugByGpi5 = getDrugByGpi(filter, "35-40-00-05-00-03-03");
    //final IPatientDrug drugByGpi6 = getDrugByGpi(filter, "35-40-00-05-00-03-20");

    //final PatientDrug patientDrug1 = new PatientDrug(drugByGpi1);
    //patientProfile.getPatientDrugs().add(patientDrug1);
    //final PatientDrug patientDrug2 = new PatientDrug(drugByGpi2);
    //patientProfile.getPatientDrugs().add(patientDrug2);
    //final PatientDrug patientDrug3 = new PatientDrug(drugByGpi3);
    //patientProfile.getPatientDrugs().add(patientDrug3);
    //final PatientDrug patientDrug4 = new PatientDrug(drugByGpi4);
    //patientProfile.getPatientDrugs().add(patientDrug4);
    //final PatientDrug patientDrug5 = new PatientDrug(drugByGpi5);
    //patientProfile.getPatientDrugs().add(patientDrug5);
    //final PatientDrug patientDrug6 = new PatientDrug(drugByGpi6);
    //patientProfile.getPatientDrugs().add(patientDrug6);
    //final PatientDrug patientDrug7 = new PatientDrug(drugByGpi6);
    //patientProfile.getPatientDrugs().add(patientDrug7);
    //}
    //catch (InvalidParameterException e)
    //{
    //  e.printStackTrace();
    //}
    //catch (InvalidObjectException e)
    //{
    //  e.printStackTrace();
    //}
  }

  private static void DoDIInteractionScreen(final String gpi1, final String gpi2, final String gpi3)
  {
    try
    {
      medispan.foundation.collections.List<IPatientDrug> selectedDrugs = new medispan.foundation.collections.List<IPatientDrug>();
      Filter theFilter = new Filter();
      String propertyNameToSearchOn;
      String valueToSearchWith;
      TypeInfo typeInfoForSearchObject;
      TypeInfo typeForPropertyToSearchOn;
      ValuesOperatorExpression<String> propertyValueOperatorExpression;

      //Now, we'll put a penicillin drug onto the profile as a new prescription into the patientDrug collection.
      //  Penicillin G Potassium Injection Solution Reconstituted 1000000 UNIT.   I'll use the GPI: 01100010102120 to identify it.
      //We will use the QueryManager object to load this, using the isEquivalentTo operator to precisely match the GPI
      // First, a PatientDrug object to hold the result of the lookup by GPI
      // We'll search on the GenericProduct class.

      PatientDrug penicillinByGPI = null;
      propertyNameToSearchOn = "GPI";
      valueToSearchWith = gpi1;
      typeInfoForSearchObject = new TypeInfo(GenericProduct.class, null);
      typeForPropertyToSearchOn = new TypeInfo(String.class, null);

      propertyValueOperatorExpression = QueryManager.<String>getProperty(propertyNameToSearchOn, typeForPropertyToSearchOn)
          .isEqualTo(valueToSearchWith);
      List<GenericProduct> newPenicillinsToAdd = QueryManager.<GenericProduct>getSelect(
          theFilter,
          propertyValueOperatorExpression,
          typeInfoForSearchObject);
      penicillinByGPI = new PatientDrug(newPenicillinsToAdd.get(0));
      //and add this to the patient drugs
      selectedDrugs.add(penicillinByGPI);

      //Warfarin
      valueToSearchWith = gpi2;
      propertyValueOperatorExpression = QueryManager.<String>getProperty(propertyNameToSearchOn, typeForPropertyToSearchOn)
          .isEqualTo(valueToSearchWith);
      List<GenericProduct> warfarins = QueryManager.<GenericProduct>getSelect(
          theFilter,
          propertyValueOperatorExpression,
          typeInfoForSearchObject);
      selectedDrugs.add(new PatientDrug(warfarins.get(0)));

      //Cephadyn Oral Tablet 50-650 MG
      valueToSearchWith = gpi3;
      propertyValueOperatorExpression = QueryManager.<String>getProperty(propertyNameToSearchOn, typeForPropertyToSearchOn)
          .isEqualTo(valueToSearchWith);
      List<GenericProduct> cephadyns = QueryManager.<GenericProduct>getSelect(
          theFilter,
          propertyValueOperatorExpression,
          typeInfoForSearchObject);

      //cephadyns.get(0).getDetailedGenericDispensableDrug().getDetailedPackagedDrugs().get(0)
      selectedDrugs.add(new PatientDrug(cephadyns.get(0)));
      //Now, to screen:
      //We'll need a patient profile.  This is a class that holds medically relevant
      //information for a patient:
      PatientProfile patientProfile = new PatientProfile(theFilter);

      ////Set birthdate
      //DateFormat birthdayFromStringFormat = new SimpleDateFormat("dd/mm/yyyy");
      //Date birthdayDate = birthdayFromStringFormat.parse("01/01/1980");
      //patientProfile.setBirthDate(birthdayDate);
      ////set the gender
      //Gender patientGender = Gender.getMale(theFilter);
      //System.out.println("Setup patient stuff: " + (CurrentTime.get().getMillis() - start) + "ms");
      //start = CurrentTime.get().getMillis();
      //The drugs and allergies should be put in their appropriate locations:
      //patientProfile.getPatientDrugs().addAll(selectedDrugs);

      //Drug to Drug interactions:
      // As an example: set minimumManagement level filter.  For a list of what these filters represent, please ref. the documentation.
      //medispan.interactions.FilterManager.setMinimumManagementLevel(theFilter,medispan.interactions.ManagementLevel.getPotentialInteractionRisk(theFilter)); // normally this value
      //and set the severity level
      //medispan.interactions.FilterManager.setIncludeMajorSeverityLevels(theFilter, true);
      //
      //medispan.interactions.Result drugToDrugInteractionResult = medispan.interactions.DrugDrugResult.getForPatientProfile(theFilter, patientProfile);
      medispan.interactions.Result drugToDrugInteractionResult = medispan.interactions.DrugDrugResult.getForDrugs(
          theFilter,
          selectedDrugs);

      for (medispan.interactions.Interaction theInteraction : drugToDrugInteractionResult.getInteractions())
      {
        System.out.println("Severity:  " + theInteraction.getDefinition().getSeverityLevel().getName());
        System.out.println(theInteraction.getMessage());
        for (InteractionClass ic : theInteraction.getDefinition().getInteractionClasses())
        {
          for (ScreenableIngredient sc : ic.getDefinitelyInteractingIngredients())
          {
            System.out.println(sc.getIdentifier() + ": " + sc.getDescription());
          }
        }
        //These can be retrieved as XML documents too!
        //System.out.println(medispan.documents.HTMLManager.getAsHTML(theInteraction.getPatientMonograph()));

      }
    }
    catch (Exception ex)
    {
      System.err.println(ex.getMessage());
    }
  }

  private static void doDuplicateTherapyDrugToDrugScreening(
      final String firstDrugNdc,
      final String secondDrugName,
      final String thirdDrugGpi)
  {
    try
    {
      //Country.setDefaultISO3166("si");
      final Filter theFilter = new Filter();

      final medispan.foundation.collections.List<IPatientDrug> drugs = new medispan.foundation.collections.List<>();

      final TypeInfo typeInfoForStringPropertyToSearchOn = new TypeInfo(String.class, null);
      final TypeInfo typeInfoForSearchForDrugName = new TypeInfo(DrugName.class, null);
      final TypeInfo typeInfoForSearchForPackagedDrug = new TypeInfo(PackagedDrug.class, null);
      final TypeInfo typeInfoForSearchForGenericProduct = new TypeInfo(GenericProduct.class, null);

      //first drug by NDC
      final ValuesOperatorExpression<String> propertyValueOperatorExpressionForNDCMatch =
          QueryManager.<String>getProperty(
              PackagedDrug.NDC_PROPERTY_MEDISPAN_ID, typeInfoForStringPropertyToSearchOn).isEqualTo(firstDrugNdc);
      final PackagedDrug drug1 =
          QueryManager.<PackagedDrug>getSelect(
              theFilter, propertyValueOperatorExpressionForNDCMatch, typeInfoForSearchForPackagedDrug).get(0);

      //second drug by name
      final DrugName drug2 =
          QueryManager.<DrugName>getSelectForNameEqualTo(theFilter, secondDrugName, typeInfoForSearchForDrugName).get(0);

      //third drug by GPI
      final ValuesOperatorExpression<String> propertyValueOperatorExpressionForGPIMatch =
          QueryManager.<String>getProperty(
              GenericProduct.GPI_PROPERTY_MEDISPAN_ID,
              typeInfoForStringPropertyToSearchOn).contains(thirdDrugGpi);
      final GenericProduct drug3 = QueryManager.<GenericProduct>getSelect(
          theFilter,
          propertyValueOperatorExpressionForGPIMatch,
          typeInfoForSearchForGenericProduct).get(0);

      //drugs.add(drug1);
      //drugs.add(drug2);
      drugs.add(drug3);
      final Result result = Result.getForDrugs(theFilter, drugs);
      final DuplicateTherapyAtClass duplicateTherapyAtClass = (DuplicateTherapyAtClass)result.getDuplicateTherapies().get(0);
      System.out.println("Result: " + result.getMessage());

      System.out.println(
          "Duplication Allowance:" + duplicateTherapyAtClass.getDuplicateTherapyClass().getDuplicationAllowance());
      System.out.println(
          "Has Abuse or Dependency Potential:" +
              duplicateTherapyAtClass.getDuplicateTherapyClass().getHasAbuseOrDependencyPotential());
    }

    catch (InvalidObjectException ex)
    {
      System.out.println(ex.getMessage());
    }
    catch (InvalidParameterException ex)
    {
      System.out.println(ex.getMessage());
    }
  }

  /*** get allergens ***/

  private static void getCommonAllergens()
  {
    try
    {
      final Filter theFilter = new Filter();
      medispan.foundation.collections.List<Allergen> allergens = AllergenManager.getAll(
          theFilter,
          theFilter.getDefaultLocale());

      for (Allergen allergen : allergens)
      {
        System.out.println(
            allergen.getId() + ",  " +
                allergen.getName() + ", " +
                allergen.getConcept().getConceptType().getMediSpanId() + ", " +
                allergen.getConcept().getConceptType().getName() + ", " +
                allergen.getAllergenConcept().getIdentifier() + ", " +
                allergen.getAllergenConcept().getName());
      }
    }
    catch (InvalidObjectException ex)
    {
      System.out.println(ex);
    }
  }

  private static void getListOfAllergyClasses() throws InvalidParameterException
  {
    try
    {
      Filter theFilter = new Filter();
      //Now, Java generics do a process called TypeErasure.
      TypeInfo typeForPropertyToSearchOn = new TypeInfo(String.class, null);
      TypeInfo typeInfoForSearchObject = new TypeInfo(AllergenClass.class, null);
      List<AllergenClass> allergenClasses = QueryManager.<AllergenClass>getSelect(theFilter, typeInfoForSearchObject);
      System.out.println(allergenClasses.size() + " Allergy Classes found at the time of compilation");
      for (AllergenClass allergenClass : allergenClasses)
      {

        // For brevity, this line has been commented out. It will display all allergy classes once uncommented.
        System.out.println("allergenClass.getName: " + allergenClass.getName());
        System.out.println("allergenClass.getMediSpanId()" + allergenClass.getMediSpanId());
        System.out.println("allergenClass.allergenClass.getIdentifier()" + allergenClass.getIdentifier());
        System.out.println("allergenClass.allergenClass.getId()" + allergenClass.getId());
      }
    }
    catch (InvalidObjectException ex)
    {
      // Logger.getLogger(MSC.class.getName()).log(Level.SEVERE, null, ex);
    }
  }


  /*** export to excel ***/

  private static class AllergenExcelExportCellRenderer extends DefaultExcelExportCellRenderer
  {
    @Override
    protected WritableCellFormat createValueFormat(final DisplayFormat displayFormat)
    {
      return new WritableCellFormat(ExcelExportUtils.REGULAR_FONT);
    }

    @Override
    public CellFormat getColumnHeaderFormat(final int sheet, final int headerRow, final int column)
    {
      return new WritableCellFormat(ExcelExportUtils.BOLD_FONT);
    }
  }


  private static class AllergenExcelExportModel extends ListColumnExcelExportModel<Pair<String, String>>
  {
    private static final List<ExcelExportColumn<?>> columns = createColumns();

    private AllergenExcelExportModel(final List<Pair<String, String>> reportData)
    {
      super(columns, reportData);
    }

    @Override
    public int getColumnHeaderRowCount(final int sheet)
    {
      return 1;
    }

    @Override
    public String getColumnHeaderLabel(final int sheet, final int headerRow, final int column)
    {
      return getColumn(column).getHeaderOrKey();
    }
  }

  private abstract static class ValueProvider<V>
      extends ListColumnExcelExportModel.ValueProvider<Pair<String, String>, V>
  {
  }

  private static List<ExcelExportColumn<?>> createColumns()
  {
    final List<ExcelExportColumn<?>> columns = new ArrayList<>();

    columns.add(
        new StringExcelExportColumn(
            "id",
            new ValueProvider<String>()
            {
              @Override
              protected String getValue(final Pair<String, String> data)
              {
                return data.getFirst();
              }
            }));
    columns.add(
        new StringExcelExportColumn(
            "name",
            new ValueProvider<String>()
            {
              @Override
              protected String getValue(final Pair<String, String> data)
              {
                return data.getSecond();
              }
            }));
    return columns;
  }

  private static void saveGPIsAndDrugNamesToExcel() throws InvalidParameterException, InvalidObjectException
  {
    final Filter theFilter = new Filter();
    final List<GenericProduct> genericProducts = QueryManager.getSelect(theFilter, new TypeInfo(GenericProduct.class, null));
    final List<Pair<String, String>> gpiNamePairs = new ArrayList<>();

    for (final GenericProduct genericProduct : genericProducts)
    {
      gpiNamePairs.add(Pair.of(genericProduct.getGPI(), genericProduct.getDescription()));
    }
    final AllergenExcelExportModel model = new AllergenExcelExportModel(gpiNamePairs);
    ExcelExportUtils.export("DrugGPIs.xls", model, new AllergenExcelExportCellRenderer());
  }

  private static void saveGPIsAndDrugNamesToTxt() throws InvalidParameterException, IOException, InvalidObjectException
  {
    final Filter theFilter = new Filter();
    final List<GenericProduct> genericProducts = QueryManager.getSelect(theFilter, new TypeInfo(GenericProduct.class, null));
    final FileWriter fw = new FileWriter("DrugGPIs.txt");

    for (final GenericProduct genericProduct : genericProducts)
    {
      fw.write(genericProduct.getGPI() + " " + genericProduct.getDescription() + "\n");
    }
    fw.close();
  }
}
