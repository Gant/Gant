<%--********************************************************************************
* CruiseControl, a Continuous Integration Toolkit
* Copyright (c) 2001, ThoughtWorks, Inc.
* 651 W Washington Ave. Suite 600
* Chicago, IL 60661 USA
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
*
*     + Redistributions of source code must retain the above copyright
*       notice, this list of conditions and the following disclaimer.
*
*     + Redistributions in binary form must reproduce the above
*       copyright notice, this list of conditions and the following
*       disclaimer in the documentation and/or other materials provided
*       with the distribution.
*
*     + Neither the name of ThoughtWorks, Inc., CruiseControl, nor the
*       names of its contributors may be used to endorse or promote
*       products derived from this software without specific prior
*       written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
* "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
* LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
* A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR
* CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
********************************************************************************--%>
<%@ taglib uri="/WEB-INF/cruisecontrol-jsp11.tld" prefix="cruisecontrol"%>
<cruisecontrol:xsl xslFile="/xsl/header.xsl"/>

<cruisecontrol:artifactsLink>
  <table width="98%" border="0" cellspacing="0" cellpadding="2" align="center">
    <tr><td class="header-label"><!--<a href="<%= artifacts_url %>">--><a href="http://build.canoo.com/gant/artifacts/">Build Artifacts</a></td></tr>
  </table>
</cruisecontrol:artifactsLink>

<cruisecontrol:xsl xslFile="/xsl/maven.xsl"/>
<p>
<cruisecontrol:xsl xslFile="/xsl/checkstyle.xsl"/>
<p>
<cruisecontrol:xsl xslFile="/xsl/compile.xsl"/>
<p>
<cruisecontrol:xsl xslFile="/xsl/javadoc.xsl"/>
<p>
<cruisecontrol:xsl xslFile="/xsl/unittests.xsl"/>
<p>
<cruisecontrol:xsl xslFile="/xsl/modifications.xsl"/>
<p>
<cruisecontrol:xsl xslFile="/xsl/distributables.xsl"/>
