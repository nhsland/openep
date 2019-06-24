package com.marand.thinkmed.medications.connector.impl.provider.ehr;

import java.util.List;

import com.marand.maf.core.openehr.dao.OpenEhrDaoSupport;
import com.marand.thinkmed.medications.connector.impl.provider.HeightProvider;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * @author Mitja Lapajne
 */
@Component
public class EhrHeightProvider extends OpenEhrDaoSupport<String> implements HeightProvider
{

  @Override
  public Double getPatientHeight(final @NonNull String patientId)
  {
    final String ehrId = currentSession().findEhr(patientId);
    if (StringUtils.isEmpty(ehrId))
    {
      return null;
    }
    currentSession().useEhr(ehrId);

    final String aqlString =
        "SELECT o/data[at0001]/events[at0002]/data[at0003]/items[at0004]/value/magnitude " +
            "FROM EHR e[ehr_id/value='" + ehrId + "'] " +
            "CONTAINS COMPOSITION c " +
            "CONTAINS OBSERVATION o[openEHR-EHR-OBSERVATION.height.v1] " +
            "ORDER BY o/data[at0001]/origin/value DESC " +
            "FETCH 1";

    final List<Double> heights = queryEhrContent(aqlString, (resultRow, hasNext) -> (Double)resultRow[0]);

    return heights.isEmpty() ? null : heights.get(0);
  }
}
