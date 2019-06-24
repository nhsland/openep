package com.marand.meds.rest.html;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.core.Response;

import com.marand.meds.app.prop.HtmlProperties;
import com.marand.thinkmed.html.AppHtmlConst;
import com.marand.thinkmed.html.AppHtmlHelper;
import com.marand.thinkmed.html.AppHtmlViewConfig;
import com.marand.thinkmed.html.RestServletHelper;
import com.marand.thinkmed.html.common.HtmlFramework;
import com.marand.thinkmed.html.common.HtmlJavaScript;
import com.marand.thinkmed.html.common.HtmlStyleSheet;
import com.marand.thinkmed.html.common.HtmlViewConfig;
import com.marand.thinkmed.html.externals.AngularJSHtmlExternal;
import com.marand.thinkmed.html.externals.AngularJSHtmlExternalPlugin;
import com.marand.thinkmed.html.externals.HighchartsHtmlExternal;
import com.marand.thinkmed.html.externals.MomentJsHtmlExternal;
import com.marand.thinkmed.html.externals.VisJsHtmlExternal;
import com.marand.thinkmed.html.framework.TmJQueryAngularHtmlFramework;
import com.marand.thinkmed.html.framework.TmJQueryHtmlFramework;
import com.marand.thinkmed.html.framework.TmJQueryHtmlFrameworkLite;
import com.marand.thinkmed.medications.TherapyAuthorityEnum;
import com.marand.thinkmed.medications.config.MedsProperties;
import com.marand.thinkmed.medications.witnessing.WitnessingProperties;
import com.marand.thinkmed.request.user.RequestUser;
import org.jboss.resteasy.spi.HttpRequest;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import static com.marand.thinkmed.html.RestServletHelper.createHtmlJavaScriptMap;
import static com.marand.thinkmed.html.RestServletHelper.createHtmlStyleSheetMap;
import static com.marand.thinkmed.html.RestServletHelper.resolveCSSResourcesAsSingleCssResource;
import static com.marand.thinkmed.html.RestServletHelper.resolveFrameworkResourcesAsSingleResource;
import static com.marand.thinkmed.html.RestServletHelper.resolveJSResourcesAsSingleJSResource;

/**
 * @author Boris Marn.
 */
@RestController
@RequestMapping("/medications")
public class ViewController
{

  private static final String MODULE_DIR_PATH = AppHtmlConst.DEFAULT_APP_VIEWS_SERVLET_PATH + "/medications";
  private static final String ANGULAR_MODULE_DIR_PATH = MODULE_DIR_PATH + "/jquery/pharmacistsTasks/angularComponents";
  private static final String ANGULAR_NURSE_TASK_MODULE_DIR_PATH = MODULE_DIR_PATH + "/jquery/nurseTasks/angularComponents";

  private static final String MODULE_COMMON_DIR_PATH = MODULE_DIR_PATH + "/jquery/common";
  private static final String MODULE_ORDERING_DIR_PATH = MODULE_DIR_PATH + "/jquery/ordering";
  private static final String MODULE_GRID_DIR_PATH = MODULE_DIR_PATH + "/jquery/grid";
  private static final String MODULE_TIMELINE_DIR_PATH = MODULE_DIR_PATH + "/jquery/timeline";
  private static final String MODULE_PHARMACISTS_REVIEW_DIR_PATH = MODULE_DIR_PATH + "/jquery/pharmacists";
  private static final String MODULE_MENTAL_HEALTH_DIR_PATH = MODULE_DIR_PATH + "/jquery/mentalHealth";
  private static final String MODULE_WARNING_DIR_PATH = MODULE_DIR_PATH + "/jquery/warnings";
  private static final String MODULE_RECONCILIATION_DIR_PATH = MODULE_DIR_PATH + "/jquery/reconciliation";
  private static final String MODULE_THERAPY_DOCUMENTATION_DIR_PATH = MODULE_DIR_PATH + "/jquery/documentation";

  private final HtmlProperties htmlProperties;
  private final MedsProperties medsProperties;
  private final WitnessingProperties witnessingProperties;

  @Autowired
  public ViewController(
      final HtmlProperties htmlProperties,
      final MedsProperties medsProperties,
      final WitnessingProperties witnessingProperties)
  {
    this.htmlProperties = htmlProperties;
    this.medsProperties = medsProperties;
    this.witnessingProperties = witnessingProperties;
  }

  @GetMapping(value = "getTimeOffset")
  public long getTimeOffset(@RequestHeader("Request-Time") final long requestTime)
  {
    return calculateTimeOffset(requestTime);
  }

  @ResponseBody
  @GetMapping(path = "therapyView", produces = MediaType.TEXT_PLAIN_VALUE)
  public String therapyView(
      @RequestParam("data") final String data,
      @RequestParam("language") final String language,
      @RequestParam("theme") final String theme,
      @RequestParam(value = "dictionary", required = false) final String dictionary,
      @RequestHeader("Request-Time") final long requestTime)
  {
    final AppHtmlViewConfig config = buildViewConfig("therapy", theme, language);
    config.addAllDictionaries();

    addTimeOffsetToConfig(config, requestTime);

    // properties
    final Map<String, Object> properties = medsProperties.getProperties();
    for (final Map.Entry<String, Object> property : properties.entrySet())
    {
      config.addProperty(property.getKey(), property.getValue());
    }

    // witnessing
    config.addProperty(WitnessingProperties.WITNESSING_ENABLED_CLIENT_PROPERTY, witnessingProperties.isEnabled());
    config.addProperty(WitnessingProperties.WITNESSING_MOCKED_CLIENT_PROPERTY, witnessingProperties.isMocked());
    config.addProperty(
        WitnessingProperties.WITNESSING_IV_REQUIRED_CLIENT_PROPERTY,
        witnessingProperties.isEnabled() && witnessingProperties.isIvRequired());

    // user authorities
    addUserDataToConfig(config);

    return config.toJson();
  }

  @GetMapping(path = "therapyTasksInpatientView", produces = MediaType.APPLICATION_JSON_VALUE)
  public String therapyTasksInpatientView(
      @RequestParam("data") final String data,
      @RequestParam("language") final String language,
      @RequestParam("theme") final String theme,
      @RequestHeader("Request-Time") final long requestTime)
  {
    final AppHtmlViewConfig config = buildViewConfig("therapyTasksInpatientView", theme, language);
    addTimeOffsetToConfig(config, requestTime);

    return config.toJson();
  }

