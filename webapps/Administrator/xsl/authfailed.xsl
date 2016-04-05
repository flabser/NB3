<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
	<xsl:output method="html" encoding="utf-8"/>
	<xsl:template match="/content">
		<html>
			<head>
				<title>SmartDoc - Ошибка</title>						
				<link rel="stylesheet" href="sdcss/main.css"/>	
				<link rel="stylesheet" href="sdcss/actionbar.css"/>			
			</head>
			<body>
				<font size="6">Ошибка</font>&#xA0;
				<br/>				
				<hr color="#6790b3"/>
				<br/>
				<font size="3">
				<xsl:choose>
					<xsl:when test="@type = 'authfailed'">Ошибка авторизации</xsl:when>
				</xsl:choose>	
				</font>
				<br/>
				<div style="font-size:1.5em">	
					Пользователь не найден в структуре организации
				</div>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>