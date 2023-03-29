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

package uk.ac.leedsbeckett.ltitoolset.page;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.logging.Level;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import uk.ac.leedsbeckett.lti.state.LtiStateStore;
import uk.ac.leedsbeckett.ltitoolset.ToolSetLtiState;
import static uk.ac.leedsbeckett.ltitoolset.page.PageSupport.logger;
import uk.ac.leedsbeckett.ltitoolset.websocket.ToolEndpoint;

/**
 * This class logic for JSP pages that run in the context of LTI
 * launches.Provides subclasses with easy access to LTI state.
 * 
 * @author jon
 * @param <T> The type of the dynamic data used by subclasses
 */
public abstract class ToolPageSupport<T extends DynamicPageData> extends PageSupport
{
  protected T dynamicPageData=null;
  
  
  public abstract T makeDynamicPageData();
  
  protected ToolSetLtiState state;
  

  /**
   * The JSP will call this to initiate processing and then call the getter
   * methods to retrieve outcomes of the processing.
   * 
   * @param request The HttpRequest associated with the JSP's servlet.
   * @throws javax.servlet.ServletException Thrown to abort processing of the page request.
   */
  @Override
  public void setRequest( HttpServletRequest request ) throws ServletException
  {
    super.setRequest( request );
    
    String stateid = request.getParameter( "state_id" );
    if ( stateid == null )
      throw new ServletException( "State ID missing." );
    LtiStateStore<ToolSetLtiState> statestore = this.toolCoordinator.getLtiStateStore();
    if ( statestore == null )
      throw new ServletException( "State store missing." );
    state = statestore.getState( stateid );
    if ( state == null )
      throw new ServletException( "State missing. " + stateid );
    
    dynamicPageData=makeDynamicPageData();
    dynamicPageData.setMyId( state.getPersonId() );
    dynamicPageData.setMyName( state.getPersonName() );
    String uri = state.getToolLaunchState().getRelativeWebSocketUri();
    if ( uri != null )
      dynamicPageData.setWebSocketUri( getBaseUri() + uri );
  }
  
  /**
   * If there is a websocket which will be used by the page, this method
   * will calculate the URI of the endpoint based on the web application
   * context and the Annotation of the endpoint.
   * 
   * @return The full URI of the websocket.
   */
  protected String getBaseUri()
  {
    StringBuilder sb = new StringBuilder();
    sb.append( (request.isSecure()?"wss://":"ws://") );
    sb.append( request.getServerName() );
    sb.append( ":" );
    sb.append( request.getServerPort() );
    sb.append( request.getServletContext().getContextPath() );    
    return sb.toString();
  }
  
  public String getDynamicPageDataAsJSON()
  {
    try
    {
      ObjectMapper om = new ObjectMapper();
      return om.writerWithDefaultPrettyPrinter().writeValueAsString( dynamicPageData );
    }
    catch ( JsonProcessingException ex )
    {
      logger.log( Level.SEVERE, null, ex );
      return "{}";
    }
  }
  
  public T getDynamicPageData()
  {
    return dynamicPageData;
  }  

}