  @GetMapping(path = "demoPortalTherapyView", produces = MediaType.TEXT_HTML_VALUE)
  public String openepView(
      @RequestParam("data") final String data,
      @RequestParam("theme") final String theme,
      @RequestParam("language") final String language,
      @RequestParam(value = "debug", defaultValue = "false") final Boolean debug,
      @RequestHeader("Request-Time") final long requestTime)
  {
    final AppHtmlViewConfig config = buildViewConfig("demoPortalTherapyView", theme, language);
    addTimeOffsetToConfig(config, requestTime);
    return config.toJson();
  }

  @GetMapping(path = "stressTestTherapyView", produces = MediaType.TEXT_HTML_VALUE)
  public String stressTestTherapyView(
      @RequestParam("data") final String data,
      @RequestParam("theme") final String theme,
      @RequestParam("language") final String language,
      @RequestParam(value = "debug", defaultValue = "false") final Boolean debug,
      @RequestHeader("Request-Time") final long requestTime)
  {
    final AppHtmlViewConfig config = buildViewConfig("stressTestTherapyView", theme, language);
    addTimeOffsetToConfig(config, requestTime);
    return config.toJson();
  }

  @ResponseBody
  @GetMapping(path = "pharmacistTasksView", produces = MediaType.TEXT_PLAIN_VALUE)
  public String pharmacistTasksView(
      @RequestParam("data") final String data,
      @RequestParam("language") final String language,
      @RequestParam("theme") final String theme,
      @RequestParam(value = "dictionary", required = false) final String dictionary,
      @RequestHeader("Request-Time") final long requestTime)
  {
    final AppHtmlViewConfig config = buildViewConfig("pharmacistTasksView", theme, language);
    config.addAllDictionaries();
    addTimeOffsetToConfig(config, requestTime);
    //user authorities
    addUserDataToConfig(config);
    return config.toJson();
  }

  @ResponseBody
  @GetMapping(path = "nurseTasksView", produces = MediaType.TEXT_PLAIN_VALUE)
  public String nurseTasksView(
      @RequestParam("data") final String data,
      @RequestParam("language") final String language,
      @RequestParam("theme") final String theme,
      @RequestParam(value = "dictionary", required = false) final String dictionary,
      @RequestHeader("Request-Time") final long requestTime)
  {
    final AppHtmlViewConfig config = buildViewConfig("nurseTasksView", theme, language);
    config.addAllDictionaries();
    addTimeOffsetToConfig(config, requestTime);

    return config.toJson();
  }

  private AppHtmlViewConfig getViewConfig(final String view, final String theme, final String language)
  {
    if ("therapy".equals(view))
    {
      return getTherapyViewConfig(theme, language);
    }
    if ("demoTherapyView".equals(view))
    {
      return getTherapyViewConfig(theme, language);
    }
    if ("demoPortalTherapyView".equals(view))
    {
      return getDemoPortalTherapyViewConfig(theme, language);
    }
    if ("stressTestTherapyView".equals(view))
    {
      return getStressTestTherapyViewConfig(theme, language);
    }
    if ("pharmacistTasksView".equals(view))
    {
      return getPharmacistTaskLists(theme, language);
    }
    if ("nurseTasksView".equals(view))
    {
      return getNurseTaskLists(theme, language);
    }
    if ("externalcallview".equals(view))
    {
      return getExternalCallViewConfig(theme, language);
    }
    return null;
  }

  private void addUserDataToConfig(final AppHtmlViewConfig config)
  {
    config.addProperty("userPersonId", RequestUser.getId());
    config.addProperty("userPersonName", RequestUser.getFullName());

    final Set<String> authorities = RequestUser.getAuthorities()
        .stream()
        .map(ga -> ga.getAuthority().toLowerCase()) // support case insensitive authorities matching
        .collect(Collectors.toSet());

    final Map<String, Boolean> authoritiesMap = TherapyAuthorityEnum.getMatchedAuthoritiesByLowerCase(authorities);
    for (final Map.Entry<String, Boolean> userAuthority : authoritiesMap.entrySet())
    {
      config.addProperty(userAuthority.getKey(), userAuthority.getValue());
    }

    config.addProperty(
        TherapyAuthorityEnum.PRESCRIBE_BY_TEMPLATES_ALLOWED_CLIENT_SIDE_NAME,
        TherapyAuthorityEnum.isPrescribeByTemplatesAllowed(authorities));
  }

