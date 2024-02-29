/*
 * Copyright 2022 Leeds Beckett University.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.leedsbeckett.ltitoolset.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import uk.ac.leedsbeckett.lti.claims.LtiClaims;
import uk.ac.leedsbeckett.lti.claims.LtiDeepLinkingSettings;
import uk.ac.leedsbeckett.lti.config.ClientLtiConfiguration;
import uk.ac.leedsbeckett.lti.config.ClientLtiConfigurationKey;
import uk.ac.leedsbeckett.lti.config.LtiConfiguration;
import uk.ac.leedsbeckett.lti.messages.LtiMessageDeepLinkingResponse;
import uk.ac.leedsbeckett.lti.servlet.LtiLaunchServlet;
import uk.ac.leedsbeckett.lti.state.LtiStateStore;
import uk.ac.leedsbeckett.ltitoolset.LaunchDisallowedException;
import uk.ac.leedsbeckett.ltitoolset.Tool;
import uk.ac.leedsbeckett.ltitoolset.ToolCoordinator;
import uk.ac.leedsbeckett.ltitoolset.ToolKey;
import uk.ac.leedsbeckett.ltitoolset.ToolLaunchState;
import uk.ac.leedsbeckett.ltitoolset.ToolSetLtiState;
import uk.ac.leedsbeckett.ltitoolset.annotations.ToolMapping;
import uk.ac.leedsbeckett.ltitoolset.config.ClientLtiConfigurationImpl;
import uk.ac.leedsbeckett.ltitoolset.config.LtiConfigurationImpl;
import uk.ac.leedsbeckett.ltitoolset.deeplinking.DeepLinkingLaunchState;


/**
 * This API's implementation of the LTI launch servlet. This implementation
 * will map onto a url pattern.
 * 
 * @author jon
 */
public class ToolLaunchServlet extends LtiLaunchServlet<ToolSetLtiState>
{
  static final Logger logger = Logger.getLogger(ToolLaunchServlet.class.getName() );
  
