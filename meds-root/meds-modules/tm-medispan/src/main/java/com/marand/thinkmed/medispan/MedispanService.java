package com.marand.thinkmed.medispan;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import com.marand.maf.core.exception.SystemException;
import com.marand.thinkmed.api.demographics.data.Gender;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.config.MedsProperties;
import com.marand.thinkmed.medications.connector.data.object.ehr.IdNameDto;
import com.marand.thinkmed.medications.service.WarningSeverity;
import com.marand.thinkmed.medications.service.WarningType;
import com.marand.thinkmed.medications.service.dto.MedicationsWarningDto;
import com.marand.thinkmed.medications.service.dto.WarningScreenMedicationDto;
import com.marand.thinkmed.medications.warnings.WarningsPlugin;
import com.marand.thinkmed.medispan.config.MedispanProperties;
import lombok.NonNull;
import medispan.allergicreactions.AllergenClass;
import medispan.allergicreactions.AllergicReaction;
import medispan.allergicreactions.AllergicReactionAtClass;
import medispan.allergicreactions.AllergicReactionAtIngredient;
import medispan.business.Concept;
import medispan.business.ConceptWithMediSpanId;
import medispan.business.Filter;
import medispan.business.query.QueryManager;
import medispan.business.query.ValuesOperatorExpression;
import medispan.concepts.IPatientDrug;
import medispan.concepts.ingredients.ScreenableIngredient;
import medispan.concepts.therapeuticclassification.GenericProduct;
import medispan.conditions.Condition;
import medispan.diseasecontraindications.Contraindication;
import medispan.diseasecontraindications.FilterManager;
import medispan.diseasecontraindications.SeverityLevel;
import medispan.documents.HTMLManager;
import medispan.documents.IXMLContent;
import medispan.duplicatetherapy.DuplicateTherapy;
import medispan.duplicatetherapy.Result;
import medispan.foundation.TypeInfo;
import medispan.foundation.caching.CacheManager;
import medispan.foundation.exceptions.InvalidObjectException;
import medispan.foundation.exceptions.InvalidParameterException;
import medispan.icd10s.ICD10CM;
import medispan.interactions.DrugDrugResult;
import medispan.interactions.Interaction;
import medispan.screening.PatientAllergy;
import medispan.screening.PatientCondition;
import medispan.screening.PatientDrug;
import medispan.screening.PatientProfile;
import org.joda.time.DateTime;
import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Component;

import static com.marand.thinkmed.medications.service.WarningSeverity.HIGH_OVERRIDE;

/**
 * @author Mitja Lapajne
 */
@Secured("ROLE_User")
@Component
public class MedispanService implements WarningsPlugin
{
  private MedispanProperties medispanProperties;
  private MedsProperties medsProperties;

  @Autowired
  public void setMedispanProperties(final MedispanProperties medispanProperties)
  {
    this.medispanProperties = medispanProperties;
  }

  @Autowired
  public void setMedsProperties(final MedsProperties medsProperties)
  {
    this.medsProperties = medsProperties;
  }

  private static final Logger LOG = LoggerFactory.getLogger(MedispanService.class);
  private final Filter filter = new Filter();

  @PostConstruct
  public void init()
  {
    if (medispanProperties.isPreloadCache())
    {
      new Thread(
          () -> {
            try
            {
              loadCache();
            }
            catch (Exception e)
            {
              LOG.error("Failed loading Medispan cache: " + e.getMessage(), e);
            }
          }).start();
    }
  }

  @Override
  public void reloadCache()
  {
    try
    {
      CacheManager.clear();
      loadCache();
    }
    catch (InvalidObjectException | InvalidParameterException e)
    {
      throw new SystemException("Failed loading Medispan cache: " + e.getMessage(), e);
    }
  }

