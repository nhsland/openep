package com.marand.thinkmed.medications.service.impl;

import com.marand.maf.core.eventbus.Event;

/**
 * @author Bostjan Vester
 */
public interface MedicationsServiceEvents
{
  class PrescribeTherapy extends Event { }

  class ModifyTherapy extends Event { }

  class SuspendTherapy extends Event { }

  class SuspendAllTherapies extends Event { }

  class SuspendAllTherapiesOnTemporaryLeave extends Event { }

  class ReissueAllTherapiesOnReturnFromTemporaryLeave extends Event { }

  class AbortTherapy extends Event { }

  class AbortAllTherapies extends Event { }

  class ReissueTherapy extends Event { }

  class ConfirmAdministration extends Event { }

  class CreateAdministration extends Event { }

  class DeleteAdministration extends Event { }

  class RescheduleTasks extends Event { }

  class RescheduleTask extends Event { }

  class ReviewPharmacistReview extends Event { }

  class SavePharmacistReview extends Event { }

  class SaveMedicationsOnAdmission extends Event { }

  class SaveMedicationsOnDischarge extends Event { }

  class AdministrationChanged extends Event { }

  class InfusionBagDeleted extends Event { }

  class TaskAutoConfirmed extends Event { }

  class TaskAutoCreated extends Event { }

  class AllergiesChanged extends Event { }
}
