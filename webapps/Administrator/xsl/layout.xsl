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

        <xsl:call-template name="HTML-DOCTYPE"/>
        <html>
            <xsl:call-template name="html-head">
                <xsl:with-param name="title" select="$title"/>
                <xsl:with-param name="include" select="$include_head"/>
            </xsl:call-template>
            <body class="no_transition {$body_class}">
                <xsl:copy-of select="$include_body_top"/>
                <div class="main-load" id="main-load" style="display:none"></div>
                <div class="layout {$aside_collapse}">
                    <div class="content-overlay" id="content-overlay"></div>
                    <xsl:call-template name="main-header"/>
                    <div class="container">
                        <xsl:apply-templates select="//app_menu" mode="outline">
                            <xsl:with-param name="active-id" select="$active_aside_id"/>
                        </xsl:apply-templates>
                        <section class="content">
                            <xsl:call-template name="_content"/>
                        </section>
                    </div>
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
            <link rel="shortcut icon" href="img/favicon.png"/>
            <meta name="format-detection" content="telephone=no"/>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no"/>

            <link rel="stylesheet" href="/SharedResources/vendor/bootstrap/css/bootstrap.min.css"/>
            <link rel="stylesheet" href="/SharedResources/vendor/font-awesome/css/font-awesome.min.css"/>
            <link rel="stylesheet" href="/SharedResources/vendor/jquery/jquery-ui-1.11.4.custom/jquery-ui.min.css"/>
            <link rel="stylesheet" href="css/all.min.css"/>

            <xsl:call-template name="STYLE_FIX_FIELDSET"/>

            <script src="/SharedResources/vendor/jquery/jquery-2.1.4.min.js"></script>
            <script src="/SharedResources/vendor/jquery/jquery-ui-1.11.4.custom/jquery-ui.min.js"></script>
            <script src="/SharedResources/vendor/bootstrap/js/bootstrap.min.js"></script>
            <script src="js/app.bundle.js"></script>

            <xsl:copy-of select="$include"/>
        </head>
    </xsl:template>

    <xsl:template name="main-header">
        <header class="header navbar navbar-fixed-top">
            <div class="container">
                <div class="navbar-header">
                    <xsl:if test="//app_menu">
                        <button class="btn-side-nav-toggle" type="button" data-toggle="side-nav"></button>
                    </xsl:if>
                    <img class="brand-logo" alt="logo" src="{$APP_LOGO_IMG_SRC}"/>
                    <span class="brand-title">
                        <xsl:value-of select="$APP_NAME"/>
                    </span>
                </div>
                <nav class="navbar-nav navbar-right">
                    <ul class="nav navbar-right">
                        <li class="dropdown">
                            <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                                <i class="fa fa-user"></i>
                            </a>
                            <ul class="dropdown-menu right">
                                <li>
                                    <a class="user-profile" title="{//captions/user_profile/@caption}"
                                       href="Provider?id=userprofile">
                                        <xsl:value-of select="@username"/>
                                    </a>
                                </li>
                                <li class="divider"></li>
                                <li>
                                    <a class="logout" href="Logout">
                                        Logout
                                    </a>
                                </li>
                            </ul>
                        </li>
                    </ul>
                    <form class="navbar-form navbar-search" name="ft-search" action="Provider">
                        <input type="search" class="q" name="keyword" required="required" autocomplete="off" placeholder="Search">
                            <xsl:attribute name="value" select="//query/@keyword"/>
                        </input>
                        <button type="reset" class="resetwithselect">
                            <i class="fa fa-times"></i>
                        </button>
                        <select name="id" class="native search-target">
                            <option value="{//request/@id}" selected="selected">Здесь</option>
                            <option value="search">Везде</option>
                        </select>
                        <input type="submit" value="search"/>
                    </form>
                </nav>
            </div>
        </header>
    </xsl:template>

    <xsl:template name="main-footer"/>

</xsl:stylesheet>
