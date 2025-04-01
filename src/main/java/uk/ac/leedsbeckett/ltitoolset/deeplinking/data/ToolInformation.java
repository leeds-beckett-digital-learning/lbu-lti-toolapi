/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.ac.leedsbeckett.ltitoolset.deeplinking.data;

import java.util.logging.Logger;

/**
 * Description of tool that is suitable for converting to JSON and
 * sending to client software. Will not be deserialized in server side.
 * 
 * @author jon
 */
public class ToolInformation
{
  static final Logger logger = Logger.getLogger( ToolInformation.class.getName() );
  
  private final String id;
  private final String title;
  private final ToolFacetInformation[] facets;

  public ToolInformation( String id, String title, ToolFacetInformation[] facets )
  {
    this.id = id;
    this.title = title;
    this.facets = facets;
  }
  
  public String getId()
  {
    return id;
  }

  public String getTitle()
  {
    return title;
  }

  public ToolFacetInformation[] getFacets()
  {
    return facets;
  }  
}
