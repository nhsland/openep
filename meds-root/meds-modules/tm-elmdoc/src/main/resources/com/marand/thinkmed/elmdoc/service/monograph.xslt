<?xml version="1.0" encoding="utf-8"?>
<!--<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:msxsl="urn:schemas-microsoft-com:xslt" exclude-result-prefixes="msxsl">-->
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method='html' version='1.0' encoding='UTF-8' indent='yes'/>
    <xsl:param name="documentuniqueid"></xsl:param> <!-- unique value for creating unique hyperlink ids -->
    <xsl:param name="managementlevelstitle">Действия</xsl:param>
    <xsl:param name="severitylevelstitle">Риск</xsl:param>
    <xsl:param name="documentationlevelstitle">Изученность</xsl:param>
    <xsl:param name="onsetstitle">Скорость</xsl:param>
    <xsl:param name="placentaltransferstitle">Placental Transer</xsl:param>  <!-- untranslated, not used -->
    <xsl:param name="breastfeedingaapstitle">Breast Feeding AAP</xsl:param> <!-- untranslated, not used -->
    <xsl:param name="breastfeedingratingstitle">Breast Feeding Rating</xsl:param> <!-- untranslated, not used -->
    <xsl:param name="breastfeedingexcretedstitle">Breast Feeding Excreted</xsl:param> <!-- untranslated, not used -->
    <xsl:param name="alerttitle">Предупреждение</xsl:param>
    <xsl:param name="effecttitle">Эффект</xsl:param>
    <xsl:param name="mechanismtitle">Механизм</xsl:param>
    <xsl:param name="managementtitle">Действия</xsl:param>
    <xsl:param name="discussiontitle">Обсуждение</xsl:param>
    <xsl:param name="commenttitle">Примечания</xsl:param>
    <xsl:param name="whattitle">Признаки</xsl:param>
    <xsl:param name="whytitle">Причины</xsl:param>
    <xsl:param name="instructionstitle">Инструкции</xsl:param>
    <xsl:param name="drugstitle">Препараты</xsl:param>
    <xsl:param name="referencestitle">Источники</xsl:param>
    <xsl:param name="includeReferences">true</xsl:param>

    <xsl:strip-space elements="monograph"/>
    <xsl:strip-space elements="title"/>
    <xsl:strip-space elements="alert"/>
    <xsl:strip-space elements="effect"/>
    <xsl:strip-space elements="mechanism"/>
    <xsl:strip-space elements="management"/>
    <xsl:strip-space elements="discussion"/>
    <xsl:strip-space elements="comment"/>
  
  <xsl:template match="monograph">
    <html>
      <head>
        <style type="text/css">
          h3 {
          margin-bottom: 0px;
          }
          h4 {
          margin-bottom: 0px;
          }
          p {
          margin-top: 0px;
          margin-bottom: 0px;
          }
          drug {
          font-weight: bold;
          }
          b {
          margin-right: 5px;
          }
          ul {
          margin-top: 0px;
          margin-bottom: 0px;
          }
          span {
          margin-bottom: 0;
          margin-top: 0;
          }
        </style>
      </head>
      <body>
    <xsl:if test = "title">
      <h3 id="title">
        <xsl:value-of select ="title"/>
      </h3>
    </xsl:if>
    <xsl:if test = "alert">
      <h4 style="margin-bottom: 0px;">
        <xsl:value-of select="$alerttitle"/>
      </h4>
      <xsl:for-each select="alert/para">
        <p>
          <xsl:apply-templates select="." />
        </p>
      </xsl:for-each>
    </xsl:if>
    <xsl:if test = "effect">
      <h4 style="margin-bottom: 0px;">
        <xsl:value-of select="$effecttitle"/>
      </h4>
      <xsl:for-each select="effect/para">
        <p>
          <xsl:apply-templates select="." />
        </p>
      </xsl:for-each>
    </xsl:if>
    <xsl:if test = "mechanism">
      <h4 style="margin-bottom: 0px;">
        <xsl:value-of select="$mechanismtitle"/>
      </h4>
      <xsl:for-each select="mechanism/para">
        <p>
          <xsl:apply-templates select="." />
        </p>
      </xsl:for-each>
    </xsl:if>
    <xsl:if test = "management">
      <h4 style="margin-bottom: 0px;">
        <xsl:value-of select="$managementtitle"/>
      </h4>
      <xsl:for-each select="management/para">
        <p>
          <xsl:apply-templates select="." />
        </p>
      </xsl:for-each>
    </xsl:if>
    <xsl:if test = "discussion">
      <h4 style="margin-bottom: 0px;">
        <xsl:value-of select="$discussiontitle"/>
      </h4>
      <xsl:for-each select="discussion/para">
        <p>
          <xsl:apply-templates select="." />
        </p>
      </xsl:for-each>
    </xsl:if>
    <xsl:if test = "comment">
      <h4 style="margin-bottom: 0px;">
        <xsl:value-of select="$commenttitle"/>
      </h4>
      <xsl:for-each select="comment/para">
        <p>
          <xsl:apply-templates select="." />
        </p>
      </xsl:for-each>
    </xsl:if>
    <xsl:if test = "what">
      <h4 style="margin-bottom: 0px;">
        <xsl:value-of select="$whattitle"/>
      </h4>
      <xsl:for-each select="what/para">
        <p>
          <xsl:apply-templates select="." />
        </p>
      </xsl:for-each>
    </xsl:if>
    <xsl:if test = "why">
      <h4 style="margin-bottom: 0px;">
        <xsl:value-of select="$whytitle"/>
      </h4>
      <xsl:for-each select="why/para">
        <p>
          <xsl:apply-templates select="." />
        </p>
      </xsl:for-each>
    </xsl:if>
    <xsl:if test = "instructions">
      <h4 style="margin-bottom: 0px;">
        <xsl:value-of select="$instructionstitle"/>
      </h4>
      <xsl:for-each select="instructions/para">
        <p>
          <xsl:apply-templates select="." />
        </p>
      </xsl:for-each>
    </xsl:if>
    <xsl:if test = "drugs">
      <h4 id="drugs">
        <xsl:value-of select="$drugstitle"/>
      </h4>
      <xsl:for-each select="drugs/drug">
        <p>
          <xsl:apply-templates select="." />
        </p>
      </xsl:for-each>
    </xsl:if>
    <xsl:if test="$includeReferences">
      <xsl:if test = "references/reference">
        <h4 id="references">
          <xsl:value-of select="$referencestitle"/>
        </h4>
        <p>
          <xsl:for-each select="references/reference">
            <xsl:choose>
              <xsl:when test ="@id">
                <div id="{$documentuniqueid}-{@id}" class="Reference">
                  <span class="ReferenceIndex">
                    <xsl:value-of select="@id"/>
                    <xsl:text>. </xsl:text>
                  </span>
                  <xsl:value-of select="."/>
                </div>
              </xsl:when>
              <xsl:otherwise>
                <div class="Reference">
                  <xsl:value-of select="."/>
                </div>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:for-each>
        </p>
      </xsl:if>
    </xsl:if>
    <xsl:if test = "disclaimer">
      <br></br>
      <hr></hr>
      <div id="footer" class="text-muted">
        <div id="disclaimer" class="SectionContent Disclaimer">
          <xsl:for-each select="disclaimer/para">
            <p>
              <small>
                <xsl:apply-templates select="." />
              </small>
            </p>
          </xsl:for-each>
        </div>
        <div id="copyright" class="SectionContent Copyright">
          <xsl:for-each select="copyright/para">
            <p>
              <small>
                <i>
                  <xsl:value-of select="." />
                </i>
              </small>
            </p>
          </xsl:for-each>
        </div>
      </div>
    </xsl:if>
        <p></p>
        <p></p>
      </body>
    </html>
  </xsl:template>
  <xsl:template match="drug">
    <strong>
      <xsl:value-of select="."/>
    </strong>
  </xsl:template>
  <xsl:template match="referenceref">
    <span xml:space="preserve"> </span>
    <sup>
      <a class="ReferenceLink" href="#{$documentuniqueid}-{@id}">
        <xsl:value-of select="@id"/>
      </a>
    </sup>
  </xsl:template>
  <xsl:template name="code-list">
    <xsl:param name="title"/>
    <xsl:param name="items"/>
    <td style="vertical-align:top; border: none">
      <h5>
        <xsl:value-of select="$title"  />
      </h5>
      <ul class="list-unstyled">
        <xsl:for-each select="$items">
          <li>
            <small>
              <xsl:choose>
                <xsl:when test="../@selected = text()">
                  <strong class="text-primary">
                    <xsl:apply-templates select="."/>
                  </strong>
                </xsl:when>
                <xsl:otherwise>
                  <span class="text-muted">
                    <xsl:apply-templates select="."/>
                  </span>
                </xsl:otherwise>
              </xsl:choose>
            </small>
          </li>
        </xsl:for-each>
      </ul>
    </td>
  </xsl:template>
</xsl:stylesheet>