  private void loadCache() throws InvalidObjectException, InvalidParameterException
  {
    final PatientProfile patientProfile = new PatientProfile(filter);

    final IPatientDrug drugByGpi1 = getDrugByGpi(filter, "21-30-00-50-10-03-20");
    final IPatientDrug drugByGpi2 = getDrugByGpi(filter, "66-10-00-20-00-01-05");
    final IPatientDrug drugByGpi3 = getDrugByGpi(filter, "85-15-80-20-10-03-20");

    if (drugByGpi1 != null)
    {
      final PatientDrug patientDrug1 = new PatientDrug(drugByGpi1);
      patientProfile.getPatientDrugs().add(patientDrug1);
    }
    if (drugByGpi2 != null)
    {
      final PatientDrug patientDrug2 = new PatientDrug(drugByGpi2);
      patientProfile.getPatientDrugs().add(patientDrug2);
    }
    if (drugByGpi3 != null)
    {
      final PatientDrug patientDrug3 = new PatientDrug(drugByGpi3);
      patientProfile.getPatientDrugs().add(patientDrug3);
    }
    final Concept medispanAllergy = QueryManager.getSelectForIdEqualTo(
        filter,
        Integer.parseInt("747645"),     //allergy to penicillin id
        new TypeInfo(AllergenClass.class, null));
    if (medispanAllergy != null)
    {
      final PatientAllergy patientAllergy = new PatientAllergy((AllergenClass)medispanAllergy);
      patientAllergy.setScreen(true);      //we screen medications against known (old) allergies
      patientProfile.getPatientAllergies().add(patientAllergy);
    }
    if (!patientProfile.getPatientDrugs().isEmpty())
    {
      DrugDrugResult.getForPatientProfile(filter, patientProfile);  //interactions
      Result.getForPatientProfile(filter, patientProfile);          //duplicateTherapy
      medispan.allergicreactions.Result.getForPatientProfile(filter, patientProfile);   //allergicreactions
      medispan.diseasecontraindications.Result.getForPatientProfile(filter, patientProfile);  //diseasecontraindications
    }
  }

  @Override
  public List<MedicationsWarningDto> findMedicationWarnings(
      @NonNull final DateTime dateOfBirth,
      final Double patientWeightInKg,
      final Double bsaInM2,
      @NonNull final Gender gender,
      @NonNull final List<IdNameDto> diseaseTypeValues,
      @NonNull final List<IdNameDto> allergiesExternalValues,
      @NonNull final List<WarningScreenMedicationDto> medicationSummaries,
      @NonNull final DateTime when)
  {
    final List<String> diseaseTypeCodes = new ArrayList<>();
    for (final IdNameDto diseaseTypeValue : diseaseTypeValues)
    {
      diseaseTypeCodes.add(diseaseTypeValue.getId());
    }
    try
    {
      return screen(medicationSummaries, allergiesExternalValues, diseaseTypeCodes);
    }
    catch (InvalidParameterException | InvalidObjectException | TransformerException e)
    {
      throw new RuntimeException(e.getMessage());
    }
  }

  @Override
  public boolean requiresDiseaseCodesTranslation()
  {
    return false;
  }