  private AppHtmlViewConfig getTherapyViewConfig(final String theme, final String language)
  {
    final AppHtmlViewConfig config =
        new AppHtmlViewConfig(
            htmlProperties.isDevelopmentMode(),
            "tm.views.medications.TherapyView",
            new TmJQueryHtmlFramework(),
            theme,
            language);

    final AngularJSHtmlExternal angularJSHtmlExternal =
        AngularJSHtmlExternal.create(AngularJSHtmlExternal.VERSION_1_5_5.getVersion());
    angularJSHtmlExternal.addPlugin(AngularJSHtmlExternalPlugin.COMMON_1_5_5);

    // externals //
    config.addExternals(
        MomentJsHtmlExternal.VERSION_2_11_1,
        VisJsHtmlExternal.VERSION_4_20_1,
        angularJSHtmlExternal, // documentation
        HighchartsHtmlExternal.VERSION_4_1_5 // titration
    );

    // style sheets //
    config.addStyleSheetDependency(MODULE_DIR_PATH + "/jquery/TherapyView.css");
    config.addStyleSheetDependency(MODULE_WARNING_DIR_PATH + "/TherapyWarnings.css");
    config.addStyleSheetDependency(MODULE_WARNING_DIR_PATH + "/Monograph.css");

    // java scripts //
    config.addJavaScriptDependency(MODULE_DIR_PATH + "/jquery/RestApi.js");
    config.addJavaScriptDependency(MODULE_DIR_PATH + "/jquery/MedicationUtils.js");
    config.addJavaScriptDependency(MODULE_DIR_PATH + "/jquery/CurrentTime.js");
    config.addJavaScriptDependency(MODULE_DIR_PATH + "/jquery/MedicationTimingUtils.js");
    config.addJavaScriptDependency(MODULE_DIR_PATH + "/jquery/MedicationRuleUtils.js");
    config.addJavaScriptDependency(MODULE_DIR_PATH + "/jquery/TherapyEnums.js");
    config.addJavaScriptDependency(MODULE_DIR_PATH + "/jquery/TherapyAuthority.js");
    // main view
    config.addJavaScriptDependency(MODULE_DIR_PATH + "/jquery/TherapyView.js");

    // common elements
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/patient/AbstractReferenceData.js"); // keep on top
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/patient/ViewBasedReferenceData.js"); // keep on top
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/therapy/AbstractTherapyContainerData.js"); // keep on top!

    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/dto/AdditionalWarnings.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/dto/BarcodeTaskSearch.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/dto/ControlledDrugSupply.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/dto/DoseForm.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/dto/FormularyMedication.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/dto/DispenseDetails.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/dto/DispenseSource.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/dto/ReleaseDetails.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/dto/Therapy.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/dto/OxygenTherapy.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/dto/OxygenStartingDevice.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/dto/TherapyChangeReason.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/dto/MedicationData.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/dto/MedicationDocument.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/dto/Medication.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/dto/MedicationUnitType.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/dto/UnitsHolder.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/dto/PatientDataForMedications.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/dto/PrescribingDose.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/dto/PrescriptionPackage.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/dto/PrescriptionTherapy.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/dto/MedicationIngredient.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/dto/MedicationProperty.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/dto/InformationSource.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/dto/TherapyDose.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/dto/TherapyAuditTrail.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/dto/TherapyActionHistory.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/dto/TherapyChange.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/dto/TherapyViewPatient.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/dto/Range.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/dto/ReconciliationRow.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/dto/ReconciliationSummary.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/dto/MedicationRoute.js");

    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/auditTrail/AuditTrailContainer.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/auditTrail/AuditTrailTherapyContainerData.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/auditTrail/TherapyChangesContainer.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/testing/RenderCoordinator.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/overview/header/AbstractToolbarContainer.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/overview/header/DataEntryTooltipFactory.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/overview/header/OverviewHeader.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/overview/header/WestToolbarContainer.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/overview/AbstractSubViewContainer.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/overview/PatientDataContainer.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/overview/DateIntervalDataEntryContainer.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/overview/NumberDataEntryContainer.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/overview/ReferenceWeightDataEntryContainer.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/overview/TherapyRowGroupData.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/overview/TherapyRowDataGrouper.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/overview/TherapyRowCustomGroupData.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/therapy/BaseTherapyDetailsContentContainer.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/therapy/ChangeReasonTypeHolder.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/therapy/InformationSourceFilterBuilder.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/therapy/InformationSourceHolder.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/therapy/PharmacistTherapyReviewDataEntryContainer.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/therapy/TherapyActions.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/therapy/TherapyContainer.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/therapy/InlineTherapyContainer.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/therapy/TherapyContainerDisplayProvider.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/therapy/TherapyContainerToolbars.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/therapy/TherapyDetailsContentContainer.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/therapy/TherapyDetailsLegendContainer.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/therapy/TherapyDetailsLegendContainer.Filters.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/therapy/TherapyTasksRemindersContainer.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/therapy/TherapyJsonConverter.js");

    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/timeline/AbstractTimelineAdministrationTaskContentFactory.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/timeline/TimelineContentBuilder.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/ProtocolSummaryContainer.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/RangeField.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/RestErrorLogger.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/RowBasedDataContainer.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/DosingPatternValidator.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/TherapyGroupPanel.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/TemplateTherapyGroupPanel.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/TherapyStatusChangeReasonDataEntryContainer.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/VerticallyTitledComponent.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/ChangeReasonDataEntryContainer.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/MedicationSearchField.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/MedicationSearchResultFormatter.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/PerfusionSyringeDataEntryContainer.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/MedicationDetailsContainer.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/MedicationDocumentsContainer.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/TherapyMedicationDataLoader.js");
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/IconDescriptionContainer.js");

    //warnings
    config.addJavaScriptDependency(MODULE_WARNING_DIR_PATH + "/dto/MedicationForWarningsSearch.js");
    config.addJavaScriptDependency(MODULE_WARNING_DIR_PATH + "/dto/MedicationsWarning.js");
    config.addJavaScriptDependency(MODULE_WARNING_DIR_PATH + "/dto/MedicationsWarnings.js");
    config.addJavaScriptDependency(MODULE_WARNING_DIR_PATH + "/dto/ParacetamolRuleResult.js");
    config.addJavaScriptDependency(MODULE_WARNING_DIR_PATH + "/BaseWarningsContainer.js");
    config.addJavaScriptDependency(MODULE_WARNING_DIR_PATH + "/CurrentTherapiesWarningsDialogFactory.js");
    config.addJavaScriptDependency(MODULE_WARNING_DIR_PATH + "/WarningsHelpers.js");
    config.addJavaScriptDependency(MODULE_WARNING_DIR_PATH + "/WarningsContainer.js");
    config.addJavaScriptDependency(MODULE_WARNING_DIR_PATH + "/WarningsContainerRow.js");

    //grid sub view
    config.addJavaScriptDependency(MODULE_GRID_DIR_PATH + "/dto/TherapyDay.js");
    config.addJavaScriptDependency(MODULE_GRID_DIR_PATH + "/dto/TherapyFlow.js");
    config.addJavaScriptDependency(MODULE_GRID_DIR_PATH + "/dto/TherapyFlowRow.js");
    config.addJavaScriptDependency(MODULE_GRID_DIR_PATH + "/dto/TherapyReloadAfterAction.js");
    config.addJavaScriptDependency(MODULE_GRID_DIR_PATH + "/GridView.js");
    config.addJavaScriptDependency(MODULE_GRID_DIR_PATH + "/TherapyGridCellFormatter.js");
    config.addJavaScriptDependency(MODULE_GRID_DIR_PATH + "/toolbar/BaseGridTherapyContainerToolbar.js");
    config.addJavaScriptDependency(MODULE_GRID_DIR_PATH + "/toolbar/GridTherapyContainerToolbar.js");
    config.addJavaScriptDependency(MODULE_GRID_DIR_PATH + "/toolbar/InlineTherapyContainerToolbar.js");
    config.addJavaScriptDependency(MODULE_GRID_DIR_PATH + "/toolbar/GridTherapyContainerToolbarEvents.js");

    // ordering, abstract jsClasses first so we can inherit from them
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/AbstractTherapyOrder.js");
    //ordering complex
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/complex/ComplexTherapyContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/complex/HeparinPane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/complex/InfusionRatePane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/complex/VolumeSumPane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/complex/ComplexTherapyMedicationPane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/complex/ComplexVariableRateDataEntryContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/complex/ComplexVariableRateRowContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/complex/InfusionRateFormulaUnitPane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/complex/ComplexTherapyEditContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/complex/InfusionRateTypePane.js");

    // dtos
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/dto/CustomTemplatesGroup.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/dto/SaveMedicationOrder.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/dto/TherapyTemplateElement.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/dto/TherapyTemplatePrecondition.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/dto/TherapyTemplates.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/dto/TherapyTemplate.js");

    // dosing
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/dosing/AbstractVariableDoseDialogFactory.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/dosing/DoseContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/dosing/DosingFrequencyPane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/dosing/DosingPatternPane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/dosing/DefaultVariableDoseDialogFactory.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/dosing/UniversalDosePane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/dosing/VariableDoseDataEntryContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/dosing/VariableDoseDaysDataEntryContainer.js");

    //ordering simple
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/simple/ProtocolOptionsContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/simple/ReleaseDetailsContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/simple/SimpleTherapyContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/simple/SimpleTherapyEditContainer.js");

    // supply package
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/supply/ControlledDrugDetailsDataEntryContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/supply/ControlledDrugDetailsSupplyRowContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/supply/TherapySupplyContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/supply/SupplyQuantityComponent.js");

    // ordering oxygen
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/oxygen/OxygenSaturationInputContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/oxygen/OxygenRouteContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/oxygen/OxygenTherapyContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/oxygen/OxygenTherapyEditDataEntryContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/oxygen/OxygenFlowRateValidator.js");

    // ordering patient subpackage
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/patient/ReferenceData.js");

    // ordering warnings
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/warnings/WarningOverride.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/warnings/WarningsContainer.js");

    //ordering
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/TherapyDaysContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/MedicationDetailsCardPane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/RoutesPane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/MedicationsTitleHeader.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/SearchContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/MedicationSearchInpatientResultFormatter.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/MedicationsOrderingContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/BasketContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/OrderingBehaviour.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/OrderingContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/OrderingCoordinator.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/TherapyIntervalPane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/TherapyNextAdministrationLabelPane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/TherapyOrder.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/ChangeReasonContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/MaxDoseContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/ParacetamolLimitContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/OverdoseContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/TherapySaveDatePane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/LinkTherapyPane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/ApplicationPreconditionPane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/TherapyContainerToolbars.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/CommentIndicationPane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/ConfirmOrderEventData.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/SaveMedicationOrderContainerDisplayProvider.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/SaveOrderToTemplateEventData.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/PrescriptionContentExtensionContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/HighRiskMedicationIconsContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/InformationSourceContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/InformationSourceFilterBuilder.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/InformationSourceSelectionDataEntryContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/UniversalMedicationDataContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/ValueLabel.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/UniversalStrengthContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/PastTherapyStartContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/TherapyIconContainerFactory.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/calculationDisplay/CalculatedDosagePane.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/calculationDisplay/CalculationDetailsContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/calculationDisplay/CalculationFormula.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/calculationDisplay/DoseCalculationFormula.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/calculationDisplay/BodySurfaceCalculationFormula.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/calculationDisplay/MaxDoseCalculationFormula.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/calculationDisplay/RateCalculationFormula.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/timeline/AdministrationPreviewTimeline.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/timeline/PreviewTimelineAdministrationTaskContentFactory.js");

    // the templates package extends the ordering base classes so keep it underneath
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/templates/ManageTemplatesDataEntryContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/templates/ManageTemplatesDialogBuilder.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/templates/SaveTemplateDataEntryContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/templates/TemplatesContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/templates/TherapyTemplatePreconditionContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/templates/TherapyTemplatesHelpers.js");

    // outpatient prescribing (EER)
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/outpatient/EERContentExtensionContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/outpatient/OutpatientOrderingContainer.js");
    config.addJavaScriptDependency(MODULE_ORDERING_DIR_PATH + "/outpatient/MedicationSearchOutpatientResultFormatter.js");

    //timeline
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/dto/TherapyRow.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/TherapyTimeline.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/TherapyTimelineContainer.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/TherapyTimelineUtils.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/RescheduleTasksContainer.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/TimelineAdministrationTaskContentFactory.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/TimelineTherapyContainerToolbar.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/DoctorsCommentDataEntryContainer.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/TherapyTimelineAdditionalWarningsDialogContainer.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/TherapyTimelineTooltip.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/DeleteAdministrationContainer.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/administration/BaseTherapyAdministrationDataEntryContainer.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/administration/InfusionSetChangeDataEntryContainer.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/administration/OxygenStartingDeviceDataEntryContainer.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/administration/PlannedDoseTimeValidator.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/administration/SelfAdministrationDataEntryContainer.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/administration/AdministrationWarnings.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/administration/AdministrationWarningsProvider.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/administration/MedicationBarcodeContainer.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/administration/TherapyAdministrationDataEntryContainer.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/administration/TherapyAdministrationDetailsContentContainer.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/administration/TherapyAdministrationDialogBuilder.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/administration/TherapyAdministrationDoseContainer.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/administration/WarningContainer.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/administration/WitnessContainer.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/administration/dto/Administration.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/titration/dto/TherapyForTitration.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/titration/dto/Titration.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/titration/dto/QuantityWithTime.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/titration/ChartHelpers.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/titration/TherapyDoseHistoryRowContainer.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/titration/MeasurementResultRowContainer.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/titration/BaseApplicationRowContainer.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/titration/DoseApplicationRowContainer.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/titration/RateApplicationRowContainer.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/titration/TitrationBasedAdministrationDataEntryContainer.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/titration/TitrationDataLoader.js");
    config.addJavaScriptDependency(MODULE_TIMELINE_DIR_PATH + "/titration/TitrationDialogBuilder.js");

    // T2T3 prescribing
    config.addJavaScriptDependency(MODULE_MENTAL_HEALTH_DIR_PATH + "/dto/MentalHealthDocument.js");
    config.addJavaScriptDependency(MODULE_MENTAL_HEALTH_DIR_PATH + "/dto/MentalHealthMedication.js");
    config.addJavaScriptDependency(MODULE_MENTAL_HEALTH_DIR_PATH + "/dto/MentalHealthTemplate.js");
    config.addJavaScriptDependency(MODULE_MENTAL_HEALTH_DIR_PATH + "/dto/MentalHealthTherapy.js");
    config.addJavaScriptDependency(MODULE_MENTAL_HEALTH_DIR_PATH + "/MentalHealthTherapyContainerData.js");
    config.addJavaScriptDependency(MODULE_MENTAL_HEALTH_DIR_PATH + "/MentalHealthTemplateContainerData.js");
    config.addJavaScriptDependency(MODULE_MENTAL_HEALTH_DIR_PATH + "/T2T3OrderingContainer.js");
    config.addJavaScriptDependency(MODULE_MENTAL_HEALTH_DIR_PATH + "/T2T3TherapySelectionColumn.js");
    config.addJavaScriptDependency(MODULE_MENTAL_HEALTH_DIR_PATH + "/T2T3BasketContainer.js");
    config.addJavaScriptDependency(MODULE_MENTAL_HEALTH_DIR_PATH + "/T2T3MaxDoseContainer.js");
    config.addJavaScriptDependency(MODULE_MENTAL_HEALTH_DIR_PATH + "/TherapyContainerToolbars.js");
    config.addJavaScriptDependency(MODULE_MENTAL_HEALTH_DIR_PATH + "/RouteSelectionContainer.js");
    config.addJavaScriptDependency(MODULE_MENTAL_HEALTH_DIR_PATH + "/TherapyGroupPanel.js");
    config.addJavaScriptDependency(MODULE_MENTAL_HEALTH_DIR_PATH + "/TherapyOrder.js");

    //pharmacist's review
    config.addJavaScriptDependency(MODULE_PHARMACISTS_REVIEW_DIR_PATH + "/dto/TherapyProblemDescription.js");
    config.addJavaScriptDependency(MODULE_PHARMACISTS_REVIEW_DIR_PATH + "/dto/PharmacistReviewTherapy.js");
    config.addJavaScriptDependency(MODULE_PHARMACISTS_REVIEW_DIR_PATH + "/dto/PharmacistMedicationReview.js");
    config.addJavaScriptDependency(MODULE_PHARMACISTS_REVIEW_DIR_PATH + "/ResizingTextArea.js");
    config.addJavaScriptDependency(MODULE_PHARMACISTS_REVIEW_DIR_PATH + "/ColumnContainer.js");
    config.addJavaScriptDependency(MODULE_PHARMACISTS_REVIEW_DIR_PATH + "/ProblemDescriptionViewContainer.js");
    config.addJavaScriptDependency(MODULE_PHARMACISTS_REVIEW_DIR_PATH + "/ProblemDescriptionEditContainer.js");
    config.addJavaScriptDependency(MODULE_PHARMACISTS_REVIEW_DIR_PATH + "/ReviewView.js");
    config.addJavaScriptDependency(MODULE_PHARMACISTS_REVIEW_DIR_PATH + "/TherapyContainerToolbars.js");
    config.addJavaScriptDependency(MODULE_PHARMACISTS_REVIEW_DIR_PATH + "/TherapyContainer.js");
    config.addJavaScriptDependency(MODULE_PHARMACISTS_REVIEW_DIR_PATH + "/ReviewContainerViewContentCard.js");
    config.addJavaScriptDependency(MODULE_PHARMACISTS_REVIEW_DIR_PATH + "/ReviewContainerEditContentCard.js");
    config.addJavaScriptDependency(MODULE_PHARMACISTS_REVIEW_DIR_PATH + "/ReviewContainerHeader.js");
    config.addJavaScriptDependency(MODULE_PHARMACISTS_REVIEW_DIR_PATH + "/ConfirmAllTherapiesPlaceholderContainer.js");
    config.addJavaScriptDependency(MODULE_PHARMACISTS_REVIEW_DIR_PATH + "/ReviewContainer.js");
    config.addJavaScriptDependency(MODULE_PHARMACISTS_REVIEW_DIR_PATH + "/PharmacistMedicationReviewsByDate.js");
    config.addJavaScriptDependency(MODULE_PHARMACISTS_REVIEW_DIR_PATH + "/DailyReviewsContainer.js");
    config.addJavaScriptDependency(MODULE_PHARMACISTS_REVIEW_DIR_PATH + "/DailyReviewsContainerHeader.js");

    // medicine reconciliation
    config.addJavaScriptDependency(MODULE_RECONCILIATION_DIR_PATH + "/dto/SummaryRowTherapyData.js");
    config.addJavaScriptDependency(MODULE_RECONCILIATION_DIR_PATH + "/dto/SourceMedication.js");
    config.addJavaScriptDependency(MODULE_RECONCILIATION_DIR_PATH + "/dto/DischargeSourceMedication.js");
    config.addJavaScriptDependency(MODULE_RECONCILIATION_DIR_PATH + "/dto/MedicationGroup.js");
    config.addJavaScriptDependency(MODULE_RECONCILIATION_DIR_PATH + "/dto/MedicationGroupTherapy.js");
    config.addJavaScriptDependency(MODULE_RECONCILIATION_DIR_PATH + "/dto/MedicationOnAdmissionGroup.js");
    config.addJavaScriptDependency(MODULE_RECONCILIATION_DIR_PATH + "/dto/MedicationOnDischargeGroup.js");
    config.addJavaScriptDependency(MODULE_RECONCILIATION_DIR_PATH + "/dto/MedicationOnAdmission.js");
    config.addJavaScriptDependency(MODULE_RECONCILIATION_DIR_PATH + "/dto/MedicationOnDischarge.js");
    config.addJavaScriptDependency(MODULE_RECONCILIATION_DIR_PATH + "/ActiveTherapiesListAppContainer.js");
    config.addJavaScriptDependency(MODULE_RECONCILIATION_DIR_PATH + "/BaseMedicationReconciliationContainer.js");
    config.addJavaScriptDependency(MODULE_RECONCILIATION_DIR_PATH + "/MedicationOnAdmissionAndPrescribingEntryContainer.js");
    config.addJavaScriptDependency(MODULE_RECONCILIATION_DIR_PATH + "/MedicationOnAdmissionEntryContainer.js");
    config.addJavaScriptDependency(MODULE_RECONCILIATION_DIR_PATH + "/MedicationOnAdmissionContainer.js");
    config.addJavaScriptDependency(MODULE_RECONCILIATION_DIR_PATH + "/MedicationOnAdmissionDialogBuilder.js");
    config.addJavaScriptDependency(MODULE_RECONCILIATION_DIR_PATH + "/SummaryColumnTitleContainer.js");
    config.addJavaScriptDependency(MODULE_RECONCILIATION_DIR_PATH + "/SummaryRowContainer.js");
    config.addJavaScriptDependency(MODULE_RECONCILIATION_DIR_PATH + "/SummaryView.js");
    config.addJavaScriptDependency(MODULE_RECONCILIATION_DIR_PATH + "/BasketContainer.js");
    config.addJavaScriptDependency(MODULE_RECONCILIATION_DIR_PATH + "/CardContainer.js");
    config.addJavaScriptDependency(MODULE_RECONCILIATION_DIR_PATH + "/TherapyContainerToolbars.js");
    config.addJavaScriptDependency(MODULE_RECONCILIATION_DIR_PATH + "/TherapySelectionColumn.js");
    config.addJavaScriptDependency(MODULE_RECONCILIATION_DIR_PATH + "/ContinueConfirmCancelFooterButtonsContainer.js");
    config.addJavaScriptDependency(MODULE_RECONCILIATION_DIR_PATH + "/DischargeVariableDoseDaysDataEntryContainer.js");
    config.addJavaScriptDependency(MODULE_RECONCILIATION_DIR_PATH + "/DischargeVariableDoseDaysRowContainer.js");
    config.addJavaScriptDependency(MODULE_RECONCILIATION_DIR_PATH + "/DispenseSourcePrescriptionContentExtensionContainer.js");
    config.addJavaScriptDependency(MODULE_RECONCILIATION_DIR_PATH + "/InformationSourceDefaultsFilterFactory.js");
    config.addJavaScriptDependency(MODULE_RECONCILIATION_DIR_PATH + "/MedicationOnAdmissionPrescribingContainer.js");
    config.addJavaScriptDependency(MODULE_RECONCILIATION_DIR_PATH + "/MedicationOnDischargeContainer.js");
    config.addJavaScriptDependency(MODULE_RECONCILIATION_DIR_PATH + "/MedicationOnDischargeEntryContainer.js");
    config.addJavaScriptDependency(MODULE_RECONCILIATION_DIR_PATH + "/MedicationOnDischargeTherapyContainerDisplayProvider.js");
    config.addJavaScriptDependency(MODULE_RECONCILIATION_DIR_PATH + "/MedicationOnDischargeVariableDoseDialogFactory.js");
    config.addJavaScriptDependency(MODULE_RECONCILIATION_DIR_PATH + "/SuspendAdmissionTherapyDialogFooter.js");
    config.addJavaScriptDependency(MODULE_RECONCILIATION_DIR_PATH + "/SuspendAdmissionTherapyEntryContainer.js");

    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/TherapyDocumentationApp.module.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/common/ehr/Ehr.module.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/common/ehr/CompositionUidUtils.service.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/common/hub/Hub.module.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/common/hub/HubActionName.constant.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/common/document/Document.module.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/common/document/TmMedsDocumentPresenter.directive.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/common/document/DocumentPresenterController.controller.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/common/document/eerPrescription/TmMedsEerPrescriptionDocument.directive.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/common/document/eerPrescription/EerPrescriptionDocumentController.controller.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/common/document/eerPrescription/TmMedsEerPrescriptionDocumentSection.directive.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/common/document/eerPrescription/EerPrescriptionDocumentSectionController.controller.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/common/document/externalEerPrescription/TmMedsExternalEerPrescriptionDocument.directive.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/common/document/externalEerPrescription/TmMedsExternalEerPrescriptionDocumentController.controller.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/common/document/externalEerPrescription/TmMedsExternalEerPrescriptionDocumentSection.directive.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/common/document/externalEerPrescription/TmMedsExternalEerPrescriptionDocumentSectionController.controller.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/common/document/mentalHealth/MentalHealthDocumentController.controller.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/common/document/mentalHealth/TmMedsMentalHealthDocument.directive.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/common/document/TmMedsPrescriptionLocalDetailsAdditionalInformationValueFilter.filter.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/common/document/TmMedsEnumTranslationFilter.filter.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/common/documentHeader/DocumentHeader.module.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/common/documentHeader/TmMedsNamedExternalNameFilter.filter.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/common/documentHeader/TmMedsDocumentHeader.directive.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/common/documentHeader/DocumentHeaderController.controller.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/data/models/TherapyDocumentationModels.module.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/data/models/PrescriptionDocumentType.constant.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/data/models/PrescriptionLocalDetails.factory.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/data/models/PrescriptionPackage.factory.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/data/models/ExternalPrescriptionPackage.factory.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/data/models/ExternalPrescriptionTherapy.factory.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/data/models/MentalHealthDocumentContent.factory.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/data/models/MentalHealthTemplate.factory.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/data/models/MentalHealthMedication.factory.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/data/models/TherapyDocumentType.constant.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/data/models/PrescriptionStatus.constant.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/data/models/PrescriptionTherapy.factory.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/data/models/Therapy.factory.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/data/models/TherapyDocument.factory.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/data/models/TherapyDocuments.factory.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/data/Data.module.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/data/DocumentService.service.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/data/DocumentRestApi.service.js");

    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/externalPrescriptions/ExternalPrescriptions.module.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/externalPrescriptions/ExternalPrescriptionsPresenter.service.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/externalPrescriptions/PrescriptionsDialogController.controller.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/indexColumn/IndexColumn.module.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/indexColumn/TmMedsIndexColumnList.directive.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/indexColumn/TmMedsIndexColumnListItem.directive.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/indexColumn/ListItemController.controller.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/multiDocumentColumn/MultiDocumentColumn.module.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/multiDocumentColumn/TmMedsMultiDocumentColumn.directive.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/singleDocumentColumn/SingleDocumentColumn.module.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/singleDocumentColumn/TmMedsSingleDocumentColumn.directive.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/TherapyDocumentationView.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/TherapyDocumentationController.controller.js");
    config.addJavaScriptDependency(MODULE_THERAPY_DOCUMENTATION_DIR_PATH + "/TmMedsTherapyDocumentationView.directive.js");

    // resources - dictionaries //

    // resources - paths //
    return config;
  }

