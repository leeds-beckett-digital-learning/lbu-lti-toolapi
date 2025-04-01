/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.ac.leedsbeckett.ltitoolset.deeplinking.data;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a user's requirement for specifying a tool selection.
 * For use in websocket messages.
 * 
 * @author jon
 */
public class DeepLinkingSelection
{
  final String toolId;
  final String toolFacetId;
  final String toolResourceTitle;
  final String toolResourceDescription;
  
  final String toolResourceId;

  /**
   * Construct object from properties.
   * 
   * @param toolId The ID of the tool
   * @param toolFacetId The ID of the tool's facet
   * @param toolResourceTitle The title of the new tool resource
   * @param toolResourceDescription Description of the new tool resource
   * @param toolResourceId The ID of an existing tool resource or null to 
   * indicate new tool resource.
   */
  public DeepLinkingSelection( 
          @JsonProperty("toolId") String toolId, 
          @JsonProperty("toolFacetId") String toolFacetId, 
          @JsonProperty("toolResourceTitle") String toolResourceTitle,
          @JsonProperty("toolResourceDescription") String toolResourceDescription,
          @JsonProperty("toolResourceId") String toolResourceId
          )
  {
    this.toolId = toolId;
    this.toolFacetId = toolFacetId;
    this.toolResourceTitle = toolResourceTitle;
    this.toolResourceDescription = toolResourceDescription;
    this.toolResourceId = toolResourceId;
  }

  /**
   * Standard POJO getter
   * @return The ID.
   */
  public String getToolId()
  {
    return toolId;
  }

  /**
   * Standard POJO getter
   * @return The type.
   */
  public String getToolFacetId()
  {
    return toolFacetId;
  }

  /**
   * Standard POJO getter
   * @return The title for the tool resource.
   */
  public String getToolResourceTitle()
  {
    return toolResourceTitle;
  }

  /**
   * Standard POJO getter
   * @return The description
   */
  public String getToolResourceDescription()
  {
    return toolResourceDescription;
  }

  /**
   * Standard POJO getter
   * @return The ID of an existing tool resource or null if new tool 
   * resource is requested.
   */
  public String getToolResourceId()
  {
    return toolResourceId;
  }
}
