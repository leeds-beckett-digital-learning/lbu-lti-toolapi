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

package uk.ac.leedsbeckett.lti.toolset.page;

import java.lang.annotation.Annotation;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.ServerEndpoint;
import uk.ac.leedsbeckett.lti.toolset.ToolCoordinator;

/**
 * This class provides common logic for supporting all the JSP pages.
 * 
 * @author jon
 */
public abstract class PageSupport
{
  protected HttpServletRequest request;
  protected String importantmessage="";
  
  protected ToolCoordinator toolCoordinator;

  /**
   * Get the HTTP request associated with the JSP page that uses this object.
   * @return The HTTPServletRequest that was set for this object.
   */
  public HttpServletRequest getRequest()
  {
    return request;
  }

  /**
   * The JSP will call this to initiate processing and then call the getter
   * methods to retrieve outcomes of the processing.
   * 
   * @param request The HttpRequest associated with the JSP's servlet.
   * @throws javax.servlet.ServletException An exception which should abort processing of the page request.
   */
  public void setRequest( HttpServletRequest request ) throws ServletException
  {
    this.request = request;
    toolCoordinator = ToolCoordinator.get( request.getServletContext() );
  }

  /**
   * Get the important message.
   * 
   * @return An important message or an empty string.
   */
  public String getImportantMessage()
  {
    return importantmessage;
  }
  
  /**
   * If there is a websocket which will be used by the page, this method
   * will calculate the URI of the endpoint based on the web application
   * context and the Annotation of the endpoint.
   * 
   * @param annotation The annotation of an Endpoint.
   * @return The full URI of the websocket.
   */
  protected String computeWebSocketUri( Annotation annotation )
  {
    if ( annotation != null && annotation instanceof ServerEndpoint )
    {
      ServerEndpoint se = (ServerEndpoint)annotation;
      StringBuilder sb = new StringBuilder();
      sb.append( (request.isSecure()?"wss://":"ws://") );
      sb.append( request.getServerName() );
      sb.append( ":" );
      sb.append( request.getServerPort() );
      sb.append( request.getServletContext().getContextPath() );
      sb.append( se.value() );
      return sb.toString();
    }
    return null;
  }
}