  private AppHtmlViewConfig getDemoPortalTherapyViewConfig(final String theme, final String language)
  {
    final AppHtmlViewConfig config =
        new AppHtmlViewConfig(
            htmlProperties.isDevelopmentMode(),
            "app.views.medications.demo.View",
            new TmJQueryHtmlFramework(),
            theme,
            language
        );

    config.addStyleSheetDependency(MODULE_DIR_PATH + "/jquery/demo/app.views.medications.demo.View.css");

    config.addJavaScriptDependency(MODULE_DIR_PATH + "/jquery/demo/app.views.medications.demo.View.js");

    return config;
  }

  private AppHtmlViewConfig getStressTestTherapyViewConfig(final String theme, final String language)
  {
    final AppHtmlViewConfig config =
        new AppHtmlViewConfig(
            htmlProperties.isDevelopmentMode(),
            "app.views.medications.stresstool.View",
            new TmJQueryHtmlFramework(),
            theme,
            language
        );

    config.addJavaScriptDependency(MODULE_DIR_PATH + "/jquery/stresstool/app.views.medications.stresstool.View.js");

    return config;
  }

  private AppHtmlViewConfig getPharmacistTaskLists(final String theme, final String language)
  {
    final TmJQueryAngularHtmlFramework framework = new TmJQueryAngularHtmlFramework();
    final AngularJSHtmlExternal angularJSExternal = framework.getAngularJSHtmlExternal();
    angularJSExternal.addPlugin(AngularJSHtmlExternalPlugin.COMMON_1_5_5);
    angularJSExternal.addPlugin(AngularJSHtmlExternalPlugin.DATA_TABLE_0_3_12);

    final AppHtmlViewConfig config =
        new AppHtmlViewConfig(
            htmlProperties.isDevelopmentMode(),
            "app.views.medications.pharmacistsTasks.View",
            framework,
            theme,
            language
        );

    config.addExternal(angularJSExternal);
    config.addExternal(MomentJsHtmlExternal.VERSION_2_11_1);
    addCommonMedsAngularJsResourcesToConfig(config);

    // error reporting
    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/RestErrorLogger.js");

    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/filter/pharmacistTasks.filter.js");
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/filter/syringeProgressTaskType.filter.js");

    //common directives
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/directive/resupplyForm/resupplyForm.dir.js");
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/directive/openCloseFilter/openCloseFilter.dir.js");

    //supply grid
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/directive/dueDate/dueDate.dir.js");
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/directive/nextDose/nextDose.dir.js");
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/directive/therapyType/therapyType.dir.js");
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/directive/therapyModificationType/therapyModificationType.dir.js");
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/directive/therapyAction/therapyAction.dir.js");
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/directive/reminderNote/reminderNote.dir.js");
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/directive/moreDropDownMenu/moreDropDownMenu.dir.js");

    //dispense grid
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/directive/dispenseRequested/dispenseRequested.dir.js");
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/directive/pharmacyReview/pharmacyReview.dir.js");
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/directive/supplyStatus/supplyStatus.dir.js");
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/directive/taskPrinted/taskPrinted.dir.js");

    //review grid
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/directive/dateTimeUser/dateTimeUser.dir.js");
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/directive/dateTime/dateTime.dir.js");
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/directive/careProvider/careProvider.dir.js");
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/directive/therapyTypeCell/therapyTypeCell.dir.js");

    // perfusion syringe list
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/directive/syringesList/syringesList.dir.js");
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/directive/syringesList/syringesListRow.dir.js");
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/directive/syringesFilterMenu/syringesFilterMenu.dir.js");
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/directive/syringeProgress/syringeProgress.dir.js");
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/directive/syringeOverview/syringeOverview.dir.js");

    //resources
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/resources/supply.resource.js");
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/resources/supply.service.js");
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/resources/taskAction.service.js");
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/controller/thinkGrid.ctrl.js");
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/controller/thinkGrid.service.js");
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/controller/syringesList.ctrl.js");
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/service/pharmacistsTask.service.js");

    config.addStyleSheetDependency(MODULE_DIR_PATH + "/jquery/pharmacistsTasks/app.views.medications.pharmacistsTasks.View.css");
    config.addJavaScriptDependency(ANGULAR_MODULE_DIR_PATH + "/app.views.medications.pharmacistsTask.module.js");
    config.addJavaScriptDependency(MODULE_DIR_PATH + "/jquery/pharmacistsTasks/app.views.medications.pharmacistsTasks.View.js");

    return config;
  }

