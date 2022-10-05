/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.ac.leedsbeckett.lti.toolset;

import uk.ac.leedsbeckett.lti.config.ClientLtiConfigurationKey;
import uk.ac.leedsbeckett.lti.state.LtiStateSupplier;

/**
 *
 * @author jon
 */
public class ToolSetLtiStateSupplier implements LtiStateSupplier<ToolSetLtiState>
{
  @Override
  public ToolSetLtiState get( ClientLtiConfigurationKey ck )
  {
    return new ToolSetLtiState( ck );
  }
}
