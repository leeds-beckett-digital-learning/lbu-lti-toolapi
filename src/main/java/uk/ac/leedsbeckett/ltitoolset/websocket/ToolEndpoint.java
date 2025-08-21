/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.ac.leedsbeckett.ltitoolset.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.DecodeException;
import javax.websocket.SendHandler;
import javax.websocket.SendResult;
import javax.websocket.Session;
import uk.ac.leedsbeckett.lti.LtiException;
import uk.ac.leedsbeckett.ltitoolset.ToolCoordinator;
import uk.ac.leedsbeckett.ltitoolset.ToolLaunchState;
import uk.ac.leedsbeckett.ltitoolset.ToolSetLtiState;
import uk.ac.leedsbeckett.ltitoolset.backchannel.Backchannel;
import uk.ac.leedsbeckett.ltitoolset.backchannel.BackchannelKey;
import uk.ac.leedsbeckett.ltitoolset.backchannel.BackchannelOwner;
import uk.ac.leedsbeckett.ltitoolset.backchannel.OAuth2Token;
import uk.ac.leedsbeckett.ltitoolset.blobex.BlobExException;
import uk.ac.leedsbeckett.ltitoolset.blobex.BlobExchanger;
import uk.ac.leedsbeckett.ltitoolset.websocket.annotations.EndpointMessageHandler;
import uk.ac.leedsbeckett.ltitoolset.websocket.annotations.HandlerPromisesReply;
import uk.ac.leedsbeckett.ltitoolset.websocket.control.ControlClientConfiguration;
import uk.ac.leedsbeckett.ltitoolset.websocket.control.ControlErrorReply;
import uk.ac.leedsbeckett.ltitoolset.websocket.control.ControlMessageName;

/**
 * This is the superclass of all the web socket endpoint classes.
 * Some old functionality has been pushed down to MultitonToolEndpoint
 * because it is specific to tools that are multi-instantiated.
 * 
 * @author jon
 */
public abstract class ToolEndpoint implements BackchannelOwner
{
  static final Logger logger = Logger.getLogger(ToolEndpoint.class.getName() );

  /**
   * A map of maps to keep track of handlers in implementations.
   */
  static final HashMap<Class<? extends ToolEndpoint>,HashMap<String,HandlerMethodRecord>> classHandlerMaps = new HashMap<>();

  /**
   * For a given sub-class of ToolEndpoint find a map of message names against handlers.
   * 
   * @param c The class.
   * @return The corresponding map.
   */
  public static HashMap<String,HandlerMethodRecord> getHandlerMap( Class<? extends ToolEndpoint> c )
  {
    synchronized ( classHandlerMaps )
    {
      HashMap<String,HandlerMethodRecord> handlerMap = classHandlerMaps.get( c );
      if ( handlerMap == null )
      {
        handlerMap = createHandlerMap( c );
        classHandlerMaps.put( c, handlerMap );
      }
      return handlerMap;
    }
  }
  
  /**
   * Use reflection and annotations to build a map of handlers for a specific subclass of ToolEndpoint.
   * 
   * @param clasz The specific class.
   * @return The map.
   */
  static HashMap<String,HandlerMethodRecord> createHandlerMap( Class<? extends ToolEndpoint> clasz )
  {
    HashMap<String,HandlerMethodRecord> handlerMap = new HashMap<>();
    
    for ( Method method : clasz.getMethods() )
    {
      //logger.log( Level.INFO, "Checking method {0}", method.getName() );
      for ( EndpointMessageHandler handler : method.getAnnotationsByType( EndpointMessageHandler.class ) )
      {
        boolean replyPromised = method.getAnnotationsByType( HandlerPromisesReply.class ).length > 0;
        
        //logger.log(Level.INFO, "Method has EndpointMessageHandler annotation and name = {0}", handler.name());
        Class<?>[] classarray = method.getParameterTypes();
        //logger.log(Level.INFO, "Method parameter count = {0}", classarray.length);
        if ( ( classarray.length == 2 || classarray.length == 3 ) && 
                classarray[0].equals( Session.class ) &&
                classarray[1].equals( ToolMessage.class ) )
        {
          //logger.log(Level.INFO, "Parameters match signature and second parameter class is {0}", classarray.length == 3?classarray[2]:"not present");
          if ( classarray.length == 3 )
            ToolMessageTypeSet.addType( classarray[2].getName() );
          String name = handler.name();
          if ( name == null || name.length() == 0 )
          {
            name = method.getName();
            if ( name.startsWith( "handle") )
              name = name.substring( "handle".length() );
            char c = name.charAt( 0 );
            if ( Character.isAlphabetic( c ) && Character.isLowerCase( c ) )
              name = "" + Character.toUpperCase( c ) + name.substring( 1 );
          }
          //logger.log( Level.INFO, "Using message name = {0}", name );
          if ( handlerMap.containsKey( name ) )
            logger.log( Level.SEVERE, "Message handlers with duplicate names = ", name );
          else
          {
            HandlerMethodRecord record = new HandlerMethodRecord( 
                          name, 
                          method, 
                          classarray.length == 3?classarray[2]:null,
                          replyPromised );
            //logger.log( Level.INFO, "Javascript:" );
            //logger.log( Level.INFO, record.getJavaScriptClass() );
            handlerMap.put( name, record );
          }
        }
      }
    }
    //logger.log( Level.INFO, "Javascript:" );
    //logger.log( Level.INFO, getJavaScript() );
    
    return handlerMap;
  }