  private List<MedicationsWarningDto> screen(
      final List<WarningScreenMedicationDto> medicationSummaries,
      final List<IdNameDto> allergiesExternalValues,
      final List<String> patientIcd10DiseasesCodes)
      throws InvalidParameterException, InvalidObjectException, TransformerException
  {
    final PatientProfile patientProfile = new PatientProfile(filter);
    medispan.screening.FilterManager.setUseScreen(filter, true);

    //screenForInteractions, screenForDuplicateTherapy
    for (final WarningScreenMedicationDto medicationSummary : medicationSummaries)
    {
      final IPatientDrug drugByGpi = getDrugByGpi(filter, medicationSummary.getExternalId());
      if (drugByGpi != null)
      {
        final PatientDrug patientDrug = new PatientDrug(drugByGpi);
        patientDrug.setScreen(medicationSummary.isProspective());
        patientDrug.setCustomId(String.valueOf(medicationSummary.getId()));
        patientDrug.setCustomName(medicationSummary.getName());
        patientProfile.getPatientDrugs().add(patientDrug);
      }
    }

    //screenForAllergicReactions
    final List<ConceptWithMediSpanId> allergenList = getAllergensForIds(allergiesExternalValues);
    for (final ConceptWithMediSpanId conceptWithMediSpanId : allergenList)
    {
      if (conceptWithMediSpanId instanceof AllergenClass)
      {
        final PatientAllergy patientAllergy = new PatientAllergy((AllergenClass)conceptWithMediSpanId);
        patientAllergy.setScreen(false);      //we screen medications against known (old) allergies
        patientProfile.getPatientAllergies().add(patientAllergy);
      }
      else if (conceptWithMediSpanId instanceof ScreenableIngredient)
      {
        final PatientAllergy patientAllergy = new PatientAllergy((ScreenableIngredient)conceptWithMediSpanId);
        patientAllergy.setScreen(false);      //we screen medications against known (old) allergies
        patientProfile.getPatientAllergies().add(patientAllergy);
      }
    }

    //screenForDrugDiseaseContraindications
    final List<Condition> conditions = getPatientConditions(patientIcd10DiseasesCodes);
    for (final Condition condition : conditions)
    {
      final PatientCondition patientCondition = new PatientCondition(condition);
      patientCondition.setScreen(false);
      patientProfile.getPatientConditions().add(patientCondition);
    }

    final List<MedicationsWarningDto> warningsList = new ArrayList<>();
    if (!patientProfile.getPatientDrugs().isEmpty())
    {
      final List<MedicationsWarningDto> allergicReactionsWarnings = screenForAllergicReactions(patientProfile);
      warningsList.addAll(allergicReactionsWarnings);

      final medispan.diseasecontraindications.Result contraindications =
          medispan.diseasecontraindications.Result.getForPatientProfile(filter, patientProfile);

      final List<MedicationsWarningDto> contraindicationsContraindicatedWarnings =
          extractDrugContraindications(
              contraindications,
              patientProfile,
              SeverityLevel.getContraindicated(filter));
      warningsList.addAll(contraindicationsContraindicatedWarnings);

      final DrugDrugResult interactions = DrugDrugResult.getForPatientProfile(filter, patientProfile);
      final List<MedicationsWarningDto> interactionsMajorWarnings =
          extractInteractions(
              interactions,
              patientProfile,
              medispan.interactions.SeverityLevel.getMajor(filter));
      warningsList.addAll(interactionsMajorWarnings);

      final List<MedicationsWarningDto> duplicateTherapyWarnings = screenForDuplicateTherapy(patientProfile);
      warningsList.addAll(duplicateTherapyWarnings);

      final List<MedicationsWarningDto> contraindicationsNotRecommendedWarnings =
          extractDrugContraindications(
              contraindications,
              patientProfile,
              SeverityLevel.getNotRecommended(filter));
      warningsList.addAll(contraindicationsNotRecommendedWarnings);

      final List<MedicationsWarningDto> contraindicationsExtremeCautionWarnings =
          extractDrugContraindications(
              contraindications,
              patientProfile,
              SeverityLevel.getExtremeCaution(filter));
      warningsList.addAll(contraindicationsExtremeCautionWarnings);

      final List<MedicationsWarningDto> interactionsModerateWarnings =
          extractInteractions(
              interactions,
              patientProfile,
              medispan.interactions.SeverityLevel.getModerate(filter));
      warningsList.addAll(interactionsModerateWarnings);

      final List<MedicationsWarningDto> contraindicationsUseCautiouslyWarnings =
          extractDrugContraindications(
              contraindications,
              patientProfile,
              SeverityLevel.getUseCautiously(filter));
      warningsList.addAll(contraindicationsUseCautiouslyWarnings);

      final List<MedicationsWarningDto> interactionsMinorWarnings =
          extractInteractions(
              interactions,
              patientProfile,
              medispan.interactions.SeverityLevel.getMinor(filter));
      warningsList.addAll(interactionsMinorWarnings);

      final List<MedicationsWarningDto> contraindicationsInformationalWarnings =
          extractDrugContraindications(
              contraindications,
              patientProfile,
              SeverityLevel.getInformational(filter));
      warningsList.addAll(contraindicationsInformationalWarnings);

      //TODO remove TMC-7083
      final medispan.foundation.collections.List<PatientDrug> screenableDrugs = new medispan.foundation.collections.List<>();
      for (final PatientDrug patientDrug : patientProfile.getPatientDrugs())
      {
        if (patientDrug.getScreen())
        {
          screenableDrugs.add(patientDrug);
        }
      }
      patientProfile.getPatientDrugs().clear();
      patientProfile.getPatientDrugs().addAll(screenableDrugs);
    }

    return warningsList;
  }

