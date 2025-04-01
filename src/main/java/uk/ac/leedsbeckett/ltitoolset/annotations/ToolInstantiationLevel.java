/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package uk.ac.leedsbeckett.ltitoolset.annotations;

/**
 * Different ways for a tool to be instantiated.
 * @author jon
 */
public enum ToolInstantiationLevel
{
  /**
   * There is one instance for the whole platform.
   */
  PLATFORM,
  
  /**
   * There is one instance for each course.
   */
  COURSE,
  
  /**
   * One instance allowed for each course resource on the platform.
   */
  PLATFORM_RESOURCE,

  /**
   * Users can create as many instances as they like using deep linking.
   */
  TOOL_RESOURCE
}