  private static long serial = 0x1000;
  
  // We are using default configuration for instantiation which means
  // there should be one instance per session and onOpen should only be
  // called once for an instance of this class. So, it should be safe
  // to store the session here.
  Session session = null;
  ControlClientConfiguration clientConfiguration;  
  
  String uniqueid;
  String stateid;
  ToolSetLtiState state;
  ToolLaunchState toolState;
  protected ToolCoordinator toolCoordinator;
  protected BlobExchanger blobEx;

  String platformHost;
  
  public ToolEndpoint()
  {
    uniqueid = Long.toHexString( serial++ );
    logger.log(Level.FINE, "Created Instance {0}", uniqueid);
    logger.log(Level.FINE, "Class {0}", this.getClass().toString() );
  }

  public boolean indexByPlatformResource()
  {
    return false;
  }
  
  public boolean indexByToolResource()
  {
    return false;
  }
  
  /**
   * After the endpoint is open, get the LTI state ID.
   *
   * @return The ID as string.
   */
  public String getStateid()
  {
    return stateid;
  }

  /**
   * After the endpoint is open, get the LTI state object.
   *
   * @return The state.
   */
  public ToolSetLtiState getState()
  {
    return state;
  }
  
  public ToolLaunchState getToolState()
  {
    return toolState;
  }

  /**
   * Get the ToolCoordinator for the tool-set that this endpoint is working in.
   *
   * @return The ToolCoordinator instance.
   */
  public ToolCoordinator getToolCoordinator()
  {
    return toolCoordinator;
  }

  public String getPlatformHost()
  {
    return platformHost;
  }

  public Backchannel getBackchannel( BackchannelKey key )
  {
    return getToolCoordinator().getBackchannel( this, key, state );
  }

  
  /**
   * Subclasses should call this first via super when their overriden onOpen method
   * is called. After, other setting up can be done. Getters will return
   * valid values after this has been called.
   * 
   * @param session The session that the endpoint originates from.
   * @throws IOException Only thrown if the state ID is missing.
   */
  public void onOpen(Session session) throws IOException
  {
    if ( this.session != null ) throw new IOException( "onOpen was called more than once on this ToolEndpoint instance." );
    this.session = session;
    toolCoordinator = ToolCoordinator.get( session.getContainer() );
    blobEx = toolCoordinator.getBlobExchanger();
    // If it hasn't already been done for this class, map the handler methods.
    getHandlerMap( this.getClass() );
    List<String> list = session.getRequestParameterMap().get( "state_id" );
    if ( list != null && list.size() == 1 )
      stateid = list.get( 0 );
    if ( stateid == null ) throw new IOException( "No state ID parameter provided in URL to web socket endpoint." );
    logger.log(Level.INFO, "State ID = {0}", stateid);

    String claimedNonce=null;
    list = session.getRequestParameterMap().get( "nonce" );
    if ( list != null && list.size() == 1 )
      claimedNonce = list.get( 0 );
    if ( claimedNonce == null ) throw new IOException( "No nonce parameter provided in URL to web socket endpoint." );
    logger.log(Level.INFO, "Claimed nonce = {0}", claimedNonce );
    
    try
    {
      state = toolCoordinator.getLtiStateStore().getState( stateid, claimedNonce );
    }
    catch ( LtiException ltiex )
    {
      throw new IOException( "Invalid nonce with state ID.", ltiex );
    }
    
    if ( state == null ) throw new IOException( "State not found for ID given in URL." );
    toolState = state.getToolLaunchState();
    if ( toolState == null ) throw new IOException( "Tool state not found in LTI state." );
    String platform = getState().getPlatformName();
    try
    {
      URL url = new URL( platform );
      platformHost = url.getHost();
    }
    catch ( Exception e )
    {
      platformHost = null;
    }
    URI wsUri = session.getRequestURI();
    StringBuilder sb = new StringBuilder();
    sb.append( "https://" );
    sb.append( wsUri.getHost() );
    if ( wsUri.getPort() > 0 )
    {
      sb.append( ":" );
      sb.append( wsUri.getPort() );
    }
    sb.append( toolCoordinator.getBlobExchanger().getServletUrl() );
    String blobNonce = toolCoordinator.getBlobExchanger().registerSession( stateid );
    
    toolCoordinator.addWsSession( this, session );
    clientConfiguration = new ControlClientConfiguration();
    clientConfiguration.setSessionId( stateid );
    clientConfiguration.setBlobUri( sb.toString() );
    clientConfiguration.setBlobNonce( blobNonce );
    ToolMessage clientConfigMessage = new ToolMessage( null, ControlMessageName.ControlClientConfiguration, clientConfiguration, true );
    sendToolMessage( session, clientConfigMessage );    
  }

