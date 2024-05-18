/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.ac.leedsbeckett.ltitoolset.deeplinking.data;

import java.util.ArrayList;
import uk.ac.leedsbeckett.ltitoolset.annotations.ToolInformation;

/**
 * A data object sent to websocket clients containing information about
 * all the tools/resources that the authenticated user can link/create.
 * @author jon
 */
public class DeepLinkingOptions
{
  ArrayList<ToolInformation> toolInformations;
  
  /**
   * Constructs an empty DeepLinkingOptions object.
   */
  public DeepLinkingOptions()
  {
    this.toolInformations = new ArrayList<>();
  }

  /**
   * Get the list of tool informations.
   * @return The list of tool information objects.
   */
  public ArrayList<ToolInformation> getToolInformations()
  {
    return toolInformations;
  }

  /**
   * Set the list of tool informations.
   * @param toolInformations The list to use.
   */
  private void setToolInformations( ArrayList<ToolInformation> toolInformations )
  {
    this.toolInformations = toolInformations;
  }
  
  /**
   * Add one block of tool information to the list.
   * @param toolInfo The info to add.
   */
  public void addToolInformation( ToolInformation toolInfo )
  {
    toolInformations.add( toolInfo );
  }
}
