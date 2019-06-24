package com.marand.thinkmed.medications.connector.impl.provider.ehr;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import com.marand.maf.core.openehr.dao.OpenEhrDaoSupport;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.medications.connector.data.object.AllergiesDto;
import com.marand.thinkmed.medications.connector.data.object.AllergiesStatus;
import com.marand.thinkmed.medications.connector.data.object.ehr.IdNameDto;
import com.marand.thinkmed.medications.connector.impl.provider.AllergiesProvider;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.openehr.jaxb.rm.DvDateTime;
import org.springframework.stereotype.Component;

/**
 * @author Mitja Lapajne
 * @author Nejc Korasa
 */

@SuppressWarnings("VariableNotUsedInsideIf")
@Component
public class EhrAllergiesProvider extends OpenEhrDaoSupport<String> implements AllergiesProvider
{
  static final String STATUS_SUSPECTED = "at0127";
  static final String STATUS_LIKELY = "at0064";
  static final String STATUS_CONFIRMED = "at0065";
  static final Set<String> VALID_STATUSES = Sets.newHashSet(STATUS_SUSPECTED, STATUS_LIKELY, STATUS_CONFIRMED);

  @Override
  public AllergiesDto getAllergies(final @NonNull String patientId)
  {
    final String ehrId = currentSession().findEhr(patientId);
    if (StringUtils.isEmpty(ehrId))
    {
      return new AllergiesDto(AllergiesStatus.NOT_CHECKED);
    }
    currentSession().useEhr(ehrId);

    final String latestAllergyUId = getLatestAllergyUId(ehrId);

    return latestAllergyUId == null
           ? new AllergiesDto(AllergiesStatus.NOT_CHECKED)
           : getAllergies(ehrId, latestAllergyUId);
  }

  @Override
  public AllergiesDto getAllergies(final @NonNull String ehrId, final @NonNull String uId)
  {
    final AllergiesDto allergies = new AllergiesDto();

    currentSession().useEhr(ehrId);
    queryEhrContent(buildLoadAllergiesAql(ehrId, uId), (resultRow, hasNext) ->
    {
      final String allergyName = (String)resultRow[1];
      final String globalExclusionStatement = (String)resultRow[3];
      final String absenceStatement = (String)resultRow[4];
      final String allergyId = (String)resultRow[5];

      if (allergyName != null)
      {
        final String status = (String)resultRow[2];

        if (status == null || VALID_STATUSES.contains(status))
        {
          final IdNameDto allergen = new IdNameDto(allergyId, allergyName);
          allergies.getAllergens().add(allergen);

          final DateTime reviewDate = DataValueUtils.getDateTime((DvDateTime)resultRow[0]);
          allergies.setReviewDate(reviewDate);
        }
      }
      else if (globalExclusionStatement != null)
      {
        allergies.setAllergiesStatus(AllergiesStatus.NO_KNOWN_ALLERGY);
      }
      else if (absenceStatement != null)
      {
        allergies.setAllergiesStatus(AllergiesStatus.NO_INFORMATION);
      }
      else
      {
        allergies.setAllergiesStatus(AllergiesStatus.NOT_CHECKED);
      }

      return null;
    });

    if (!allergies.getAllergens().isEmpty())
    {
      allergies.setAllergiesStatus(AllergiesStatus.PRESENT);
    }

    if (allergies.getAllergens().isEmpty() && allergies.getAllergiesStatus() == null)
    {
      allergies.setAllergiesStatus(AllergiesStatus.NO_KNOWN_ALLERGY);
    }

    return allergies;
  }

  @Override
  public List<String> getLatestAllergyUIds(final @NonNull String ehrId, final int numberOfIds)
  {
    currentSession().useEhr(ehrId);

    final String aql = new StringBuilder()
        .append("select top ").append(numberOfIds)
        .append(" c/uid/value as uid")
        .append(" FROM EHR[ehr_id/value='").append(ehrId).append("']")
        .append(" CONTAINS COMPOSITION c")
        .append(" CONTAINS SECTION c_s[openEHR-EHR-SECTION.allergies_adverse_reactions_rcp.v1]")
        .append(" ORDER BY c/context/start_time DESC")
        .toString();

    return queryEhrContent(aql, (resultRow, hasNext) -> (String)resultRow[0]);
  }

