<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:import href="templates/constants.xsl"/>
    <xsl:import href="templates/outline.xsl"/>
    <xsl:import href="templates/view.xsl"/>
    <xsl:import href="templates/actions.xsl"/>

    <xsl:output method="html" encoding="utf-8" indent="no"/>

    <xsl:template name="layout">
        <xsl:param name="title" select="concat(//captions/title/@caption, ' - ', $APP_NAME)"/>
        <xsl:param name="active_aside_id" select="//app_menu//outline_current"/>
        <xsl:param name="aside_collapse" select="''"/>
        <xsl:param name="include_head" select="''"/>
        <xsl:param name="include_body_top" select="''"/>
        <xsl:param name="include_body_bottom" select="''"/>
        <xsl:param name="body_class" select="''"/>

        <html>
            <xsl:call-template name="html-head">
                <xsl:with-param name="title" select="$title"/>
                <xsl:with-param name="include" select="$include_head"/>
            </xsl:call-template>
            <body class="{$body_class}">
                <xsl:copy-of select="$include_body_top"/>
                <div class="main-load" id="main-load" style="display:none"></div>
                <div class="layout {$aside_collapse}">
                    <div class="content-overlay" id="content-overlay"></div>
                    <xsl:call-template name="main-header"/>
                    <xsl:apply-templates select="//app_menu" mode="outline">
                        <xsl:with-param name="active-id" select="$active_aside_id"/>
                    </xsl:apply-templates>
                    <section class="content">
                        <xsl:call-template name="_content"/>
                    </section>
                    <xsl:call-template name="main-footer"/>
                </div>
                <xsl:copy-of select="$include_body_bottom"/>
            </body>
        </html>
    </xsl:template>

    <xsl:template name="_content"/>

    <xsl:template name="html-head">
        <xsl:param name="title" select="''"/>
        <xsl:param name="include" select="''"/>
        <head>
            <title>
                <xsl:value-of select="$title"/>
            </title>
            <link rel="shortcut icon" href="favicon.ico"/>
            <meta name="format-detection" content="telephone=no"/>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no"/>

            <link rel="stylesheet" href="/SharedResources/nb/css/nb.min.css"/>
            <link rel="stylesheet" href="css/all.min.css"/>

            <script src="/SharedResources/nb/js/nb.build.js"></script>

            <xsl:copy-of select="$include"/>
        </head>
    </xsl:template>

    <xsl:template name="main-header">
        <header class="header navbar">

        </header>
    </xsl:template>

    <xsl:template name="main-footer"/>

</xsl:stylesheet>
