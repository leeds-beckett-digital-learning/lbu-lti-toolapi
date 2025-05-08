/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.ac.leedsbeckett.ltitoolset.deeplinking;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import uk.ac.leedsbeckett.lti.messages.LtiMessageDeepLinkingResponse;
import uk.ac.leedsbeckett.lti.resourcelink.LtiResourceLink;
import uk.ac.leedsbeckett.ltitoolset.Tool;
import uk.ac.leedsbeckett.ltitoolset.ToolSetLtiState;
import uk.ac.leedsbeckett.ltitoolset.annotations.ToolInstantiationLevel;
import uk.ac.leedsbeckett.ltitoolset.deeplinking.data.DeepLinkingOptions;
import uk.ac.leedsbeckett.ltitoolset.deeplinking.data.DeepLinkingSelection;
import uk.ac.leedsbeckett.ltitoolset.websocket.ToolEndpoint;
import uk.ac.leedsbeckett.ltitoolset.websocket.HandlerAlertException;
import uk.ac.leedsbeckett.ltitoolset.websocket.ToolMessage;
import uk.ac.leedsbeckett.ltitoolset.websocket.ToolMessageDecoder;
import uk.ac.leedsbeckett.ltitoolset.websocket.ToolMessageEncoder;
import uk.ac.leedsbeckett.ltitoolset.websocket.annotations.EndpointJavascriptProperties;
import uk.ac.leedsbeckett.ltitoolset.websocket.annotations.EndpointMessageHandler;
import uk.ac.leedsbeckett.ltitoolset.annotations.ToolFacet;
import uk.ac.leedsbeckett.ltitoolset.deeplinking.data.ToolInformation;

/**
 * A web socket endpoint that helps a deeplinking JSP page to create a deep link in a
 * platform.
 * 
 * @author jon
 */
@ServerEndpoint( 
        value="/socket/deeplinking", 
        decoders=ToolMessageDecoder.class, 
        encoders=ToolMessageEncoder.class )
@EndpointJavascriptProperties(
        module="deeplinking",
        prefix="Deep",
        messageEnum="uk.ac.leedsbeckett.ltitoolset.deeplinking.DeepServerMessageName" )
public class DeepLinkingEndpoint extends ToolEndpoint
{
  static final Logger logger = Logger.getLogger(DeepLinkingEndpoint.class.getName() );


  DeepLinkingLaunchState deepstate;
  
  /**
   * Most work is done by the super-class. This sub-class fetches references
   * to tool specific objects.
   * 
   * @param session The session this endpoint belongs to.
   * @throws IOException If opening should be aborted.
   */
  @OnOpen
  @Override
  public void onOpen(Session session) throws IOException
  {
    super.onOpen( session );
    logger.log( Level.SEVERE, "open" );
    deepstate = (DeepLinkingLaunchState) getState().getToolLaunchState();
    if ( deepstate == null )
      throw new IOException( "Deep state missing. " + getState().getId() );
  }
  
  /**
   * Simply invokes super-class.
   * 
   * @param session The session this endpoint belongs to.
   * @throws IOException Unlikely to be thrown.
   */
  @OnClose
  @Override
  public void onClose(Session session) throws IOException
  {
    super.onClose( session );
    logger.log( Level.SEVERE, "close" );
  }

  /**
   * At present justs puts a line in the log.
   * 
   * @param session The session this endpoint belongs to.
   * @param throwable The throwable that caused the issue.
   */
  @OnError
  public void onError(Session session, Throwable throwable)
  {
    logger.log( Level.SEVERE, "Web socket error.", throwable );
  }  

  @OnMessage
  @Override
  public void onMessage(Session session, String text) throws IOException
  {
    super.onMessage( session, text );
  }  

  /**
   * Client requested the list of tools/facets that can be instantiated.
   * 
   * @param session The session this endpoint belongs to.
   * @param message The incoming message from the client end.
   * @throws IOException Indicates failure to process. 
   * @throws uk.ac.leedsbeckett.ltitoolset.websocket.HandlerAlertException Indicates problem with handling the incoming message.
   */
  @EndpointMessageHandler()
  public void handleGetOptions( Session session, ToolMessage message )
          throws IOException, HandlerAlertException
  {
    logger.log( Level.INFO, "Rxed GetOptions message." );
    
    DeepLinkingOptions options = new DeepLinkingOptions();

    for ( String toolId : toolCoordinator.getToolIds() )
    {
      Tool tool = toolCoordinator.getTool( toolId );      
      ToolInformation toolinfo = tool.getDeepLinkingToolInformation( deepstate );
      if ( toolinfo.getFacets().length > 0 )
        options.addToolInformation( toolinfo );
    }
    
    sendToolMessage( session, new ToolMessage( message.getId(), DeepServerMessageName.Options, options ) );    
  }  

