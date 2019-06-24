package com.marand.thinkmed.medications.model.impl;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.marand.thinkmed.medications.model.impl.core.AbstractCatalogEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */

@SuppressWarnings("CallToSimpleSetterFromWithinClass")
@Entity
@Table(indexes = {
    @Index(name = "xpAtcClassificationCode", columnList = "code"),
    @Index(name = "xfAtcClassificationParent", columnList = "parent_id"),
    @Index(name = "xfAtcClassificationTopParent", columnList = "top_parent_id")})
public class AtcClassificationImpl extends AbstractCatalogEntity
{
  private AtcClassificationImpl parent;
  private AtcClassificationImpl topParent;
  private int depth;
  private boolean leaf;
  private Set<AtcClassificationImpl> children = new HashSet<>();

  @ManyToOne(targetEntity = AtcClassificationImpl.class, fetch = FetchType.LAZY)
  public AtcClassificationImpl getParent()
  {
    return parent;
  }

  public void setParent(final AtcClassificationImpl parent)
  {
    this.parent = parent;
  }

  @ManyToOne(targetEntity = AtcClassificationImpl.class, fetch = FetchType.LAZY)
  public AtcClassificationImpl getTopParent()
  {
    return topParent;
  }

  public void setTopParent(final AtcClassificationImpl topParent)
  {
    this.topParent = topParent;
  }

  public int getDepth()
  {
    return depth;
  }

  public void setDepth(final int depth)
  {
    this.depth = depth;
  }

  public boolean isLeaf()
  {
    return leaf;
  }

  public void setLeaf(final boolean leaf)
  {
    this.leaf = leaf;
  }

  @OneToMany(targetEntity = AtcClassificationImpl.class, mappedBy = "parent", fetch = FetchType.LAZY)
  public Set<AtcClassificationImpl> getChildren()
  {
    return children;
  }

  public void setChildren(final Set<AtcClassificationImpl> children)
  {
    this.children = children;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);

    tsb.append("parent", parent)
        .append("topParent", topParent)
        .append("depth", depth)
        .append("leaf", leaf)
        .append("children", children)
    ;
  }
}
