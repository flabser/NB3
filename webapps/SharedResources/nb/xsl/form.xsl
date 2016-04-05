<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:import href="layout.xsl"/>

    <xsl:template match="/request">
        <xsl:call-template name="layout"/>
    </xsl:template>

    <xsl:template name="_content">
        <form class="form">
            <header class="content-header">
                <h1 class="header-title"></h1>
                <div class="content-actions"></div>
            </header>
            <section class="content-body">
                <fieldset class="fieldset">
                    <div class="form-group">
                        <div class="control-label">
                            @caption
                        </div>
                        <div class="controls">
                            <input type="text" name="name" value=""/>
                        </div>
                    </div>
                </fieldset>
            </section>
        </form>
    </xsl:template>

</xsl:stylesheet>
