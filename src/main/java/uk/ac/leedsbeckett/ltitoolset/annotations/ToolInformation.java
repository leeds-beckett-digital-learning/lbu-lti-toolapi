/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.ac.leedsbeckett.ltitoolset.annotations;

import java.io.Serializable;
import java.util.logging.Logger;
import uk.ac.leedsbeckett.ltitoolset.Tool;

/**
 * Serializable object that describes a tool with fields that
 * are filled by scanning annotations on the Tool class.
 * 
 * @author jon
 */
public class ToolInformation implements Serializable
{
  static final Logger logger = Logger.getLogger( ToolInformation.class.getName() );
  
  String id = "unknown";
  String type = "unknown";
  String title = "Unknown Tool Title";
  
  ToolInstantiationType instantiationType = ToolInstantiationType.SINGLETON;
  boolean instantiateOnDeepLinking = false;
  boolean instantiateOnLaunching = false;

  /**
   * Fill fields with data from a tool.
   * @param tool The tool to scan. 
   */
  public void scanTool( Tool tool )
  {
    logger.fine( "Scanning " + tool.getClass().getName() );
    
    ToolMapping[] mappings = tool.getClass().getAnnotationsByType( ToolMapping.class );
    logger.fine( "Found " + mappings.length + " mappings." );
    if ( mappings.length >= 1 )
    {
      id = mappings[0].id();
      type = mappings[0].type();
      title = mappings[0].title();
      logger.fine( "Title " + title );
    }

    ToolFunctionality[] functionalities = tool.getClass().getAnnotationsByType( ToolFunctionality.class );
    logger.fine( "Found " + functionalities.length + " functionalities." );
    if ( functionalities.length >= 1 )
    {
      instantiationType = functionalities[0].instantiationType();    
      instantiateOnDeepLinking = functionalities[0].instantiateOnDeepLinking();    
      instantiateOnLaunching = functionalities[0].instantiateOnLaunching();    
      logger.fine( "instantiationType " + instantiationType );
    }
  }

  /**
   * Standard getter.
   * @return The ID
   */
  public String getId()
  {
    return id;
  }

  /**
   * Standard getter.
   * @return The type
   */
  public String getType()
  {
    return type;
  }

  /**
   * Standard getter.
   * @return The title
   */  
  public String getTitle()
  {
    return title;
  }

  /**
   * Standard getter.
   * @return The instantiation type.
   */
  public ToolInstantiationType getInstantiationType()
  {
    return instantiationType;
  }

  /**
   * Standard getter.
   * @return Is capable of instantiating by deep linking?
   */
  public boolean isInstantiateOnDeepLinking()
  {
    return instantiateOnDeepLinking;
  }

  /**
   * Standard getter.
   * @return Is capable of instantiating when launching?
   */
  public boolean isInstantiateOnLaunching()
  {
    return instantiateOnLaunching;
  }  
}