  private List<Condition> getPatientConditions(final List<String> patientIcd10DiseasesCodes)
      throws InvalidParameterException, InvalidObjectException
  {
    final List<Condition> conditions = new ArrayList<>();

    //uncomment, replace in for loop and prescribe a beta blocker like Atenolol
    //final List<String> demoDiseases = new ArrayList<>();
    //demoDiseases.add("J459");

    for (final String patientDisease : patientIcd10DiseasesCodes)
    {
      final List<ICD10CM> icd10List = QueryManager.getSelect(                //fix ICD10CM to ICD10CA ?
          filter,
          QueryManager.<String>getProperty(
              ICD10CM.UNFORMATTED_ICD10_ID_PROPERTY_MEDISPAN_ID,
              new TypeInfo(String.class, null)).isEqualTo(patientDisease),
          new TypeInfo(ICD10CM.class, null));

      for (final ICD10CM icd10 : icd10List)
      {
        final List<Condition> conditionsList = icd10.getConditions();
        for (final Condition condition : conditionsList)
        {
          conditions.add(condition);
        }
      }
    }

    return conditions;
  }

  private List<ConceptWithMediSpanId> getAllergensForIds(final List<IdNameDto> allergiesExternalValues)
  {
    final List<ConceptWithMediSpanId> allergensList = new ArrayList<>();

    for (final IdNameDto allergen : allergiesExternalValues)
    {
      if (allergen.getId() != null)
      {
        final ConceptWithMediSpanId screenableIngredient =
            QueryManager.getSelectForIdEqualTo(
                filter, Integer.parseInt(allergen.getId()), new TypeInfo(ScreenableIngredient.class, null));
        if (screenableIngredient != null)
        {
          allergensList.add(screenableIngredient);
        }
        else
        {
          final ConceptWithMediSpanId allergenClass =
              QueryManager.getSelectForIdEqualTo(
                  filter, Integer.parseInt(allergen.getId()), new TypeInfo(AllergenClass.class, null));
          if (allergenClass != null)
          {
            allergensList.add(allergenClass);
          }
          //else
          //{
          //  LOG.error("Alergen with id : " + allergenId + " not found in MEDISPAN base!");
          //}
        }
      }
    }
    return allergensList;
  }

  private String escapeMonographHtml(
      final String monographHtml,
      final medispan.foundation.collections.List<medispan.concepts.IPatientDrug> patientDrugs )
  {
    patientDrugs.forEach(drug -> monographHtml.replace(drug.getName(), Encode.forHtml(drug.getName())));
    return monographHtml;
  }

  private List<MedicationsWarningDto> extractInteractions(
      final DrugDrugResult interactions,
      final PatientProfile patientProfile,
      final medispan.interactions.SeverityLevel filterSeverityLevel) throws TransformerException
  {
    final List<MedicationsWarningDto> warningsList = new ArrayList<>();
    for (final Interaction interaction : interactions.getInteractions())
    {
      if (filterSeverityLevel.equals(interaction.getDefinition().getSeverityLevel()))
      {
        final MedicationsWarningDto warning = new MedicationsWarningDto();
        warning.setDescription(interaction.getMessage());
        warning.setMedications(getMedicationsForWarning(interaction.getDrugs(), patientProfile.getPatientDrugs()));

        final medispan.interactions.SeverityLevel severityLevel = interaction.getDefinition().getSeverityLevel();
        warning.setSeverity(getSeverityForForInteractions(severityLevel));
        warning.setExternalSeverity(severityLevel.getName());
        warning.setExternalType("Drug-Drug Interactions");
        warning.setType(WarningType.INTERACTION);
        warning.setMonographHtml(escapeMonographHtml(
            getMonographHtmlContent(interaction.getProfessionalMonograph()),
            interaction.getDrugs()));
        warningsList.add(warning);
      }
    }
    return warningsList;
  }

