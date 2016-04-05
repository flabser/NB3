<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:template match="app_menu" mode="outline">
        <xsl:param name="active-id" select="//app_menu//outline_current"/>

        <aside class="aside side-nav" id="side-nav">
            <xsl:apply-templates select="response/content/outline" mode="outline">
                <xsl:with-param name="active-id" select="$active-id"/>
            </xsl:apply-templates>
        </aside>
    </xsl:template>

    <xsl:template match="outline" mode="outline">
        <xsl:param name="active-id" select="''"/>

        <section>
            <xsl:if test="@caption != ''">
                <xsl:attribute name="class" select="'side-nav-collapsible'"/>
                <xsl:attribute name="id" select="concat('side-nav-', @id)"/>
                <header data-role="toggle">
                    <i class="side-tree-toggle fa"></i>
                    <span>
                        <xsl:value-of select="@caption"/>
                    </span>
                </header>
            </xsl:if>
            <ul>
                <xsl:apply-templates mode="outline">
                    <xsl:with-param name="active-id" select="$active-id"/>
                </xsl:apply-templates>
            </ul>
        </section>
    </xsl:template>

    <xsl:template match="entry" mode="outline">
        <xsl:param name="active-id" select="''"/>

        <li>
            <a href="{@url}" title="{@hint}" class="nav-link" data-nav="{@id}{position()}">
                <xsl:if test="./entry">
                    <xsl:attribute name="class" select="'nav-link collapsible nav-link-collapsed'"/>
                </xsl:if>
                <xsl:if test="@id = $active-id">
                    <xsl:attribute name="class" select="'nav-link active'"/>
                    <xsl:if test="./entry">
                        <xsl:attribute name="class" select="'nav-link active collapsible nav-link-collapsed'"/>
                    </xsl:if>
                </xsl:if>
                <xsl:if test="./entry">
                    <i class="side-tree-toggle fa" data-role="side-tree-toggle"></i>
                </xsl:if>
                <xsl:choose>
                    <xsl:when test="@id = 'users'">
                        <i class="fa fa-users"></i>
                    </xsl:when>
                    <xsl:otherwise>
                        <i class="fa fa-file-o"></i>
                    </xsl:otherwise>
                </xsl:choose>
                <span>
                    <xsl:value-of select="@caption"/>
                </span>
            </a>
            <xsl:if test="./entry">
                <ul>
                    <xsl:apply-templates mode="outline">
                        <xsl:with-param name="active-id" select="$active-id"/>
                    </xsl:apply-templates>
                </ul>
            </xsl:if>
        </li>
    </xsl:template>

</xsl:stylesheet>
