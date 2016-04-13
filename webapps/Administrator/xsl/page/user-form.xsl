<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:import href="../layout.xsl"/>

    <xsl:template match="/request">
        <xsl:call-template name="layout"/>
    </xsl:template>

    <xsl:template name="_content">
        <xsl:apply-templates select="//document[@entity = 'user']"/>
    </xsl:template>

    <xsl:template match="document[@entity]">
        <form name="{@entity}" action="" data-edit="{@editable}">
            <header class="content-header">
                <h1 class="header-title">
                    User <xsl:value-of select="fields/login"/>
                </h1>
                <div class="content-actions">
                    <xsl:apply-templates select="//actionbar"/>
                </div>
            </header>
            <section class="content-body">
                <fieldset class="fieldset">
                    <div class="form-group">
                        <div class="control-label">
                            Login
                        </div>
                        <div class="controls">
                            <input type="text" name="login" value="{fields/login}" class="span4" autofocus="true"/>
                        </div>
                    </div>
                    <div class="form-group">
                        <div class="control-label">
                           E-mail
                        </div>
                        <div class="controls">
                            <input type="email" name="email" value="{fields/email}" class="span4"/>
                        </div>
                    </div>
                    <div class="form-group">
                        <div class="control-label">
                           Password
                        </div>
                        <div class="controls">
                            <input type="password" name="pwd" value="" class="span3" autocomplete="off"/>
                        </div>
                    </div>
                    <div class="form-group">
                        <div class="control-label">
                            Password comfirmation
                        </div>
                        <div class="controls">
                            <input type="password" name="pwd_confirm" value="" class="span3" autocomplete="off"/>
                        </div>
                    </div>
                    <div class="form-group">
                        <div class="control-label">
                            Allowed applications
                        </div>
                        <div class="controls">
                            <ul class="list-style-none">
                                <xsl:variable name="apps" select="fields/apps/apps"/>
                                <xsl:for-each select="//query[@entity = 'application']/entry">
                                    <li>
                                        <label>
                                            <input type="checkbox" name="app" value="{@id}">
                                                <xsl:if test="$apps/entry/@id = @id">
                                                    <xsl:attribute name="checked" select="'checked'"/>
                                                </xsl:if>
                                            </input>
                                            <span>
                                                <xsl:value-of select="viewcontent/app"/>
                                            </span>
                                        </label>
                                    </li>
                                </xsl:for-each>
                            </ul>
                        </div>
                    </div>
                </fieldset>
            </section>
        </form>
    </xsl:template>

</xsl:stylesheet>