  private AppHtmlViewConfig getNurseTaskLists(final String theme, final String language)
  {
    final AngularJSHtmlExternal angularJSHtmlExternal =
        AngularJSHtmlExternal.create(AngularJSHtmlExternal.VERSION_1_5_5.getVersion());
    angularJSHtmlExternal.addPlugin(AngularJSHtmlExternalPlugin.COMMON_1_5_5);
    angularJSHtmlExternal.addPlugin(AngularJSHtmlExternalPlugin.DATA_TABLE_0_3_12);

    final AppHtmlViewConfig config =
        new AppHtmlViewConfig(
            htmlProperties.isDevelopmentMode(),
            "app.views.medications.nurseTasks.View", new TmJQueryAngularHtmlFramework(), theme, language);
    // externals //
    config.addExternals(MomentJsHtmlExternal.VERSION_2_11_1, angularJSHtmlExternal);
    addCommonMedsAngularJsResourcesToConfig(config);

    config.addJavaScriptDependency(MODULE_COMMON_DIR_PATH + "/RestErrorLogger.js");

    config.addJavaScriptDependency(ANGULAR_NURSE_TASK_MODULE_DIR_PATH + "/filter/nurseTask.filter.js");
    config.addJavaScriptDependency(ANGULAR_NURSE_TASK_MODULE_DIR_PATH + "/filter/plannedDoseLabelKey.filter.js");

    config.addJavaScriptDependency(ANGULAR_NURSE_TASK_MODULE_DIR_PATH + "/controller/nurseListGrid.ctrl.js");

    config.addJavaScriptDependency(ANGULAR_NURSE_TASK_MODULE_DIR_PATH + "/directive/nurseTaskList/nurseTaskList.dir.js");
    config.addJavaScriptDependency(ANGULAR_NURSE_TASK_MODULE_DIR_PATH + "/directive/nurseTaskList/nurseTaskListRow.dir.js");
    config.addJavaScriptDependency(ANGULAR_NURSE_TASK_MODULE_DIR_PATH + "/directive/aplicationType/aplicationType.dir.js");
    config.addJavaScriptDependency(ANGULAR_NURSE_TASK_MODULE_DIR_PATH + "/directive/applicationPrecondition/applicationPrecondition.dir.js");
    config.addJavaScriptDependency(ANGULAR_NURSE_TASK_MODULE_DIR_PATH + "/directive/roomAndBed/roomAndBed.dir.js");

    config.addJavaScriptDependency(ANGULAR_NURSE_TASK_MODULE_DIR_PATH + "/resources/administrationTasksForCareProviders.resource.js");
    config.addJavaScriptDependency(ANGULAR_NURSE_TASK_MODULE_DIR_PATH + "/resources/administrationTasksForCareProviders.service.js");
    config.addJavaScriptDependency(ANGULAR_NURSE_TASK_MODULE_DIR_PATH + "/service/nurseTask.service.js");

    config.addStyleSheetDependency(MODULE_DIR_PATH + "/jquery/nurseTasks/app.views.medications.nurseTasks.View.css");
    config.addJavaScriptDependency(ANGULAR_NURSE_TASK_MODULE_DIR_PATH + "/app.views.medications.nurseTask.module.js");
    config.addJavaScriptDependency(MODULE_DIR_PATH + "/jquery/nurseTasks/app.views.medications.nurseTasks.View.js");

    return config;
  }

