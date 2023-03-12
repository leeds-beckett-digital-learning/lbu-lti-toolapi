/*
 * Copyright 2022 maber01.
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
package uk.ac.leedsbeckett.ltitoolset.websocket;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.Session;
import uk.ac.leedsbeckett.ltitoolset.ToolCoordinator;
import uk.ac.leedsbeckett.ltitoolset.ToolLaunchState;
import uk.ac.leedsbeckett.ltitoolset.ToolSetLtiState;
import uk.ac.leedsbeckett.ltitoolset.backchannel.OAuth2Token;
import uk.ac.leedsbeckett.ltitoolset.backchannel.Backchannel;
import uk.ac.leedsbeckett.ltitoolset.backchannel.BackchannelKey;
import uk.ac.leedsbeckett.ltitoolset.backchannel.BackchannelOwner;
import uk.ac.leedsbeckett.ltitoolset.websocket.annotations.EndpointMessageHandler;

/**
 * A super class for tools to base endpoints on. Provides functionality to keep
 * track of handler methods and to dispatch messages to them.
 * 
 * @author maber01
 */
public abstract class ToolEndpoint implements BackchannelOwner
{
  static final Logger logger = Logger.getLogger( ToolEndpoint.class.getName() );

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
   * Use reflection and annotations to build a map of handlers for a specific
   * subclass of ToolEndpoint.
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
                          classarray.length == 3?classarray[2]:null );
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


  String stateid;
  ToolSetLtiState state;
  ToolLaunchState toolState;
  ToolCoordinator toolCoordinator;
  
  String platformHost;
  OAuth2Token platformAuthToken=null;
  
  
  /**
   * Default constructor for ToolEndpoint.
   */
  public ToolEndpoint()
  {
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
    toolCoordinator = ToolCoordinator.get( session.getContainer() );
    List<String> list = session.getRequestParameterMap().get( "state" );
    if ( list != null && list.size() == 1 )
      stateid = list.get( 0 );
    if ( stateid == null ) throw new IOException( "No state ID parameter provided in URL to web socket endpoint." );
    logger.log(Level.INFO, "State ID = {0}", stateid);
    state = toolCoordinator.getLtiStateStore().getState( stateid );
    toolState = state.getToolLaunchState();
    toolCoordinator.addWsSession( this, session );
    // If it hasn't already been done for this class, map the handler methods.
    getHandlerMap( this.getClass() );
    String platform = getState().getPlatformName();
    URL url = new URL( platform );
    platformHost = url.getHost();
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

  /**
   * Subclasses should use 'super' to call this when their own onMessage
   * method is called. Probably no other processing will be required. This
   * implementation examines the message and calls the appropriate annotated
   * handler method.
   * 
   * @param session The session this endpoint originated from.
   * @param message The incoming message from the client that needs processing.
   * @throws IOException Exception that aborted processing.
   */
  public void onMessage(Session session, ToolMessage message) throws IOException
  {
    if ( !message.isValid() )
    {
      logger.log(Level.SEVERE, "Endpoint received invalid message: {0}", message.getRaw());
      return;
    }
    
    if ( dispatchMessage( session, message ) )
      return;

    logger.log( Level.WARNING, "Did not find handler for message." );
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
      if ( original instanceof HandlerAlertException )
        processHandlerAlert(session, (HandlerAlertException) original);
      else
      {
        if ( original instanceof IOException )
          throw (IOException)original;
        logger.log( Level.SEVERE, "Web socket message handler error.", ex );
      }
    }
    
    return true;
  }
  
  public abstract void processHandlerAlert( Session session, HandlerAlertException haex ) throws IOException;
  
  
  /**
   * Send the tool message from this server to the client.
   * 
   * @param session The session which should send the message.
   * @param tm The message to send.
   */
  public void sendToolMessage( Session session, ToolMessage tm )
  {
    session.getAsyncRemote().sendObject( tm );    
  }
  
  /**
   * Send the tool message from this server to the client.
   * 
   * @param predicate Object that defines which sessions will receive messages.
   * @param tm The message to send.
   */
  public void sendToolMessage( ToolEndpointSessionRecordPredicate predicate, ToolMessage tm )
  {
    for ( Session s : toolCoordinator.getWsSessions( predicate ) )
    {
      logger.info( "Telling a client." );
      s.getAsyncRemote().sendObject( tm );
    }
  }
  
  /**
   * Find all the sessions that are current and relate to the same resource
   * key as for this endpoint. Then send a copy of the message to each of them.
   * Will include the client connected to the other end of this socket.
   * 
   * @param tm The message to send.
   */
  public void sendToolMessageToResourceUsers( ToolMessage tm )
  {
    for ( Session s : toolCoordinator.getWsSessionsForResource( toolState.getResourceKey() ) )
    {
      logger.info( "Telling a client." );
      s.getAsyncRemote().sendObject( tm );
    }
  }
  
}
