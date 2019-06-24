package com.marand.thinkmed.medications.connector.impl.provider.ehr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.marand.maf.core.openehr.dao.OpenEhrDaoSupport;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.medications.connector.data.object.ObservationDto;
import com.marand.thinkmed.medications.connector.impl.provider.LabResultProvider;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.openehr.jaxb.rm.DvDateTime;
import org.openehr.jaxb.rm.DvText;
import org.springframework.stereotype.Component;

/**
 * @author Vid Kumse
 */

@Component
public class EhrLabResultProvider extends OpenEhrDaoSupport<String> implements LabResultProvider
{

  @Override
  public List<ObservationDto> getLabResults(
      final @NonNull String patientId,
      final @NonNull String resultCode,
      final @NonNull Interval interval)
  {
    final String ehrId = currentSession().findEhr(patientId);

    if (StringUtils.isEmpty(ehrId))
    {
      return Collections.emptyList();
    }
    currentSession().useEhr(ehrId);

    final String aql = buildAql(ehrId, resultCode, interval);

    return new ArrayList<>(queryEhrContent(aql, (resultRow, hasNext) ->
    {
      final Double resultValue = (Double)resultRow[0];
      final DateTime dateTime = DataValueUtils.getDateTime((DvDateTime)resultRow[1]);

      return resultRow[2] != null
             ? new ObservationDto(dateTime, resultValue, ((DvText)resultRow[2]).getValue())
             : new ObservationDto(dateTime, resultValue);
    }));
  }

  private String buildAql(final String ehrId, final String resultCode, final Interval interval)
  {
    final String measurementInr = "findings/items[at0001]/value/magnitude as measurementInr";
    final String dateTime = "spec/items[at0015]/value as dateTime";
    final String comment = "findings/items[at0003]/value as comment";

    return new StringBuilder()
        .append("SELECT ")
        .append(measurementInr).append(", ")
        .append(dateTime).append(", ")
        .append(comment).append(" ")
        .append(" FROM EHR e[ehr_id/value='").append(ehrId).append("'] ")
        .append(" CONTAINS OBSERVATION r[openEHR-EHR-OBSERVATION.laboratory_test_result.v0] ")
        .append(" CONTAINS ( CLUSTER spec[openEHR-EHR-CLUSTER.specimen.v0] AND ")
        .append("            CLUSTER findings[openEHR-EHR-CLUSTER.laboratory_test_analyte.v0] )")
        .append(" WHERE dateTime > ").append(getAqlDateTimeQuoted(interval.getStart()))
        .append(" AND dateTime < ").append(getAqlDateTimeQuoted(interval.getEnd()))
        .append(" AND findings/name/defining_code/code_string = '").append(resultCode).append("'")
        .toString();
  }
}
