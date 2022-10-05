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

package uk.ac.leedsbeckett.lti.toolset.servlet;

import java.util.logging.Logger;
import javax.servlet.ServletContext;
import uk.ac.leedsbeckett.lti.config.LtiConfiguration;
import uk.ac.leedsbeckett.lti.servlet.LtiLoginServlet;
import uk.ac.leedsbeckett.lti.state.LtiStateStore;
import uk.ac.leedsbeckett.lti.toolset.ToolCoordinator;
import uk.ac.leedsbeckett.lti.toolset.ToolSetLtiState;

/**
 * This demo's implementation of the LTI login servlet. The annotation determines
 * where the servlet appears in the app's URL space.
 * 
 * @author jon
 */
public class ToolLoginServlet extends LtiLoginServlet<ToolSetLtiState>
{
  static final Logger logger = Logger.getLogger(ToolLoginServlet.class.getName() );

  /**
   * This implementation ensures that the library code knows how to store
   * LTI state.
   * 
   * @param context The servlet context in whose attributes the store can be found.
   * @return The store.
   */  
  @Override
  protected LtiStateStore<ToolSetLtiState> getLtiStateStore( ServletContext context )
  {
    ToolCoordinator toolManager = ToolCoordinator.get( context );
    return toolManager.getLtiStateStore();
  } 
  
  /**
   * This implementation ensures that the library code knows the configuration.
   * 
   * @param context The servlet context in whose attributes the store can be found.
   * @return The configuration.
   */  
  @Override
  protected LtiConfiguration getLtiConfiguration( ServletContext context )
  {
    return ToolCoordinator.get( context ).getLtiConfiguration();
  } 
}
