package org.codehaus.gant.tests.bugs.subPackage

import org.codehaus.gant.GantBinding

class GANT_29_SampleTool {
  private final Map<String,String> properties = [ name : '' ]
  GANT_29_SampleTool ( GantBinding binding ) { properties.binding = binding }
  public getProperty ( String name ) { properties[name] }
  public void setProperty ( String name , value ) { properties[name] = value }  
}
