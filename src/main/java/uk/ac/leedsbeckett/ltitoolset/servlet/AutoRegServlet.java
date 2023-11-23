/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.ac.leedsbeckett.ltitoolset.servlet;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import uk.ac.leedsbeckett.lti.config.ClientLtiConfigurationKey;
import uk.ac.leedsbeckett.lti.registration.ConsumerConfiguration;
import uk.ac.leedsbeckett.lti.registration.LtiToolConfigurationMessage;
import uk.ac.leedsbeckett.lti.registration.LtiToolRegistration;
import uk.ac.leedsbeckett.ltitoolset.Tool;
import uk.ac.leedsbeckett.ltitoolset.ToolCoordinator;
import uk.ac.leedsbeckett.ltitoolset.ToolKey;
import uk.ac.leedsbeckett.ltitoolset.backchannel.BackchannelOwner;
import uk.ac.leedsbeckett.ltitoolset.backchannel.LtiAutoRegistrationBackchannel;
import uk.ac.leedsbeckett.ltitoolset.backchannel.LtiAutoRegistrationBackchannelKey;
import uk.ac.leedsbeckett.ltitoolset.config.ClientLtiConfigurationImpl;
import uk.ac.leedsbeckett.ltitoolset.config.LtiConfigurationImpl;

/**
 *
 * @author jon
 */
public class AutoRegServlet extends HttpServlet implements BackchannelOwner
{
  static final Logger logger = Logger.getLogger(AutoRegServlet.class.getName() );

  @Override
  public void destroy()
  {
    ToolCoordinator toolCoord  = ToolCoordinator.get( getServletContext() );
    toolCoord.releaseBackchannels( this );
  }
  
  
  
  @Override
  protected void doGet( HttpServletRequest req, HttpServletResponse resp )
          throws ServletException, IOException
  {
    logger.info( "AutoReg" );
    logger.info( req.getRemoteAddr() );
    logger.info( req.getRemoteHost() );
    logger.info( req.getMethod() );
    logger.info( req.getContextPath() );
    logger.info( req.getPathInfo() );
    logger.info( req.getQueryString() );
    
    String[] parts = req.getPathInfo().split( "/" );
    for ( int i=0; i<parts.length; i++ )
      logger.log(Level.INFO, "Path part {0} = {1}", new Object[]{i, parts[i]} );
    
    if ( parts.length != 4 )
    {
      resp.sendError( 404, "Invalid URL contains wrong number of elements in path." );
      return;
    }
    
    String action   = parts[1];
    String tooltype = parts[2];
    String toolid   = parts[3];
    if ( null == action )
    {
      resp.sendError( 404, "Invalid URL contains null 'action'." );
      return;      
    }
            
    switch ( action )
    {
      case "init":
        actionInit( req, resp, tooltype, toolid );
        break;
      case "confirm":
        actionConfirm( req, resp );
        break;
      default:
        resp.sendError( 404, "Unknown 'action'." );
        return;      
    }
    
  }