  @Override
  public String getPreviousAllergyUId(final @NonNull String ehrId, final @NonNull String compositionUId)
  {
    currentSession().useEhr(ehrId);

    final DateTime compositionStart = getCompositionTime(ehrId, compositionUId);
    if (compositionStart == null)
    {
      return null;
    }

    final String aql = new StringBuilder()
        .append("select top 1")
        .append(" c/uid/value as uid")
        .append(" FROM EHR[ehr_id/value='").append(ehrId).append("']")
        .append(" CONTAINS COMPOSITION c")
        .append(" CONTAINS SECTION c_s[openEHR-EHR-SECTION.allergies_adverse_reactions_rcp.v1]")
        .append(" WHERE c/context/start_time < ").append(getAqlDateTimeQuoted(compositionStart))
        .append(" ORDER BY c/context/start_time DESC")
        .toString();

    final List<String> result = queryEhrContent(aql, (resultRow, hasNext) -> (String)resultRow[0]);
    return result.isEmpty() ? null : result.get(0);
  }

  private DateTime getCompositionTime(final String ehrId, final String compositionUId)
  {
    final String aql = new StringBuilder()
        .append("select")
        .append(" c/context/start_time")
        .append(" FROM EHR[ehr_id/value='").append(ehrId).append("']")
        .append(" CONTAINS COMPOSITION c")
        .append(" WHERE c/uid/value = '").append(compositionUId).append("'")
        .append(" ORDER BY c/context/start_time DESC")
        .toString();

    final List<DateTime> result = queryEhrContent(
        aql,
        (resultRow, hasNext) -> DataValueUtils.getDateTime((DvDateTime)resultRow[0]));

    return result.isEmpty() ? null : result.get(0);
  }

  private String getLatestAllergyUId(final String ehrId)
  {
    final List<String> latestUIds = getLatestAllergyUIds(ehrId, 1);
    return latestUIds.isEmpty() ? null : latestUIds.get(0);
  }

  private String buildLoadAllergiesAql(final String ehrId, final String uId)
  {
    final String reviewDatePath = "c/context/start_time as start_time";
    final String allergenNamePath = "e/data[at0001]/items[at0002, 'Causative agent']/value/value as causative_agent_name";
    final String statusPath = "e/data[at0001]/items[at0063]/value/defining_code/code_string as status_code";
    final String globalExclusionPath = "e_b/data[at0001]/items[at0002, 'Global exclusion of adverse reactions']/value/value as global_exclusion";
    final String absenceStatementPath = "e_c/data[at0001]/items[at0002]/value/value as absence_statement";
    final String allergenIdPath = "e/data[at0001]/items[at0002, 'Causative agent']/value/defining_code/code_string as causative_agent_id";

    final StringBuilder aql = new StringBuilder()
        .append("SELECT").append(" ")
        .append(reviewDatePath).append(", ")
        .append(allergenNamePath).append(", ")
        .append(statusPath).append(", ")
        .append(globalExclusionPath).append(", ")
        .append(absenceStatementPath).append(", ")
        .append(allergenIdPath).append(" ")
        .append("FROM EHR[ehr_id/value='").append(ehrId).append("'] ")
        .append(" CONTAINS VERSIONED_OBJECT vo")
        .append(" CONTAINS VERSION v[all_versions]")
        .append(" CONTAINS COMPOSITION c")
        .append(" CONTAINS SECTION s[openEHR-EHR-SECTION.allergies_adverse_reactions_rcp.v1]")
        .append(" CONTAINS")
          .append(" (EVALUATION e[openEHR-EHR-EVALUATION.adverse_reaction_risk.v1] or")
          .append(" EVALUATION e_b[openEHR-EHR-EVALUATION.exclusion_global.v1] or")
          .append(" EVALUATION e_c[openEHR-EHR-EVALUATION.absence.v1])")
        .append(" WHERE v/uid/value='" + uId + "' ");

    return aql.toString();
  }
}