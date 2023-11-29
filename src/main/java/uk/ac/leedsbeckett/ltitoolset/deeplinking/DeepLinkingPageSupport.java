/*
 * Copyright 2023 maber01.
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
package uk.ac.leedsbeckett.ltitoolset.deeplinking;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import uk.ac.leedsbeckett.lti.state.LtiStateStore;
import uk.ac.leedsbeckett.ltitoolset.ToolSetLtiState;
import uk.ac.leedsbeckett.ltitoolset.page.PageSupport;

/**
 *
 * @author maber01
 */
public class DeepLinkingPageSupport extends PageSupport
{
  static final Logger logger = Logger.getLogger( DeepLinkingPageSupport.class.getName() );
  
  protected ToolSetLtiState state;
  protected Object dynamicPageData;
  
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
}
