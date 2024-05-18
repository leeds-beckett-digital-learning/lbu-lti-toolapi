/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package uk.ac.leedsbeckett.ltitoolset.annotations;

/**
 * Different ways for a tool to be instantiated.
 * @author jon
 */
public enum ToolInstantiationType
{
  /**
   * There is a single instance of the tool unconnected with
   * any context on the platform.
   */
  SINGLETON,
  
  /**
   * The tool must be instantiated as resources which are
   * linked to from the platform with unique identifiers.
   */
  MULTITON
}
