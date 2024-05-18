/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.ac.leedsbeckett.ltitoolset.deeplinking;

import uk.ac.leedsbeckett.ltitoolset.deeplinking.data.DeepLinkingOptions;
import uk.ac.leedsbeckett.ltitoolset.websocket.ToolMessageName;

/**
 * All the server generated messages that the DeepLinkingEndpoint
 * can send.
 * 
 * @author jon
 */
public enum DeepServerMessageName implements ToolMessageName
{
  Alert(              "Alert",              String.class ),
  Options(            "Options",            DeepLinkingOptions.class ),
  Jwt(                "Jwt",                String.class );
  
  /**
   * Each constant has a name which can be used in encoded messages passing
   * through the web socket.
   */
  private final String name;
  
  /**
   * The class of the payload for this message. Note that multiple
   * message names can use the same payload class.
   */
  private final Class payloadClass;

  private DeepServerMessageName( String name, Class payloadClass )
  {
    this.name = name;
    this.payloadClass = payloadClass;
  }

  @Override
  public String getName()
  {
    return this.name;
  }

  @Override
  public Class<?> getPayloadClass()
  {
    return this.payloadClass;
  }
}
