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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import uk.ac.leedsbeckett.lti.messages.LtiMessageDeepLinkingResponse;
import uk.ac.leedsbeckett.lti.state.LtiStateStore;
import uk.ac.leedsbeckett.lti.resourcelink.LtiResourceLink;
import uk.ac.leedsbeckett.ltitoolset.Tool;
import uk.ac.leedsbeckett.ltitoolset.ToolKey;
import uk.ac.leedsbeckett.ltitoolset.ToolSetLtiState;
import uk.ac.leedsbeckett.ltitoolset.annotations.ToolMapping;
import uk.ac.leedsbeckett.ltitoolset.page.PageSupport;
import uk.ac.leedsbeckett.ltitoolset.page.ToolPageSupport;

/**
 * Note that this class comes from an LTI launch and therefore subclasses
 * ToolPageSupport but there is no actual tool so it needs to do a little
 * more work - e.g. build the right websocket URL.
 * 
 * @author maber01
 */
public class DeepLinkingPageSupport extends ToolPageSupport<DeepLinkingPageData>
{
  static final Logger logger = Logger.getLogger( DeepLinkingPageSupport.class.getName() );
  
  protected DeepLinkingLaunchState deepstate;
  
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
    
    deepstate = (DeepLinkingLaunchState) state.getToolLaunchState();
    if ( deepstate == null )
      throw new ServletException( "Deep state missing. " + state.getId() );
    dynamicPageData.deepLinkReturnUrl = deepstate.deepLinkReturnUrl;
  }

  @Override
  public String getDynamicPageDataAsJSON()
  {
    logger.log( Level.SEVERE, "Getting dynamic page data." );
    try
    {
      ObjectMapper om = new ObjectMapper();
      String str = om.writerWithDefaultPrettyPrinter().writeValueAsString( dynamicPageData );
      logger.log( Level.INFO, str );
      return str;
    }
    catch ( JsonProcessingException ex )
    {
      logger.log( Level.SEVERE, null, ex );
      return "{}";
    }
  }  

  @Override
  public DeepLinkingPageData makeDynamicPageData()
  {
    return new DeepLinkingPageData();
  }
}
