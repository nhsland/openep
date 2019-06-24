package com.marand.thinkmed.medications.connector.impl.provider.ehr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.marand.maf.core.openehr.dao.OpenEhrDaoSupport;
import com.marand.thinkmed.medications.connector.data.object.DiseaseDto;
import com.marand.thinkmed.medications.connector.impl.provider.DiseasesProvider;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvText;
import org.springframework.stereotype.Component;

/**
 * @author Mitja Lapajne
 */
@Component
public class EhrDiseasesProvider extends OpenEhrDaoSupport<String> implements DiseasesProvider
{

  @Override
  public List<DiseaseDto> getPatientDiseases(final @NonNull String patientId)
  {
    final String ehrId = currentSession().findEhr(patientId);

    if (StringUtils.isEmpty(ehrId))
    {
      return Collections.emptyList();
    }
    currentSession().useEhr(ehrId);

    final String lastProblemListDateAql = "SELECT top 1 " +
        "c/uid/value as uid " +
        "FROM EHR[ehr_id/value='" + ehrId + "'] contains COMPOSITION c contains (" +
        "EVALUATION c_a[openEHR-EHR-EVALUATION.problem_diagnosis.v1] or " +
        "EVALUATION c_b[openEHR-EHR-EVALUATION.exclusion-problem_diagnosis.v0] or " +
        "EVALUATION c_c[openEHR-EHR-EVALUATION.absence.v1]) " +
        "WHERE c/archetype_details/template_id/value='Problem list' ORDER BY c/context/start_time DESC";

    final List<String> uuidsOfLastDisease = queryEhrContent(
        lastProblemListDateAql, (resultRow, hasNext) -> (String)resultRow[0]);
    final String uuidOfLastDisease = uuidsOfLastDisease.isEmpty() ? null : uuidsOfLastDisease.get(0);

    final List<DiseaseDto> diseases = new ArrayList<>();

    if (uuidOfLastDisease == null)
    {
      return diseases;
    }

    final String aqlString =
        "SELECT  " +
            " c/context/start_time as start_time," +
            " a_a/data[at0001]/items[at0002]/value as Diagnose," +
            " a_a/data[at0001]/items[at0069]/value as Comment" +
            " FROM EHR[ehr_id/value='" + ehrId + "'] contains COMPOSITION c contains (" +
            " EVALUATION a_a[openEHR-EHR-EVALUATION.problem_diagnosis.v1] or" +
            " EVALUATION a_b[openEHR-EHR-EVALUATION.exclusion-problem_diagnosis.v0] or" +
            " EVALUATION a_c[openEHR-EHR-EVALUATION.absence.v1])" +
            " WHERE c/uid/value='" + uuidOfLastDisease + "'" +
            " ORDER BY c/context/start_time DESC";

    queryEhrContent(aqlString, (resultRow, hasNext) ->
    {
      final DvText diagnose = (DvText)resultRow[1];

      if (diagnose != null)
      {
        final DiseaseDto disease;
        if (diagnose instanceof DvCodedText)
        {
          final String diseaseId = ((DvCodedText)diagnose).getDefiningCode().getCodeString();
          disease = new DiseaseDto(diseaseId, diagnose.getValue());
        }
        else
        {
          disease = new DiseaseDto(null, diagnose.getValue());
        }

        if (resultRow[2] != null)
        {
          disease.setComment(((DvText)resultRow[2]).getValue());
        }

        diseases.add(disease);
      }
      return null;
    });
    return diseases;
  }
}

