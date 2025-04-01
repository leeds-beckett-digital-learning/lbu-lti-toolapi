/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/AnnotationType.java to edit this template
 */
package uk.ac.leedsbeckett.ltitoolset.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation used to describe a tool.
 * 
 * @author jon
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.TYPE )
public @interface ToolProperties
{
  /**
   * Tool name that maps to the annotated tool.
   * 
   * @return The name.
   */
  public String id();

  /**
   * The human readable title.
   * 
   * @return The title.
   */
  public String title();

  /**
   * Which facet to use if none is specified.
   * 
   * @return The ID.
   */
  public String defaultFacetId();
  
}
