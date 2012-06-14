package org.eclipse.rap.nebula.widgets.grid.snippets.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.rap.nebula.widgets.grid.snippets.GridSnippet;
import org.eclipse.rwt.application.Application;
import org.eclipse.rwt.application.Application.OperationMode;
import org.eclipse.rwt.application.ApplicationConfiguration;
import org.eclipse.rwt.client.WebClient;


public class GridSnippetsApplication implements ApplicationConfiguration {

  public void configure( Application application ) {
    Map<String, String> properties = new HashMap<String, String>();
    properties.put( WebClient.PAGE_TITLE, "Nebula Grid Snippets" );
    application.addEntryPoint( "/snippet1", GridSnippet.class, properties );
    application.setOperationMode( OperationMode.SWT_COMPATIBILITY );
  }

}