  /**
   * Subclasses should call this from their own onClose method. Important to
   * call in order to keep track of endpoints that are currently accessing the
   * same resource.
   * 
   * @param session The session this endpoint originated from.
   * @throws IOException Unlikely to be thrown.
   */
  public void onClose(Session session) throws IOException
  {
    getToolCoordinator().releaseBackchannels( this );    
    toolCoordinator.removeWsSession( this );
  }

  public void onMessage(Session session, String text) throws IOException
  {
    try
    {
      ToolMessageIncomingParts partial = new ToolMessageIncomingParts( session, text );
      if ( !partial.isValid() )
      {
        logger.log(Level.SEVERE, "Endpoint received invalid message: {0}", text );
        return;
      }

      for ( BinaryPart bp : partial.getBinaryParts() )
      {
        byte[] data = blobEx.getBlob( stateid, bp.getId() );
        if ( data == null )
        {
          logger.log(Level.SEVERE, "Blob missing from message: {0}", text );
          return;
        }
        bp.setData( data );
      }

      partial.parsePayload();
      if ( dispatchMessage( session, partial.getToolMessage() ) )
        return;
      logger.log( Level.WARNING, "Did not find handler for message." );        
    }
    catch ( DecodeException ex )
    {
      logger.log( Level.SEVERE, null, ex );
    }
  }
  
  /**
   * Takes an incoming message from the client end of the socket and
   * dispatches it to the right handler method using reflection.
   * 
   * @param session The websocket session that the message came in on.
   * @param message The websocket message.
   * @return Returns true if the message was recognised and handled, whether or not it resulted in an error or warning.
   * @throws IOException Thrown to abort message processing.
   */
  private boolean dispatchMessage( Session session, ToolMessage message ) throws IOException
  {
    logger.log(Level.INFO, "dispatchMessage type = {0}", message.getMessageType());
    HandlerMethodRecord record = getHandlerMap( this.getClass() ).get( message.getMessageType() );
    if ( record == null ) return false;
    
    Class pc = record.getParameterClass();
    logger.log(Level.INFO, "dispatchMessage found handler record {0}", pc);
    
    if ( pc != null )
    {
      if ( message.getPayload() == null ) return false;
      logger.log(Level.INFO, "dispatchMessage payload class is {0}", message.getPayload().getClass());
      if ( !(message.getPayload().getClass().isAssignableFrom( record.getParameterClass() ) ) )
        return false;
    }
    
    logger.log( Level.INFO, "Invoking method." );
    try
    {
      if ( pc == null )
        record.getMethod().invoke( this, session, message );
      else
        record.getMethod().invoke( this, session, message, message.getPayload() );
    }
    catch ( IllegalAccessException | IllegalArgumentException ex )
    {
      logger.log( Level.SEVERE, "Web socket message handler error.", ex );
    }
    catch ( InvocationTargetException ex )
    {
      // method threw exception
      Throwable original = ex.getCause();
      logger.log( Level.SEVERE, "Invocation target exception when calling handler.", original );
      if ( original instanceof HandlerException )
      {
        HandlerException he = (HandlerException) original;
        logger.log( Level.FINE, "Sending ControlErrorReply" );
        logger.log( Level.FINE, he.getReply().getLogMessage() );
        logger.log( Level.FINE, he.getReply().getUserMessage() );
        sendToolMessage( session, new ToolMessage( 
                message, 
                ControlMessageName.ControlErrorReply, 
                he.getReply() ) );    
      }
      else
      {
        if ( original instanceof IOException )
          throw (IOException)original;
        logger.log( Level.SEVERE, "Web socket message handler error.", ex );
      }
    }
    finally
    {
      if ( record.isReplyPromised() && !message.isReplyToThisSent() )
      {
        logger.log( Level.SEVERE, "Handler promised reply but no reply was sent. {0} {1}",
                    new Object[ ]{record.getName(), message.getId()});
        ControlErrorReply cereply = new ControlErrorReply();
        cereply.setLogMessage( "Handler failed to reply." );
        cereply.setUserMessage( "A technical error occured in communication between your browser and the server computer." );
        sendToolMessage( session, new ToolMessage( message, ControlMessageName.ControlErrorReply, cereply ) );    
      }
    }
    
    return true;
  }


