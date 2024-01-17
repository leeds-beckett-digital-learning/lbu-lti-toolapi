/*
 * Copyright 2024 maber01.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.leedsbeckett.ltitoolset.config;

import java.io.Serializable;

/**
 *
 * @author maber01
 */
public class RegistrationConfiguration implements Serializable
{
  String issuer;
  String name;
  int status;
  String secret;
  boolean registrationAllowed;
  boolean deepLinkingAllowed;
  boolean registered = false;
  
  public String getIssuer()
  {
    return issuer;
  }

  public void setIssuer( String issuer )
  {
    if ( this.issuer != null )
      throw new IllegalArgumentException( "Cannot change issuer field of registration configuration." );
    this.issuer = issuer;
  }

  public String getName()
  {
    return name;
  }

  public void setName( String name )
  {
    this.name = name;
  }

  public int getStatus()
  {
    return status;
  }

  public void setStatus( int status )
  {
    this.status = status;
  }

  public String getSecret()
  {
    return secret;
  }

  public void setSecret( String secret )
  {
    this.secret = secret;
  }

  public boolean isRegistrationAllowed()
  {
    return registrationAllowed;
  }

  public void setRegistrationAllowed( boolean registrationAllowed )
  {
    this.registrationAllowed = registrationAllowed;
  }

  
  
  public boolean isDeepLinkingAllowed()
  {
    return deepLinkingAllowed;
  }

  public void setDeepLinkingAllowed( boolean deepLinkingAllowed )
  {
    this.deepLinkingAllowed = deepLinkingAllowed;
  }

  public boolean isRegistered()
  {
    return registered;
  }

  public void setRegistered( boolean registered )
  {
    this.registered = registered;
  }  
}