  /**
   * Client specified a tool/facet and properties and wishes to receive a JWT encoded
   * deep linking message which it can use to create the deep link.
   * 
   * @param session The session this endpoint belongs to.
   * @param message The incoming message from the client end.
   * @param selection Input to link to/create tool resource.
   * @throws IOException Indicates failure to process. 
   * @throws uk.ac.leedsbeckett.ltitoolset.websocket.HandlerAlertException Indicates problem with handling the incoming message.
   */
  @EndpointMessageHandler()
  public void handleMakeLink( Session session, ToolMessage message, DeepLinkingSelection selection )
          throws IOException, HandlerAlertException
  {
    ToolSetLtiState s = this.getState();
    if ( selection == null )
      throw new HandlerAlertException( "Cannot process null tool selection.", message.getId() );
    if ( selection.getToolResourceId() != null )
      throw new HandlerAlertException( "Deep linking to an existing tool resource is not supported.", message.getId() );
    // Tool type now ignored
    if ( selection.getToolId() == null )
      throw new HandlerAlertException( "Cannot process tool selection with null tool id.", message.getId() );
    
    Tool tool = toolCoordinator.getTool( selection.getToolId() );
    if ( tool == null )
      throw new HandlerAlertException( "Unknown tool. id = " + selection.getToolId(), message.getId() );

    String effectiveFacetId = selection.getToolFacetId();
    if ( effectiveFacetId == null )
      effectiveFacetId = tool.getDefaultFacetId();
    
    if ( effectiveFacetId == null )
      throw new HandlerAlertException( "No tool facet id specified and no default available.", message.getId() );

    ToolFacet tf = toolCoordinator.getToolFacet( selection.getToolId(), effectiveFacetId );
    
    if ( !tool.allowDeepLink( effectiveFacetId, deepstate ) )
      throw new HandlerAlertException( "Selected facet of tool doesn't support deep linking. id = " + selection.getToolId() + " facet = " + selection.getToolFacetId(), message.getId() );

    ToolInstantiationLevel level = tf.instantiationLevel();
    
    // Do we need to create a tool resource?
    String toolResourceId = null;
    if ( level == ToolInstantiationLevel.TOOL_RESOURCE    )
    {
      toolResourceId = UUID.randomUUID().toString();
      // register this ID with the tool so it can create
      // and store appropriate data.
      if ( !tool.createToolResource( toolResourceId, effectiveFacetId, deepstate ) )
        throw new HandlerAlertException( "Selected facet of tool failed to create new resource.  id = " + selection.getToolId() + " facet = " + selection.getToolFacetId(), message.getId() );
    }
    
    
    LtiMessageDeepLinkingResponse deepmessage = new LtiMessageDeepLinkingResponse( 
            toolCoordinator.getKeyId(), 
            toolCoordinator.getPrivateKey(),
            toolCoordinator.getPublicKey() );

    deepmessage.addClaim( "iss", s.getClientId() );
    deepmessage.addClaim( "aud", deepstate.platform_issuer );
    deepmessage.addClaim( "exp", System.currentTimeMillis()/1000+5000 );
    deepmessage.addClaim( "iat", System.currentTimeMillis()/1000 );
    deepmessage.addClaim( "https://purl.imsglobal.org/spec/lti/claim/message_type", "LtiDeepLinkingResponse" );
    deepmessage.addClaim( "https://purl.imsglobal.org/spec/lti/claim/version", "1.3.0" );
    deepmessage.addClaim( "https://purl.imsglobal.org/spec/lti/claim/deployment_id", deepstate.deployment_id );
    if ( deepstate.data != null )
      deepmessage.addClaim( "https://purl.imsglobal.org/spec/lti-dl/claim/data", deepstate.data );
    deepmessage.addClaim( "https://purl.imsglobal.org/spec/lti-dl/claim/msg", "Request from LTI Tool to add deep link." );
    deepmessage.addClaim( "https://purl.imsglobal.org/spec/lti-dl/claim/log", "Request from LTI Tool to add deep link." );

    ArrayList<LtiResourceLink> reslinks = new ArrayList<>();
    LtiResourceLink reslink = new LtiResourceLink();
    reslink.setTitle( selection.getToolResourceTitle() );
    reslink.setText( selection.getToolResourceDescription() );
    reslink.setUrl( toolCoordinator.getLaunchUrl() );
    reslink.setIframe( tool.getDeepLinkingIFrameOptions() );
    reslink.putCustom( "digles.leedsbeckett.ac.uk#tool_name",  selection.getToolId()      );
    reslink.putCustom( "digles.leedsbeckett.ac.uk#tool_facet", selection.getToolFacetId() );
    // For most instantiation levels the tool will know what
    // to give the user on launch based on standard LTI claims
    // such as the platform, the course, the platform resource
    // etc. However, for the TOOL_RESOURCE level the deep link
    // needs a custom claim that identifies the tool's resource
    // which should be shown. This will be passed to the tool
    // facet which will decide how to interpret it.
    if ( level == ToolInstantiationLevel.TOOL_RESOURCE )
      reslink.putCustom( "digles.leedsbeckett.ac.uk#tool_resource_id", toolResourceId );      
    reslinks.add( reslink );
    deepmessage.addClaim( "https://purl.imsglobal.org/spec/lti-dl/claim/content_items", reslinks );       

    String jwt = deepmessage.build();
    logger.log(Level.INFO, "JWT = {0}", jwt);
    sendToolMessage( session, new ToolMessage( message.getId(), DeepServerMessageName.Jwt, jwt ) );    
  }

  
  @Override
  public void processHandlerAlert( Session session, HandlerAlertException haex )
          throws IOException
  {
    sendToolMessage( session, new ToolMessage( haex.getMessageId(), DeepServerMessageName.Alert, haex.getMessage() ) );    
  }
  
}