  private ToolMessageOutgoingParts buildOutgoingParts( ToolMessage tm ) throws DecodeException, JsonProcessingException
  {
    return new ToolMessageOutgoingParts( tm );
  }
  
  /**
   * Send the tool message from this server to the client.
   * 
   * @param session The session which should send the message.
   * @param parts
   */
  private void sendToolMessageParts( Session session, ToolMessageOutgoingParts parts )
  {
    // blobs to blob exchanger
    // text as websocket message
    if ( parts.hasBinaryParts() )
      for ( BinaryPart bp : parts.getBinaryParts() )
      {
        try
        {
          blobEx.putBlob( stateid, bp.getId(), bp.getData() );
        }
        catch ( BlobExException ex )
        {
          logger.log( Level.SEVERE, null, ex );
        }
      }
    
    session.getAsyncRemote().sendText( parts.getText() );
    // message sending complete
    // if this is reply and it is going back to original session,
    // record that it has been sent.
    ToolMessage rtm = parts.getToolMessage().getReplytoMessage();
    if ( rtm != null && session == rtm.getSession() )
      rtm.setReplyToThisSent( true );
  }

    
  /**
   * Send the tool message from this server to the client.
   * 
   * @param session The session which should send the message.
   * @param tm The message to send.
   */
  public void sendToolMessage( Session session, ToolMessage tm )
  {
    ToolMessageOutgoingParts parts;
    try
    {    
      parts = buildOutgoingParts( tm );
    }
    catch ( DecodeException | JsonProcessingException ex )
    {
      logger.log( Level.SEVERE, null, ex );
      return;
    }
    sendToolMessageParts( session, parts );
  }
  
  /**
   * Send the tool message from this server to the client.
   * 
   * @param predicate Object that defines which sessions will receive messages.
   * @param tm The message to send.
   */
  public void sendToolMessage( ToolEndpointSessionRecordPredicate predicate, ToolMessage tm )
  {
    ToolMessageOutgoingParts parts;
    try
    {    
      parts = buildOutgoingParts( tm );
    }
    catch ( DecodeException | JsonProcessingException ex )
    {
      logger.log( Level.SEVERE, null, ex );
      return;
    }
    for ( Session s : toolCoordinator.getWsSessions( predicate ) )
    {
      logger.info( "Telling a client." );
      sendToolMessageParts( s, parts );
    }
  }

  /**
   * Find all the sessions that are current and relate to the same resource
   * key as for this endpoint. Then send a copy of the message to each of them.
   * Will include the client connected to the other end of this socket.
   * 
   * @param tm The message to send.
   */
  public void sendToolMessageToPlatformResourceUsers( ToolMessage tm )
  {
    if ( !indexByPlatformResource() || toolState.getPlatformResourceKey() == null )
      return;

    ToolMessageOutgoingParts parts;
    try
    {    
      parts = buildOutgoingParts( tm );
    }
    catch ( DecodeException | JsonProcessingException ex )
    {
      logger.log( Level.SEVERE, null, ex );
      return;
    }
    
    for ( Session s : toolCoordinator.getWsSessionsForPlatformResource( toolState.getPlatformResourceKey() ) )
    {
      logger.info( "Telling a client." );
      sendToolMessageParts( s, parts );
    }
  }
    
  /**
   * Find all the sessions that are current and relate to the same tool resource
   * as for this endpoint. Then send a copy of the message to each of them.
   * Will include the client connected to the other end of this socket.
   * 
   * @param tm The message to send.
   */
  public void sendToolMessageToToolResourceUsers( ToolMessage tm )
  {
    if ( !indexByToolResource() || toolState.getToolResourceId() == null )
      return;

    ToolMessageOutgoingParts parts;
    try
    {    
      parts = buildOutgoingParts( tm );
    }
    catch ( DecodeException | JsonProcessingException ex )
    {
      logger.log( Level.SEVERE, null, ex );
      return;
    }
        
    for ( Session s : toolCoordinator.getWsSessionsForToolResource( toolState.getToolResourceId() ) )
    {
      logger.info( "Telling a client." );
      sendToolMessageParts( s, parts );
    }
  }
}
