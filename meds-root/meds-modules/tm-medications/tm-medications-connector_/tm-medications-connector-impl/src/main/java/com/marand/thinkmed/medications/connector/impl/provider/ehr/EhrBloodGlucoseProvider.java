package com.marand.thinkmed.medications.connector.impl.provider.ehr;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.marand.maf.core.openehr.dao.OpenEhrDaoSupport;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.medications.connector.data.object.ObservationDto;
import com.marand.thinkmed.medications.connector.impl.provider.BloodGlucoseProvider;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.Interval;
import org.openehr.jaxb.rm.DvDateTime;
import org.openehr.jaxb.rm.DvText;
import org.springframework.stereotype.Component;

/**
 * @author Mitja Lapajne
 * @author Nejc Korasa
 */
@Component
public class EhrBloodGlucoseProvider extends OpenEhrDaoSupport<String> implements BloodGlucoseProvider
{
  @Override
  public List<ObservationDto> getPatientBloodGlucoseMeasurements(
      final @NonNull String patientId,
      final @NonNull Interval interval)
  {
    final String ehrId = currentSession().findEhr(patientId);

    if (StringUtils.isEmpty(ehrId))
    {
      return Collections.emptyList();
    }
    currentSession().useEhr(ehrId);

    return queryEhrContent(buildLoadBloodGlucoseEntriesAql(ehrId, interval), (resultRow, hasNext) ->
    {
      final Double glucose = (Double)resultRow[0];
      final DvDateTime date = (DvDateTime)resultRow[1];

      return resultRow[2] != null
             ? new ObservationDto(DataValueUtils.getDateTime(date), glucose, ((DvText)resultRow[2]).getValue())
             : new ObservationDto(DataValueUtils.getDateTime(date), glucose);
    }).stream().filter(o -> o.getValue() != null).collect(Collectors.toList());
  }

  private String buildLoadBloodGlucoseEntriesAql(final String ehrId, final Interval interval)
  {
    return "SELECT o/data[at0001]/events[at0002]/data[at0003]/items[at0095]/items[at0096]/items[at0078.1]/value/magnitude, " +
        "o/data[at0001]/events[at0002]/time as time, " +
        "o/data[at0001]/events[at0002]/data[at0003]/items[at0101, 'Comment']/value/value " +
        "FROM EHR[ehr_id/value='" + ehrId + "'] " +
        "CONTAINS Observation o[openEHR-EHR-OBSERVATION.pathology_test-blood_glucose.v1] " +
        "WHERE time > " + getAqlDateTimeQuoted(interval.getStart()) + " " +
        "AND time < " + getAqlDateTimeQuoted(interval.getEnd()) + " " +
        "ORDER BY o/data[at0001]/events[at0002]/time DESC ";
  }
}

