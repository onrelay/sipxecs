<?xml version="1.0" encoding="UTF-8"?>
<!--
  - Add hooks for loading spring beans.
  -->
<xsl:stylesheet 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	version="1.0">
<xsl:output method="xml" />


<!-- this parameter is not needed, bean classnames are pulled from spring file -->
<xsl:template match="parameter[@name='className']" mode="spring-service">
</xsl:template>

<!-- pass thru all other parts of service -->
<xsl:template match="text()|*" mode="spring-service">
	<xsl:apply-templates select="."/>
</xsl:template>
		
<xsl:template match='text()|*'>
  <xsl:copy>
    <xsl:copy-of select="@*"/>
    <xsl:apply-templates/>
  </xsl:copy>
</xsl:template>

</xsl:stylesheet>
