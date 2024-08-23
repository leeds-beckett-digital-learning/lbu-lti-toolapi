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
import java.util.logging.Logger;
import javax.websocket.Session;

/**
 * A super class for tools to base endpoints on. Provides functionality to keep
 * track of handler methods and to dispatch messages to them. This subclasses
 * ToolEndpoint by adding ability to register with ToolCoordinator against a
 * resource ID so that different clients can communicate with each other via
 * the server about the same resource. (Source code was based on old ToolEndpoint
 * and portions pushed up to a new ToolEndpoint super class.)
 * 
 * @author maber01
 */
public abstract class MultitonToolEndpoint extends ToolEndpoint
{
  static final Logger logger = Logger.getLogger(MultitonToolEndpoint.class.getName() );

  /**
   * Default constructor for ToolEndpoint.
   */
  public MultitonToolEndpoint()
  {
  }
  
  /**
   * Subclasses should call this first via super when their overriden onOpen method
   * is called. After, other setting up can be done. Getters will return
   * valid values after this has been called.
   * 
   * @param session The session that the endpoint originates from.
   * @throws IOException Thrown if the state is not fully set up.
   */
  public void onOpen(Session session) throws IOException
  {
    super.onOpen( session );
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
    super.onClose( session );
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
