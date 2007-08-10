<?xml version='1.0'?>

<!--xsl:stylesheet xmlns:xsl='http://www.w3.org/XSL/Transform/1.0'-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:param name="baseActionURL">index.jsp</xsl:param>
<xsl:param name="locale">lv_LV</xsl:param>

<xsl:template match="editelement">
<html>
<body>

<p align="center"><xsl:value-of select="type"/> "<xsl:value-of select="name"/>" rediģēšana:</p>
<form method="post">
<xsl:attribute name="action"><xsl:value-of select="$baseActionURL"/></xsl:attribute>
<input type="hidden" name="action" value="submitEditValues"/>


<table border="1" cellpadding="5" cellspacing="0" align="center">
<tr><td><b>mainīgais</b></td><td><b>vērtība</b></td><td><b>apraksts</b></td></tr>
<tr><td colspan="3" align="right"> iekšējie <xsl:value-of select="type"/> atribūti</td></tr>
<tr><td><xsl:value-of select="type"/> nosaukums</td><td><input type="text" name="name" value="{name}"/></td><td><xsl:value-of select="type"/> nosaukums</td></tr>

<!-- eventually this should check if there are any attributes at all of this class before drawing a table row-->
<!-- process structure stylesheet attributes-->
<tr><td colspan="3" align="right"> struktūras stilu lapas atribūti</td></tr>
<xsl:for-each select="structureattributes/attribute">
<xsl:call-template name="processAttribute"/>
</xsl:for-each>

<!-- process theme stylesheet attributes-->
<tr><td colspan="3" align="right">tēmas stila lapas atribūti</td></tr>
<xsl:for-each select="themeattributes/attribute">
<xsl:call-template name="processAttribute"/>
</xsl:for-each>
</table>

<p align="center">
<input type="submit" name="submit" value="Saglabāt"/>
<input type="submit" name="submit" value="Atcelt"/>
</p>

</form>
</body>
</html>
</xsl:template>

<xsl:template name="processAttribute">
<tr><td><xsl:value-of select="name"/></td>
<td><input type="text" name="{name}" value="{value}"/></td>
<td><xsl:value-of select="description"/></td>
</tr>
</xsl:template>

</xsl:stylesheet>