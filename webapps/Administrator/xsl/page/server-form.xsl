<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:import href="../layout.xsl"/>

    <xsl:template match="/request">
        <xsl:call-template name="layout"/>
    </xsl:template>

    <xsl:template name="_content">
        <xsl:apply-templates select="//request[@id = 'server-form']/page/response"/>
    </xsl:template>

    <xsl:template match="response">
        <form name="{@entity}" action="" data-edit="{@editable}">
            <header class="content-header">
                <h1 class="header-title">
                    Server <xsl:value-of select="content/hostname"/>
                </h1>
                <div class="content-actions">
                    <xsl:apply-templates select="//actionbar"/>
                </div>
            </header>
            <section class="content-body">
                <fieldset class="fieldset">
                    <div class="form-group">
                        <div class="control-label">
                           Hostname
                        </div>
                        <div class="controls">
                            <input type="text" name="name" value="{content/hostname}" class="span3" disabled="disabled"/>
                        </div>
                    </div>
                    <div class="form-group">
                        <div class="control-label">
                            Tmpdir
                        </div>
                        <div class="controls">
                            <input type="text" name="appcode" value="{content/tmpdir}" class="span6"
                                   disabled="disabled"/>
                        </div>
                    </div>
                    <div class="form-group">
                        <div class="control-label">
                           Orgname
                        </div>
                        <div class="controls">
                            <input type="text" name="appcode" value="{content/orgname}" class="span6"
                                   disabled="disabled"/>
                        </div>
                    </div>
                    <div class="form-group">
                        <div class="control-label">
                            Database
                        </div>
                        <div class="controls">
                            <input type="text" name="appcode" value="{content/database}" class="span6"
                                   disabled="disabled"/>
                        </div>
                    </div>
                    <div class="form-group">
                        <div class="control-label">
                            Officeframe
                        </div>
                        <div class="controls">
                            <input type="text" name="appcode" value="{content/officeframe}" class="span6"
                                   disabled="disabled"/>
                        </div>
                    </div>
                    <div class="form-group">
                        <div class="control-label">
                            Kernel
                        </div>
                        <div class="controls">
                            <input type="text" name="appcode" value="{content/kernel}" class="span6"
                                   disabled="disabled"/>
                        </div>
                    </div>
                    <div class="form-group">
                        <div class="control-label">
                            Starttime
                        </div>
                        <div class="controls">
                            <input type="text" name="appcode" value="{content/starttime}" class="span3"
                                   disabled="disabled"/>
                        </div>
                    </div>
                </fieldset>
            </section>
        </form>
    </xsl:template>

</xsl:stylesheet>
