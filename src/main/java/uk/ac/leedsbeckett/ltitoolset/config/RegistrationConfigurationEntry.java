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

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import uk.ac.leedsbeckett.ltitoolset.store.Entry;

/**
 *
 * @author maber01
 */
public class RegistrationConfigurationEntry implements Entry<String>, Serializable
{
  String issuer;  // use as key
  long timeStamp;
  RegistrationConfiguration registrationConfiguration;

  public RegistrationConfigurationEntry( @JsonProperty("key") String issuer )
  {
    this.issuer = issuer;
  }
  
  @Override
  public String getKey()
  {
    return issuer;
  }

  @Override
  public void setKey( String key )
  {
    if ( this.issuer != null )
      throw new IllegalArgumentException( "Not allowed to change key." );
    issuer = key;
  }

  public long getTimeStamp()
  {
    return timeStamp;
  }

  public void setTimeStamp( long timeStamp )
  {
    this.timeStamp = timeStamp;
  }

  @Override
  public void initialize()
  {
  }

  public RegistrationConfiguration getRegistrationConfiguration()
  {
    return registrationConfiguration;
  }

  public void setRegistrationConfiguration( RegistrationConfiguration registrationConfiguration )
  {
    this.registrationConfiguration = registrationConfiguration;
  }
}
