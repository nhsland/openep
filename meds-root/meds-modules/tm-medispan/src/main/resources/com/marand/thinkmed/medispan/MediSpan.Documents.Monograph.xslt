<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method='html' version='1.0' encoding='UTF-8' indent='yes'/>
    <xsl:param name="CSSFileName"></xsl:param>
    <xsl:param name="publishedinteractionliststitle"></xsl:param>
    <xsl:param name="managementlevelstitle"></xsl:param>
    <xsl:param name="labeledavoidancelevelstitle"></xsl:param>
    <xsl:param name="severitylevelstitle"></xsl:param>
    <xsl:param name="documentationlevelstitle"></xsl:param>
    <xsl:param name="onsetstitle"></xsl:param>
    <xsl:param name="placentaltransferstitle"></xsl:param>
    <xsl:param name="breastfeedingaapstitle"></xsl:param>
    <xsl:param name="breastfeedingratingstitle"></xsl:param>
    <xsl:param name="breastfeedingexcretedstitle"></xsl:param>
    <xsl:param name="alerttitle"></xsl:param>
    <xsl:param name="effecttitle"></xsl:param>
    <xsl:param name="mechanismtitle"></xsl:param>
    <xsl:param name="managementtitle"></xsl:param>
    <xsl:param name="discussiontitle"></xsl:param>
    <xsl:param name="commenttitle"></xsl:param>
    <xsl:param name="whattitle"></xsl:param>
    <xsl:param name="whytitle"></xsl:param>
    <xsl:param name="instructionstitle"></xsl:param>
    <xsl:param name="drugstitle"></xsl:param>
    <xsl:param name="referencestitle"></xsl:param>
    <xsl:template match="monograph">
        <html xmlns="http://www.w3.org/1999/xhtml">
            <head>
                <meta http-equiv="content-type" content="text/html; charset=UTF-8" />

                <link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/2.8.2r1/build/reset/reset-min.css" />
                <link href="{$CSSFileName}" rel="stylesheet" type="text/css" />
            </head>
            <body>
                <xsl:if test = "title">
                    <div id="title" class="Section">
                        <table>
                            <tr>
                                <td class="SectionTitle Title">
                                    <xsl:value-of select ="title"/>
                                </td>
                            </tr>
                        </table>
                    </div>
                </xsl:if>
                <xsl:if test = "publishedinteractionlists or managementlevels or severitylevels or labeledavoidancelevels or documentationlevels or onsets or placentaltransfers or breastfeedingaaps or breastfeedingratings or breastfeedingexcreteds">
                    <div id="codes" class="Section">
                        <table>
                            <tr>
                            	<xsl:if test = "publishedinteractionlists">
                                    <td class="CodeList">
                                        <table>
                                            <tr>
                                                <td class="CodeTitle">
                                                    <xsl:value-of select="$publishedinteractionliststitle"  />
                                                </td>
                                            </tr>
                                            <xsl:for-each select="publishedinteractionlists/publishedinteractionlist">
                                                <tr>
                                                    <td>
                                                        <xsl:attribute name="class">
                                                            <xsl:choose>
                                                                <xsl:when test="contains(../@selected, text())">
                                                                    <xsl:text>CodeItem CodeItemSelected</xsl:text>
                                                                </xsl:when>
                                                                <xsl:otherwise>
                                                                    <xsl:text>CodeItem</xsl:text>
                                                                </xsl:otherwise>
                                                            </xsl:choose>
                                                        </xsl:attribute>
                                                        <xsl:apply-templates select="." />
                                                    </td>
                                                </tr>
                                            </xsl:for-each>
                                        </table>
                                    </td>
                                </xsl:if>
                                <xsl:if test = "managementlevels">
                                    <td class="CodeList">
                                        <table>
                                            <tr>
                                                <td class="CodeTitle">
                                                    <xsl:value-of select="$managementlevelstitle"  />
                                                </td>
                                            </tr>
                                            <xsl:for-each select="managementlevels/managementlevel">
                                                <tr>
                                                    <td>
                                                        <xsl:attribute name="class">
                                                            <xsl:choose>
                                                                <xsl:when test="contains(../@selected, text())">
                                                                    <xsl:text>CodeItem CodeItemSelected</xsl:text>
                                                                </xsl:when>
                                                                <xsl:otherwise>
                                                                    <xsl:text>CodeItem</xsl:text>
                                                                </xsl:otherwise>
                                                            </xsl:choose>
                                                        </xsl:attribute>
                                                        <xsl:apply-templates select="." />
                                                    </td>
                                                </tr>
                                            </xsl:for-each>
                                        </table>
                                    </td>
                                </xsl:if>
                                <xsl:if test = "labeledavoidancelevels">
                                    <td class="CodeList">
                                        <table>
                                            <tr>
                                                <td class="CodeTitle">
                                                    <xsl:value-of select="$labeledavoidancelevelstitle"  />
                                                </td>
                                            </tr>
                                            <xsl:for-each select="labeledavoidancelevels/labeledavoidancelevel">
                                                <tr>
                                                    <td>
                                                        <xsl:attribute name="class">
                                                            <xsl:choose>
                                                                <xsl:when test="contains(../@selected, text())">
                                                                    <xsl:text>CodeItem CodeItemSelected</xsl:text>
                                                                </xsl:when>
                                                                <xsl:otherwise>
                                                                    <xsl:text>CodeItem</xsl:text>
                                                                </xsl:otherwise>
                                                            </xsl:choose>
                                                        </xsl:attribute>
                                                        <xsl:apply-templates select="." />
                                                    </td>
                                                </tr>
                                            </xsl:for-each>
                                        </table>
                                    </td>
                                </xsl:if>
                                <xsl:if test = "severitylevels">
                                    <td class="CodeList">
                                        <table>
                                            <tr>
                                                <td class="CodeTitle">
                                                    <xsl:value-of select="$severitylevelstitle"  />
                                                </td>
                                            </tr>
                                            <xsl:for-each select="severitylevels/severitylevel">
                                                <tr>
                                                    <td>
                                                        <xsl:attribute name="class">
                                                            <xsl:choose>
                                                                <xsl:when test="contains(../@selected, text())">
                                                                    <xsl:text>CodeItem CodeItemSelected</xsl:text>
                                                                </xsl:when>
                                                                <xsl:otherwise>
                                                                    <xsl:text>CodeItem</xsl:text>
                                                                </xsl:otherwise>
                                                            </xsl:choose>
                                                        </xsl:attribute>
                                                        <xsl:apply-templates select="." />
                                                    </td>
                                                </tr>
                                            </xsl:for-each>
                                        </table>
                                    </td>
                                </xsl:if>
                                <xsl:if test = "documentationlevels">
                                    <td class="CodeList">
                                        <table>
                                            <tr>
                                                <td class="CodeTitle">
                                                    <xsl:value-of select="$documentationlevelstitle"  />
                                                </td>
                                            </tr>
                                            <xsl:for-each select="documentationlevels/documentationlevel">
                                                <tr>
                                                    <td>
                                                        <xsl:attribute name="class">
                                                            <xsl:choose>
                                                                <xsl:when test="contains(../@selected, text())">
                                                                    <xsl:text>CodeItem CodeItemSelected</xsl:text>
                                                                </xsl:when>
                                                                <xsl:otherwise>
                                                                    <xsl:text>CodeItem</xsl:text>
                                                                </xsl:otherwise>
                                                            </xsl:choose>
                                                        </xsl:attribute>
                                                        <xsl:apply-templates select="." />
                                                    </td>
                                                </tr>
                                            </xsl:for-each>
                                        </table>
                                    </td>
                                </xsl:if>
                                <xsl:if test = "onsets">
                                    <td class="CodeList">
                                        <table>
                                            <tr>
                                                <td class="CodeTitle">
                                                    <xsl:value-of select="$onsetstitle"  />
                                                </td>
                                            </tr>
                                            <xsl:for-each select="onsets/onset">
                                                <tr>
                                                    <td>
                                                        <xsl:attribute name="class">
                                                            <xsl:choose>
                                                                <xsl:when test="contains(../@selected, text())">
                                                                    <xsl:text>CodeItem CodeItemSelected</xsl:text>
                                                                </xsl:when>
                                                                <xsl:otherwise>
                                                                    <xsl:text>CodeItem</xsl:text>
                                                                </xsl:otherwise>
                                                            </xsl:choose>
                                                        </xsl:attribute>
                                                        <xsl:apply-templates select="." />
                                                    </td>
                                                </tr>
                                            </xsl:for-each>
                                        </table>
                                    </td>
                                </xsl:if>
                                <xsl:if test = "placentaltransfers">
                                    <td class="CodeList">
                                        <table>
                                            <tr>
                                                <td class="CodeTitle">
                                                    <xsl:value-of select="$placentaltransferstitle"  />
                                                </td>
                                            </tr>
                                            <xsl:for-each select="placentaltransfers/placentaltransfer">
                                                <tr>
                                                    <td>
                                                        <xsl:attribute name="class">
                                                            <xsl:choose>
                                                                <xsl:when test="contains(../@selected, text())">
                                                                    <xsl:text>CodeItem CodeItemSelected</xsl:text>
                                                                </xsl:when>
                                                                <xsl:otherwise>
                                                                    <xsl:text>CodeItem</xsl:text>
                                                                </xsl:otherwise>
                                                            </xsl:choose>
                                                        </xsl:attribute>
                                                        <xsl:apply-templates select="." />
                                                    </td>
                                                </tr>
                                            </xsl:for-each>
                                        </table>
                                    </td>
                                </xsl:if>
                                <xsl:if test = "breastfeedingaaps">
                                    <td class="CodeList">
                                        <table>
                                            <tr>
                                                <td class="CodeTitle">
                                                    <xsl:value-of select="$breastfeedingaapstitle"  />
                                                </td>
                                            </tr>
                                            <xsl:for-each select="breastfeedingaaps/breastfeedingaap">
                                                <tr>
                                                    <td>
                                                        <xsl:attribute name="class">
                                                            <xsl:choose>
                                                                <xsl:when test="contains(../@selected, text())">
                                                                    <xsl:text>CodeItem CodeItemSelected</xsl:text>
                                                                </xsl:when>
                                                                <xsl:otherwise>
                                                                    <xsl:text>CodeItem</xsl:text>
                                                                </xsl:otherwise>
                                                            </xsl:choose>
                                                        </xsl:attribute>
                                                        <xsl:apply-templates select="." />
                                                    </td>
                                                </tr>
                                            </xsl:for-each>
                                        </table>
                                    </td>
                                </xsl:if>
                                <xsl:if test = "breastfeedingratings">
                                    <td class="CodeList">
                                        <table>
                                            <tr>
                                                <td class="CodeTitle">
                                                    <xsl:value-of select="$breastfeedingratingstitle"  />
                                                </td>
                                            </tr>
                                            <xsl:for-each select="breastfeedingratings/breastfeedingrating">
                                                <tr>
                                                    <td>
                                                        <xsl:attribute name="class">
                                                            <xsl:choose>
                                                                <xsl:when test="contains(../@selected, text())">
                                                                    <xsl:text>CodeItem CodeItemSelected</xsl:text>
                                                                </xsl:when>
                                                                <xsl:otherwise>
                                                                    <xsl:text>CodeItem</xsl:text>
                                                                </xsl:otherwise>
                                                            </xsl:choose>
                                                        </xsl:attribute>
                                                        <xsl:apply-templates select="." />
                                                    </td>
                                                </tr>
                                            </xsl:for-each>
                                        </table>
                                    </td>
                                </xsl:if>
                                <xsl:if test = "breastfeedingexcreteds">
                                    <td class="CodeList">
                                        <table>
                                            <tr>
                                                <td class="CodeTitle">
                                                    <xsl:value-of select="$breastfeedingexcretedstitle"  />
                                                </td>
                                            </tr>
                                            <xsl:for-each select="breastfeedingexcreteds/breastfeedingexcreted">
                                                <tr>
                                                    <td>
                                                        <xsl:attribute name="class">
                                                            <xsl:choose>
                                                                <xsl:when test="contains(../@selected, text())">
                                                                    <xsl:text>CodeItem CodeItemSelected</xsl:text>
                                                                </xsl:when>
                                                                <xsl:otherwise>
                                                                    <xsl:text>CodeItem</xsl:text>
                                                                </xsl:otherwise>
                                                            </xsl:choose>
                                                        </xsl:attribute>
                                                        <xsl:apply-templates select="." />
                                                    </td>
                                                </tr>
                                            </xsl:for-each>
                                        </table>
                                    </td>
                                </xsl:if>
                            </tr>
                        </table>
                    </div>
                </xsl:if>
                <xsl:if test = "alert">
                    <div id="alert" class="Section">
                        <table>
                            <tr>
                                <td class="CodeTitle">
                                    <xsl:value-of select="$alerttitle"  />
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <xsl:for-each select="alert/para">
                                        <p>
                                            <xsl:apply-templates select="." />
                                        </p>
                                    </xsl:for-each>
                                </td>
                            </tr>
                        </table>
                    </div>
                </xsl:if>
                <xsl:if test = "effect">
                    <div id="effect" class="Section">
                        <table>
                            <tr>
                                <td class="CodeTitle">
                                    <xsl:value-of select="$effecttitle"  />
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <xsl:for-each select="effect/para">
                                        <p>
                                            <xsl:apply-templates select="." />
                                        </p>
                                    </xsl:for-each>
                                </td>
                            </tr>
                        </table>
                    </div>
                </xsl:if>
                <xsl:if test = "mechanism">
                    <div id="mechanism" class="Section">
                        <table>
                            <tr>
                                <td class="CodeTitle">
                                    <xsl:value-of select="$mechanismtitle"  />
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <xsl:for-each select="mechanism/para">
                                        <p>
                                            <xsl:apply-templates select="." />
                                        </p>
                                    </xsl:for-each>
                                </td>
                            </tr>
                        </table>
                    </div>
                </xsl:if>
                <xsl:if test = "management">
                    <div id="management" class="Section">
                        <table>
                            <tr>
                                <td class="CodeTitle">
                                    <xsl:value-of select="$managementtitle"  />
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <xsl:for-each select="management/para">
                                        <p>
                                            <xsl:apply-templates select="." />
                                        </p>
                                    </xsl:for-each>
                                </td>
                            </tr>
                        </table>
                    </div>
                </xsl:if>
                <xsl:if test = "discussion">
                    <div id="discussion" class="Section">
                        <table>
                            <tr>
                                <td class="CodeTitle">
                                    <xsl:value-of select="$discussiontitle"  />
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <xsl:for-each select="discussion/para">
                                        <p>
                                            <xsl:apply-templates select="." />
                                        </p>
                                    </xsl:for-each>
                                </td>
                            </tr>
                        </table>
                    </div>
                </xsl:if>
                <xsl:if test = "comment">
                    <div id="comment" class="Section">
                        <table>
                            <tr>
                                <td class="CodeTitle">
                                    <xsl:value-of select="$commenttitle"  />
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <xsl:for-each select="comment/para">
                                        <p>
                                            <xsl:apply-templates select="." />
                                        </p>
                                    </xsl:for-each>
                                </td>
                            </tr>
                        </table>
                    </div>
                </xsl:if>
                <xsl:if test = "what">
                    <div id="what" class="Section">
                        <table>
                            <tr>
                                <td class="CodeTitle">
                                    <xsl:value-of select="$whattitle"  />
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <xsl:for-each select="what/para">
                                        <p>
                                            <xsl:apply-templates select="." />
                                        </p>
                                    </xsl:for-each>
                                </td>
                            </tr>
                        </table>
                    </div>
                </xsl:if>
                <xsl:if test = "why">
                    <div id="why" class="Section">
                        <table>
                            <tr>
                                <td class="CodeTitle">
                                    <xsl:value-of select="$whytitle"  />
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <xsl:for-each select="why/para">
                                        <p>
                                            <xsl:apply-templates select="." />
                                        </p>
                                    </xsl:for-each>
                                </td>
                            </tr>
                        </table>
                    </div>
                </xsl:if>
                <xsl:if test = "instructions">
                    <div id="instructions" class="Section">
                        <table>
                            <tr>
                                <td class="CodeTitle">
                                    <xsl:value-of select="$instructionstitle"  />
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <xsl:for-each select="instructions/para">
                                        <p>
                                            <xsl:apply-templates select="." />
                                        </p>
                                    </xsl:for-each>
                                </td>
                            </tr>
                        </table>
                    </div>
                </xsl:if>
                <xsl:if test = "drugs">
                    <div id="drugs" class="Section">
                        <table>
                            <tr>
                                <td class="CodeTitle">
                                    <xsl:value-of select="$drugstitle"  />
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <xsl:for-each select="drugs/drug">
                                        <p>
                                            <xsl:apply-templates select="." />
                                        </p>
                                    </xsl:for-each>
                                </td>
                            </tr>
                        </table>
                    </div>
                </xsl:if>
                <xsl:if test = "references/reference">
                    <div id="references" class="Section">
                        <table>
                            <tr>
                                <td class="CodeTitle">
                                    <xsl:value-of select="$referencestitle"  />
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <p>
                                        <xsl:for-each select="references/reference">
                                            <xsl:choose>
                                                <xsl:when test ="@id">
                                                    <div id="{@id}" class="Reference">
                                                        <span class="ReferenceIndex">
                                                            <xsl:value-of select="@id"/>
                                                            <xsl:text>.</xsl:text>
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
                                </td>
                            </tr>
                        </table>
                    </div>
                </xsl:if>
                <xsl:if test = "disclaimer">
                    <div id="footer" class="Section Footer">
                        <div id="disclaimer" class="SectionContent Disclaimer">
                            <xsl:for-each select="disclaimer/para">
                                <p>
                                    <xsl:apply-templates select="." />
                                </p>
                            </xsl:for-each>
                        </div>
                        <div id="copyright" class="SectionContent Copyright">
                            <xsl:for-each select="copyright/para">
                                <p>
                                    <xsl:value-of select="." />
                                </p>
                            </xsl:for-each>
                        </div>
                    </div>
                </xsl:if>
            </body>
        </html>
    </xsl:template>

    <xsl:template match="referenceref">
        <span xml:space="preserve"> </span>
        <sup>
            <a class="ReferenceLink" href="#{@id}">
                <xsl:value-of select="@id"/>
            </a>
        </sup>
    </xsl:template>
</xsl:stylesheet>
