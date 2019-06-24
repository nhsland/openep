package com.marand.thinkmed.medications.batch.impl;

import com.marand.thinkmed.medications.batch.TherapyBatchActionHandler;
import com.marand.thinkmed.medications.business.MedicationsBo;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.therapy.updater.TherapyUpdater;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Mitja Lapajne
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class TherapyBatchActionHandlerTest
{
  @InjectMocks
  private TherapyBatchActionHandler therapyBatchActionHandler = new TherapyBatchActionHandlerImpl();

  @Mock
  private MedicationsOpenEhrDao medicationsOpenEhrDao;

  @Mock
  private MedicationsBo medicationsBo;

  @Mock
  private TherapyUpdater therapyUpdater;


  @Test
  public void testAbortAllTherapies()
  {
    therapyBatchActionHandler.abortAllTherapies("1", new DateTime(2016, 6, 10, 12, 0),null);
  }

  @Test
  public void testAbortAllTherapiesWithStopReason()
  {
    therapyBatchActionHandler.abortAllTherapies("1", new DateTime(2016, 6, 10, 12, 0),"Stop reason");
  }

  @Test
  public void testSuspendAllTherapies()
  {
    therapyBatchActionHandler.suspendAllTherapies("1", new DateTime(2016, 6, 10, 12, 0), null);
  }

  @Test
  public void testSuspendAllTherapiesOnTemporaryLeave()
  {
    therapyBatchActionHandler.suspendAllTherapiesOnTemporaryLeave("1", new DateTime(2016, 6, 10, 12, 0));
  }

  @Test
  public void testReissueAllTherapiesOnReturnFromTemporaryLeave()
  {
    therapyBatchActionHandler.reissueAllTherapiesOnReturnFromTemporaryLeave("1", new DateTime(2016, 6, 10, 12, 0));
  }
}
