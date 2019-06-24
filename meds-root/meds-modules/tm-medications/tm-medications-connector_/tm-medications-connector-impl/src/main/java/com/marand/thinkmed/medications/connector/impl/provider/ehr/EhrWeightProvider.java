package com.marand.thinkmed.medications.connector.impl.provider.ehr;

import java.util.List;

import com.marand.maf.core.openehr.dao.OpenEhrDaoSupport;
import com.marand.thinkmed.medications.connector.impl.provider.WeightProvider;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.openehr.jaxb.rm.DvQuantity;
import org.springframework.stereotype.Component;

/**
 * @author Mitja Lapajne
 */
@Component
public class EhrWeightProvider extends OpenEhrDaoSupport<String> implements WeightProvider
{

  @Override
  public Double getPatientWeight(final @NonNull String patientId)
  {
    final String ehrId = currentSession().findEhr(patientId);

    if (StringUtils.isEmpty(ehrId))
    {
      return null;
    }
    currentSession().useEhr(ehrId);

    final String aqlString =
        "SELECT o/data[at0002]/events[at0003]/data[at0001]/items[at0004]/value, " +
            "o/data[at0002]/events[at0003]/time " +
            "FROM EHR[ehr_id/value='" + ehrId + "'] " +
            "CONTAINS Observation o[openEHR-EHR-OBSERVATION.body_weight.v1] " +
            "ORDER BY o/data[at0002]/events[at0003]/time DESC " +
            "FETCH 1";

    final List<Double> weights = queryEhrContent(aqlString, (resultRow, hasNext) ->
        ((DvQuantity)resultRow[0]).getMagnitude());

    return weights.isEmpty() ? null : weights.get(0);
  }
}