  @GetMapping(path = "tm.application.View.css", produces = "text/css")
  public String applicationViewCss(
      final HttpRequest request,
      @RequestParam("view") final String viewParam,
      @RequestParam("framework") final String framework,
      @RequestParam("theme") @DefaultValue(value = "fresh") final String themeParam,
      @RequestParam("language") @DefaultValue(value = "en") final String languageParam)
      throws UnsupportedEncodingException
  {

    final Map<String, List<HtmlStyleSheet>> map = createHtmlStyleSheetMap(getViewConfig(
        viewParam,
        themeParam,
        languageParam));

    return resolveCSSResourcesAsSingleCssResource(
        map.get("view"),
        request.getUri().getBaseUri().getPath().replaceAll("/", ""),
        getViewConfig(viewParam, themeParam, languageParam).getTheme()
            .name()
            .toLowerCase());
  }

  @GetMapping(path = "tm.application.View.js", produces = "application/javascript")
  public String applicationViewJS(
      @RequestParam("view") final String viewParam,
      @RequestParam("framework") final String framework,
      @RequestParam("theme") @DefaultValue(value = "fresh") final String themeParam,
      @RequestParam("language") @DefaultValue(value = "en") final String languageParam)
      throws UnsupportedEncodingException
  {
    final Map<String, List<HtmlJavaScript>> map = createHtmlJavaScriptMap(getViewConfig(
        viewParam,
        themeParam,
        languageParam));

    return resolveJSResourcesAsSingleJSResource(map.get("view"), false);
  }