  protected void actionInit( HttpServletRequest req, HttpServletResponse resp, String tooltype, String toolid )
          throws ServletException, IOException
  {    
    ToolCoordinator toolCoord  = ToolCoordinator.get( req.getServletContext() );
    if ( toolCoord  == null ) { resp.sendError( 500, "Cannot find tool manager." ); return; }
    Tool tool = toolCoord.getTool( tooltype, toolid );
    if ( tool == null ) { resp.sendError( 500, "Cannot find tool based on tool type and id in path." ); return; }
    
    LtiConfigurationImpl lticonfig = toolCoord.getLtiConfiguration();
    
    String openidcurl = req.getParameter( "openid_configuration" );
    logger.info( openidcurl );
    
    // This is set in moodle but for blackboard there is no token
    // because authentication is encoded in the openidcurl
    String token = req.getParameter( "registration_token" );
    logger.info( token );
    
    if ( StringUtils.isBlank( openidcurl ) )
    {
      resp.sendError( 500, "Openid config URL missing in page request." );
      return;
    }
    
    // We need a backchannel to the LTI Tool consumer
    LtiAutoRegistrationBackchannelKey key = new LtiAutoRegistrationBackchannelKey( req.getRemoteHost(), openidcurl );
    LtiAutoRegistrationBackchannel backchannel = (LtiAutoRegistrationBackchannel)toolCoord.getBackchannel( this, key, null );
    if ( backchannel == null )
    {
      resp.sendError( 500, "Unable to create backchannel to LTI tool consumer." );
      return;
    }
    
    // Fetch the LTI tool consumer's auto registration configuration
    ConsumerConfiguration consumerconfig = backchannel.getOpenIdConfiguration();
    if ( consumerconfig == null )
    {
      resp.sendError( 500, "Unable to fetch the LTI tool consumer's configuration." );
      return;
    }
    
    // Dreadful bodge to fix "issuer" for Blackboard!!!!!
    if ( "https://developer.blackboard.com/".equals( consumerconfig.getIssuer() ) )
      consumerconfig.setIssuer( "https://blackboard.com" );
    
    // Set up an Lti Tool registration object
    LtiToolRegistration toolregin = toolCoord.createToolRegistration( tool.getTitle() );
    
    // Looks like Blackboard needs this field to be present
    toolregin.getLtiToolConfiguration().setMessages( new LtiToolConfigurationMessage[0] );
    // Experimenting with 'random' domain
    toolregin.getLtiToolConfiguration().setDomain( "doobeedoo" );
    
    // Post it to the LTI tool consumer's configured registration endpoint
    // It should be sent back with some fields filled in.
    LtiToolRegistration toolregout = backchannel.postToolRegistration( consumerconfig.getRegistrationEndpoint(), token, toolregin );
    if ( toolregout == null )
    {
      resp.sendError( 500, "Registration with tool consumer failed." );
      return;
    }
  
    ClientLtiConfigurationKey lticlientkey = new ClientLtiConfigurationKey( consumerconfig.getIssuer(),  toolregout.getClientId() );
    ClientLtiConfigurationImpl lticlient = lticonfig.get( lticlientkey, true );
    if ( lticlient == null )
    {
      resp.sendError( 500, "Unable to fetch or create LTI client configuration." );
      return;
    }
    // Now set up the LTI client config based on registration data
    lticlient.setToolId( toolid );
    lticlient.setToolType( tooltype );
    lticlient.setAuthJwksUrl( consumerconfig.getJwksUri() );
    lticlient.setAuthLoginUrl( consumerconfig.getAuthorizationEndpoint() );
    lticlient.setAuthTokenUrl( consumerconfig.getTokenEndpoint() );
    lticonfig.update( lticlient );
    
    toolCoord.getJwksStore().registerUri( consumerconfig.getJwksUri() );

    
    resp.setContentType( "text/html" );
    ServletOutputStream out = resp.getOutputStream();
    out.println( "<html><body>" );
    out.println( "<p>Not Yet Implemented</p>" );
    out.println( "<h4>OpenID Config</h4><pre>");
    out.println( req.getParameter( "openid_configuration" ) );
    out.println( "</pre><h4>Registration Token</h4><pre>");
    out.println( req.getParameter( "registration_token" ) );
    out.println( "</pre><h4>LTI Consumer Configuration Issuer</h4><pre>");
    out.println( consumerconfig.getIssuer() );
    out.println( "</pre><h4>LTI Consumer Configuration Resgistration Endpoint</h4><pre>");
    out.println( consumerconfig.getRegistrationEndpoint() );
    out.println( "</pre>" );
    
    if ( toolregout != null )
    {
      out.println( "<h4>LTI Tool Reg Client ID</h4><pre>");
      out.println( toolregout.getClientId() );
      out.println( "</pre>" );      
    }
    
    out.println( "<body><html>" );
  }
  
  protected void actionConfirm( HttpServletRequest req, HttpServletResponse resp )
          throws ServletException, IOException
  {
    resp.setContentType( "text/html" );
    ServletOutputStream out = resp.getOutputStream();
    out.println( "<html><body>" );
    out.println( "<p>Not Yet Implemented</p>" );
    out.println( "<body><html>" );
  }
  
  
}
