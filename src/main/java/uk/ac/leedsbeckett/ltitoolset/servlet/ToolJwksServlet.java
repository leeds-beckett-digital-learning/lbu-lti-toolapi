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
package uk.ac.leedsbeckett.ltitoolset.servlet;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import uk.ac.leedsbeckett.ltitoolset.ToolCoordinator;

/**
 * This servlet delivers the set of public keys that this tool service might
 * use when accessing a service API on the platform. This is different from the
 * public key set used when the platform 'logs in' during an LTI launch. (That
 * must be provided by a different server at present.)
 * 
 * @author maber01
 */
public class ToolJwksServlet extends HttpServlet
{
  static final Logger logger = Logger.getLogger(ToolJwksServlet.class.getName() );

  @Override
  protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException
  {
    logger.log( Level.FINE, "Request for tool key set from {0}", req.getRemoteAddr() );

    ToolCoordinator toolCoord = ToolCoordinator.get( req.getServletContext() );
    if ( toolCoord == null ) { resp.sendError( 500, "Cannot find tool manager." ); return; }

    resp.getWriter().println( toolCoord.getServiceJwks() );
  }
  
}