  @GetMapping(path = "framework/framework.js", produces = MediaType.TEXT_PLAIN_VALUE)
  public String resolveFrameworkResourcesAsOneResource(
      @RequestParam("theme") final String theme,
      @RequestParam("framework") final String framework,
      @RequestParam("language") final String language)
  {
    try
    {
      return resolveFrameworkResourcesAsSingleResource(
          htmlProperties.getResourceCacheMaxAge(),
          htmlProperties.isDevelopmentMode(),
          framework,
          language).toString();
    }
    catch (UnsupportedEncodingException e)
    {
      e.printStackTrace();
      return Response.noContent().build().toString();
    }
  }

  private AppHtmlViewConfig buildViewConfig(final String view, final String theme, final String language)
  {
    final AppHtmlViewConfig originalHtmlViewConfig = getViewConfig(view, theme, language);

    if (!htmlProperties.isTestMode())
    {
      removeTestSelectorJavaScriptDependency(originalHtmlViewConfig);
    }

    if (!htmlProperties.isDevelopmentMode())
    {
      final HtmlFramework framework = originalHtmlViewConfig.getFramework();

      final AppHtmlViewConfig htmlViewConfig = originalHtmlViewConfig.copy(AppHtmlHelper.findFrameworkById(framework.getId()));
      htmlViewConfig.getJavaScript().getDependencies().clear();

      // java script //
      StringBuilder jsUrl = new StringBuilder();
      jsUrl.append("/ui/tm.application.Framework.js").append("?");
      jsUrl.append("framework").append("=").append(framework.getId()).append("&");
      jsUrl.append("theme").append("=").append(theme).append("&");
      jsUrl.append("language").append("=").append(language);
      htmlViewConfig.addJavaScriptDependency(jsUrl.toString());

      jsUrl = new StringBuilder();
      jsUrl.append("/ui/tm.application.Common.js").append("?");
      jsUrl.append("framework").append("=").append(framework.getId()).append("&");
      jsUrl.append("theme").append("=").append(theme).append("&");
      jsUrl.append("language").append("=").append(language);
      htmlViewConfig.addJavaScriptDependency(jsUrl.toString());

      // additional externals (not in Framework.js & in Common.js) //
      RestServletHelper.addAdditionalDependencies(
          htmlProperties.isDevelopmentMode(),
          htmlViewConfig,
          originalHtmlViewConfig);

      jsUrl = new StringBuilder();
      jsUrl.append("/tm.application.View.js").append("?");
      jsUrl.append("framework").append("=").append(framework.getId()).append("&");
      jsUrl.append("view").append("=").append(view).append("&");
      jsUrl.append("theme").append("=").append(theme).append("&");
      jsUrl.append("language").append("=").append(language);
      htmlViewConfig.addJavaScriptDependency(jsUrl.toString());
      return htmlViewConfig;
    }
    else
    {
      return originalHtmlViewConfig;
    }
  }


