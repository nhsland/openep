package com.marand.thinkmed.medications.model.impl;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

import com.marand.thinkmed.medications.model.impl.core.AbstractCatalogEntity;

/**
 * @author Mitja Lapajne
 */
@Entity
@Table(indexes = {
    @Index(name = "xpMedIndicationCode", columnList = "code")
})
public class MedicationIndicationImpl extends AbstractCatalogEntity
{
}