  /**
   * The parent class calls this method after it has processed and validated 
   * the launch request.The job here is to look at the claims in the LTI
   * launch and decide how to prepare state and how to forward the user to
   * the servlet or JSP page that actually implements the tool.
   * 
   * @param lticlaims The validated LTI claims for this launch request.
   * @param state The LTI state object.
   * @param request The HTTP request.
   * @param response The HTTP response.
   * @throws ServletException If there is an internal problem forwarding the user's browser.
   * @throws IOException If the network connection is broken while sending the forwarding response.
   */
  @Override
  protected void processLaunchRequest( LtiClaims lticlaims, ToolSetLtiState state, HttpServletRequest request, HttpServletResponse response )
          throws ServletException, IOException
  {
    logger.info( "Processing Launch Request" );
    ToolCoordinator toolManager = ToolCoordinator.get( request.getServletContext() );
    if ( toolManager == null ) { response.sendError( 500, "Cannot find tool manager." ); return; }
    
    try
    {
      toolManager.isPlatformAllowedLaunch( lticlaims, state );
    }
    catch ( LaunchDisallowedException ex )
    {
      Logger.getLogger( ToolLaunchServlet.class.getName() ).log( Level.SEVERE, null, ex );
      response.sendError( 500, "Launch request from " + lticlaims.getIssuer() + " has been disallowed by this tool's configuration. Reason: " + ex.getMessage() );
      return;
    }
    
    
    String toolid = lticlaims.getLtiCustom().getAsString( "digles.leedsbeckett.ac.uk#tool_name" );
    String tooltype = lticlaims.getLtiCustom().getAsString( "digles.leedsbeckett.ac.uk#tool_type" );

    ToolKey toolKey = new ToolKey( tooltype, toolid );
    Tool tool = toolManager.getTool( toolKey );
    ToolMapping toolMapping = toolManager.getToolMapping( toolKey );
    
    if ( tool != null )
    {
      state.setToolKey( toolKey );
      ToolLaunchState toolstate = tool.supplyToolLaunchState();
      tool.initToolLaunchState( toolstate, lticlaims, state );
      
      // If the state is changed it needs to be updated in the cache.
      state.setToolLaunchState( toolstate );
      getLtiStateStore( request.getServletContext() ).updateState( state );
      
      if ( logger.isLoggable( Level.FINE ) )
      {
        ObjectMapper mapper = new ObjectMapper();
        logger.fine( "Launch Claims" );
        logger.fine( mapper.writerWithDefaultPrettyPrinter().writeValueAsString( lticlaims ) );
        
        if ( lticlaims.getLtiNamesRoleService() != null )
          logger.fine( "Contains claim for names/role service at " + lticlaims.getLtiNamesRoleService().getContextMembershipsUrl() );
        
        logger.fine( "Launch State" );
        logger.fine( mapper.writerWithDefaultPrettyPrinter().writeValueAsString( state ) );
      }
            
      logger.fine( "Forwarding to tool index page." );
      StringBuilder sb = new StringBuilder();
      sb.append( request.getContextPath() )
        .append( toolMapping.launchURI()     )
        .append( "?state_id="             )
        .append( state.getId()            );
      response.sendRedirect( response.encodeRedirectURL( sb.toString() ) );
      return;
    }
    
    
    response.setContentType( "text/html;charset=UTF-8" );
    try (  PrintWriter out = response.getWriter() )
    {
      /* TODO output your page here. You may use following sample code. */
      out.println( "<!DOCTYPE html>" );
      out.println( "<html>" );
      out.println( "<head>" );
      out.println( "<title>Servlet LaunchServlet</title>" );      
      out.println( "<style>" );
      out.println( "li { padding: 1em 1em 1em 1em; }" );
      out.println( "</style>" );
      out.println( "</head>" );
      out.println( "<body>" );
      out.println( "<h1>Servlet LaunchServlet at " + request.getContextPath() + "</h1>" );

      out.println( "<p>The LTI Launch was not configured properly. The following may help understand what happened.</p> " );

      out.println( "<h2>About the Launch Request</h2>" );
      out.println( "<ul>" );
      out.println( "<li>Tool platform guid<br/>" + lticlaims.getLtiToolPlatform().getGuid() + "</li>" );
      out.println( "<li>Tool platform url<br/>"  + lticlaims.getLtiToolPlatform().getUrl()  + "</li>" );
      out.println( "<li>Context label<br/>"      + lticlaims.getLtiContext().getLabel()     + "</li>" );
      out.println( "<li>Context title<br/>"      + lticlaims.getLtiContext().getTitle()     + "</li>" );
      String type = lticlaims.getLtiContext().getType( 0 );
      if ( type != null )
        out.println( "<li>Context type<br/>"     + type                        + "</li>" );
      out.println( "</ul>" );
      
      
      out.println( "<h2>Technical breakdown of launch request</h2>" );
      out.println( "<table>");
      out.println( "<tr><th>toolname</th><td>" + toolid + "</td></tr>" );
      out.println( "<tr><th>tooltype</th><td>" + tooltype + "</td></tr>" );
      out.println( "</table>");

      out.println( "<h3>LTI Claims</h3>" );
      
      out.println( "<pre>" );
      ArrayList<String> keylist = new ArrayList<>();
      for ( String k :  lticlaims.keySet() )
        keylist.add( k );
      keylist.sort( Comparator.comparing( String::toString ) );
      for ( String k : keylist )
        out.println( k + " = " + lticlaims.get( k ) + "\n" );
      out.println( "</pre>" );
      

      out.println( "</body>" );
      out.println( "</html>" );
    }
  }

  
  /**
   * The parent class calls this method after it has processed and validated 
   * the deep linking request.  
   * 
   * @param lticlaims The validated LTI claims for this launch request.
   * @param state The LTI state object.
   * @param request The HTTP request.
   * @param response The HTTP response.
   * @throws ServletException If there is an internal problem forwarding the user's browser.
   * @throws IOException If the network connection is broken while sending the forwarding response.
   */
  @Override
  protected void processDeepLinkRequest( LtiClaims lticlaims, ToolSetLtiState state, HttpServletRequest request, HttpServletResponse response )
          throws ServletException, IOException
  {
    logger.info( "Processing Deep Link Request" );
    ToolCoordinator toolManager = ToolCoordinator.get( request.getServletContext() );
    if ( toolManager == null ) { response.sendError( 500, "Cannot find tool manager." ); return; }
    LtiDeepLinkingSettings deepsettings = lticlaims.getLtideeplinkingsettings();
    if ( deepsettings == null ) { response.sendError( 500, "No LTI deep linking settings claim in the launch." ); return; }

    try
    {
      toolManager.isPlatformAllowedDeepLink( lticlaims, state );
    }
    catch ( LaunchDisallowedException ex )
    {
      Logger.getLogger( ToolLaunchServlet.class.getName() ).log( Level.SEVERE, null, ex );
      response.sendError( 500, "Deep linking request from " + lticlaims.getIssuer() + " has been disallowed by this tool's configuration. Reason: " + ex.getMessage() );
      return;
    }
    
    LtiDeepLinkingSettings deep = lticlaims.getLtideeplinkingsettings();
    if ( deep == null )
    {
      response.sendError( 500, "No deep linking settings were in the request." );
      return;
    }
    
    
    DeepLinkingLaunchState deepstate = new DeepLinkingLaunchState();
    state.setToolLaunchState( deepstate );
    deepstate.deepLinkReturnUrl = deepsettings.getDeepLinkReturnUrl();
    deepstate.platform_issuer   = lticlaims.getIssuer();
    deepstate.data              = deepsettings.getData();
    deepstate.deployment_id     = lticlaims.get( "https://purl.imsglobal.org/spec/lti/claim/deployment_id" );    
    deepstate.rc                = lticlaims.getLtiRoles();
    getLtiStateStore( request.getServletContext() ).updateState( state );

    String sid = state.getId();
    logger.info( "Saved state which has id " + sid );
    if ( getLtiStateStore( request.getServletContext() ).getState( sid ) == null )
      logger.warning( "The state is not in the state store!" );
    
    StringBuilder sb = new StringBuilder();
    sb.append( request.getContextPath() )
      .append( "/deeplinking/index.jsp"  )   // TODO this should be configured by tool set implementation
      .append( "?state_id="              )
      .append( state.getId()             );
    response.sendRedirect( response.encodeRedirectURL( sb.toString() ) );
  }
  
  /**
   * This implementation ensures that the library code knows how to store
   * LTI state.
   * 
   * @param context The servlet context in whose attributes the store can be found.
   * @return The store.
   */
  @Override
  protected LtiStateStore<ToolSetLtiState> getLtiStateStore( ServletContext context )
  {
    ToolCoordinator toolManager = ToolCoordinator.get( context );
    return toolManager.getLtiStateStore();
  }

  /**
   * This implementation ensures that the library code knows the configuration.
   * 
   * @param context The servlet context in whose attributes the store can be found.
   * @return The configuration.
   */  
  @Override
  protected LtiConfiguration getLtiConfiguration( ServletContext context )
  {
    return ToolCoordinator.get( context ).getLtiConfiguration();
  }  
}