  @ResponseBody
  @GetMapping(path = "externalcallview", produces = MediaType.TEXT_PLAIN_VALUE)
  public String externalCallView(
      @RequestParam("data") final String data,
      @RequestParam("language") final String language,
      @RequestParam("theme") final String theme)
  {
    final AppHtmlViewConfig config = buildViewConfig("externalcallview", theme, language);

    return config.toJson();
  }

  private void addCommonMedsAngularJsResourcesToConfig(final AppHtmlViewConfig config)
  {
    config.addStyleSheetDependency(MODULE_DIR_PATH + "/angularjs/common/app.views.medications.angularjs.common.css");
    config.addJavaScriptDependency(MODULE_DIR_PATH + "/angularjs/common/app.views.medications.angularjs.common.module.js");
    config.addJavaScriptDependency(MODULE_DIR_PATH + "/angularjs/common/patient/app.views.medications.angularjs.common.patient.module.js");
    config.addJavaScriptDependency(MODULE_DIR_PATH + "/angularjs/common/patient/MedsPatientBanner.directive.js");
    config.addJavaScriptDependency(MODULE_DIR_PATH + "/angularjs/common/therapy/app.views.medications.angularjs.common.therapy.module.js");
    config.addJavaScriptDependency(MODULE_DIR_PATH + "/angularjs/common/therapy/MedsTherapyIcon.directive.js");
    config.addJavaScriptDependency(MODULE_DIR_PATH + "/angularjs/common/therapy/MedsTherapyIconController.controller.js");
    config.addJavaScriptDependency(MODULE_DIR_PATH + "/angularjs/common/therapy/MedsTherapyDescription.directive.js");
    config.addJavaScriptDependency(MODULE_DIR_PATH + "/angularjs/common/therapy/MedsTherapyDescriptionController.controller.js");
  }

  private AppHtmlViewConfig getExternalCallViewConfig(final String theme, final String language)
  {
    return new AppHtmlViewConfig(
        htmlProperties.isDevelopmentMode(),
        "app.views.common.AppExternalCallView",
        new TmJQueryHtmlFrameworkLite(),
        theme, language);
  }

  private void removeTestSelectorJavaScriptDependency(final AppHtmlViewConfig originalHtmlViewConfig)
  {
    for (final HtmlViewConfig.JavaScriptDependency javaScriptDependency : originalHtmlViewConfig.getJavaScript()
        .getDependencies())
    {
      if ("TestSelector".equals(javaScriptDependency.getName()))
      {
        originalHtmlViewConfig.getJavaScript().getDependencies().remove(javaScriptDependency);
        break;
      }
    }
  }

  private void addTimeOffsetToConfig(final AppHtmlViewConfig config, final long requestTime)
  {
    config.addProperty("timeOffset", calculateTimeOffset(requestTime));
  }

  private long calculateTimeOffset(final long requestTimestamp)
  {
    return new DateTime().getMillis() - requestTimestamp;
  }
}