  private List<MedicationsWarningDto> screenForDuplicateTherapy(
     final PatientProfile patientProfile) throws InvalidObjectException, TransformerException
  {
    final Result result = Result.getForPatientProfile(filter, patientProfile);
    final List<MedicationsWarningDto> warningsList = new ArrayList<>();
    for (final DuplicateTherapy duplicateTherapy : result.getDuplicateTherapies())
    {
      final MedicationsWarningDto warning = new MedicationsWarningDto();
      warning.setType(WarningType.DUPLICATE);
      warning.setExternalType("Duplicate Therapy");
      warning.setSeverity(WarningSeverity.HIGH);
      warning.setSeverity(medsProperties.isDuplicateTherapyWarningOverrideRequired()
                          ? HIGH_OVERRIDE
                          : WarningSeverity.HIGH);
      warning.setDescription(duplicateTherapy.getMessage());
      warning.setMedications(getMedicationsForWarning(duplicateTherapy.getDrugs(), patientProfile.getPatientDrugs()));
      warning.setMonographHtml(escapeMonographHtml(
          getMonographHtmlContent(duplicateTherapy.getProfessionalMonograph()),
          duplicateTherapy.getDrugs()));
      warningsList.add(warning);
    }
    return warningsList;
  }

  private List<MedicationsWarningDto> screenForAllergicReactions(
      final PatientProfile patientProfile) throws InvalidObjectException, TransformerException
  {
    final medispan.allergicreactions.Result result = medispan.allergicreactions.Result.getForPatientProfile(
        filter,
        patientProfile);
    final List<MedicationsWarningDto> warningsList = new ArrayList<>();

    for (final AllergicReaction allergicReaction : result.getAllergicReactions())
    {
      final MedicationsWarningDto warning = new MedicationsWarningDto();
      warning.setSeverity(WarningSeverity.HIGH_OVERRIDE);
      warning.setType(WarningType.ALLERGY);
      warning.setExternalType("Allergic Reactions");
      warning.setDescription(allergicReaction.getMessage());
      warning.setMedications(getMedicationsForWarning(allergicReaction.getDrugs(), patientProfile.getPatientDrugs()));
      if (allergicReaction instanceof AllergicReactionAtClass)
      {
        final AllergicReactionAtClass allergicReactionAtClass = (AllergicReactionAtClass)allergicReaction;
        warning.setMonographHtml(escapeMonographHtml(
            getMonographHtmlContent(allergicReactionAtClass.getProfessionalMonograph()),
            allergicReaction.getDrugs()));
      }
      else if (allergicReaction instanceof AllergicReactionAtIngredient)
      {
        final AllergicReactionAtIngredient allergicReactionAtIngredient = (AllergicReactionAtIngredient)allergicReaction;
        warning.setMonographHtml(escapeMonographHtml(
            getMonographHtmlContent(allergicReactionAtIngredient.getProfessionalMonograph()),
            allergicReaction.getDrugs()));
      }
      warningsList.add(warning);
    }
    return warningsList;
  }

  private List<MedicationsWarningDto> extractDrugContraindications(
      final medispan.diseasecontraindications.Result result,
      final PatientProfile patientProfile,
      final medispan.diseasecontraindications.SeverityLevel filterSeverityLevel) throws InvalidObjectException, TransformerException
  {
    FilterManager.setMinimumSeverityLevel(filter, SeverityLevel.getInformational(filter));
    final List<MedicationsWarningDto> warningsList = new ArrayList<>();
    for (final Contraindication contraindication : result.getContraindications())
    {
      if (filterSeverityLevel.equals(contraindication.getDefinition().getSeverityLevel()))
      {
        final MedicationsWarningDto warning = new MedicationsWarningDto();
        warning.setMonographHtml(escapeMonographHtml(
            getMonographHtmlContent(contraindication.getProfessionalMonograph()),
            contraindication.getDrugs()));

        final SeverityLevel severityLevel = contraindication.getDefinition().getSeverityLevel();
        warning.setSeverity(getSeverityForDrugContraindications(severityLevel));
        warning.setExternalSeverity(severityLevel.getName());
        warning.setType(WarningType.PATIENT_CHECK);
        warning.setExternalType("Drug Contraindications");
        warning.setDescription(contraindication.getMessage());
        warning.setMedications(getMedicationsForWarning(contraindication.getDrugs(), patientProfile.getPatientDrugs()));
        warningsList.add(warning);
      }
    }
    return warningsList;
  }

