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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import uk.ac.leedsbeckett.lti.state.LtiStateStore;
import uk.ac.leedsbeckett.lti.toolset.ToolSetLtiState;

/**
 * This class logic for JSP pages that run in the context of LTI
 * launches. Provides subclasses with easy access to LTI state.
 * 
 * @author jon
 */
public abstract class ToolPageSupport extends PageSupport
{
  protected ToolSetLtiState state;

  /**
   * The JSP will call this to initiate processing and then call the getter
   * methods to retrieve outcomes of the processing.
   * 
   * @param request The HttpRequest associated with the JSP's servlet.
   * @throws javax.servlet.ServletException
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

}
