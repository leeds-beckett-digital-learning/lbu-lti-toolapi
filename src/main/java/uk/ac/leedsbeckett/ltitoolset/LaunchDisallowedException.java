/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Exception.java to edit this template
 */
package uk.ac.leedsbeckett.ltitoolset;

/**
 *
 * @author jon
 */
public class LaunchDisallowedException extends Exception
{

  /**
   * Creates a new instance of <code>LaunchDisallowedException</code> without detail message.
   */
  public LaunchDisallowedException()
  {
  }

  /**
   * Constructs an instance of <code>LaunchDisallowedException</code> with the specified detail
   * message.
   *
   * @param msg the detail message.
   */
  public LaunchDisallowedException( String msg )
  {
    super( msg );
  }
}