  private WarningSeverity getSeverityForDrugContraindications(final SeverityLevel severityLevel)
  {
    try
    {
      if (SeverityLevel.getInformational(filter).equals(severityLevel) ||
          SeverityLevel.getUseCautiously(filter).equals(severityLevel))
      {
        return WarningSeverity.OTHER;
      }
      if (SeverityLevel.getExtremeCaution(filter).equals(severityLevel) ||
          SeverityLevel.getNotRecommended(filter).equals(severityLevel))
      {
        return WarningSeverity.HIGH;
      }
      if (SeverityLevel.getContraindicated(filter).equals(severityLevel))
      {
        return WarningSeverity.HIGH_OVERRIDE;
      }
    }
    catch (final InvalidObjectException e)
    {
      throw new RuntimeException(e.getMessage());
    }
    return null;
  }

  private WarningSeverity getSeverityForForInteractions(final medispan.interactions.SeverityLevel severityLevel)
  {
    try
    {
      if (medispan.interactions.SeverityLevel.getMinor(filter).equals(severityLevel))
      {
        return WarningSeverity.OTHER;
      }
      if (medispan.interactions.SeverityLevel.getModerate(filter).equals(severityLevel))
      {
        return WarningSeverity.OTHER;
      }
      if (medispan.interactions.SeverityLevel.getMajor(filter).equals(severityLevel))
      {
        return WarningSeverity.HIGH_OVERRIDE;
      }
    }
    catch (final InvalidObjectException e)
    {
      throw new RuntimeException(e.getMessage());
    }
    return null;
  }

  private String getMonographHtmlContent(final IXMLContent monograph) throws TransformerException
  {
    final String monographHtml = getMonographHtml(monograph); //entire html page, we only want the content
    if (monographHtml != null)
    {
      final int start = monographHtml.indexOf("<body>") + 6; //+6 to skip over <body> tag
      final int end = monographHtml.indexOf("</body>");
      return monographHtml.substring(start, end);
    }
    return null;
  }

  private String getMonographHtml(final IXMLContent monograph) throws TransformerException
  {
    if (monograph != null)
    {
      final TransformerFactory tf = TransformerFactory.newInstance();
      final Transformer transformer = tf.newTransformer();
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      final StringWriter writer = new StringWriter();
      transformer.transform(
          new DOMSource(HTMLManager.getAsHTML(monograph)),
          new StreamResult(writer));
      return writer.getBuffer().toString().replaceAll("\n|\r", "");
    }
    return null;
  }

  private List<NamedExternalDto> getMedicationsForWarning(
      final medispan.foundation.collections.List<IPatientDrug> drugs,
      final medispan.foundation.collections.List<PatientDrug> patientDrugs)
  {
    final List<NamedExternalDto> medicationsList = new ArrayList<>();
    for (final IPatientDrug drug : drugs)
    {
      for (final PatientDrug patientDrug : patientDrugs)
      {
        if(drug.getIdentifier().equals(patientDrug.getIdentifier()))
        {
          medicationsList.add(new NamedExternalDto(patientDrug.getCustomId(), patientDrug.getCustomName()));
        }
      }
    }
    return medicationsList;
  }

  private IPatientDrug getDrugByGpi(final Filter filter, final String drugGpi)
      throws InvalidParameterException, InvalidObjectException
  {
    final TypeInfo stringTypeInfo = new TypeInfo(String.class, null);
    final TypeInfo genericProductTypeInfo = new TypeInfo(GenericProduct.class, null);

    final ValuesOperatorExpression<String> gpiExpression =
        QueryManager.<String>getProperty(GenericProduct.GPI_PROPERTY_MEDISPAN_ID, stringTypeInfo).contains(drugGpi);

    final medispan.foundation.collections.List<GenericProduct> drugsWithGpi =
        QueryManager.getSelect(filter, gpiExpression, genericProductTypeInfo);
    return drugsWithGpi.isEmpty() ? null : drugsWithGpi.get(0);
  }

  @Override
  public String getExternalSystemName()
  {
    return "MEDISPAN";
  }
}
