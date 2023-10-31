/*
 * Copyright 2023 maber01.
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
package uk.ac.leedsbeckett.ltitoolset.backchannel;


/**
 *
 * @author maber01
 */
public class LtiAutoRegistrationBackchannelKey extends BackchannelKey
{
  protected String url;

  public LtiAutoRegistrationBackchannelKey( String platform, String url )
  {
    super( platform );
    this.url = url;
  }
  
  
  
  @Override
  public int hashCode()
  {
    return platform.hashCode() | url.hashCode();
  }

  @Override
  public boolean equals( Object obj )
  {
    if ( this == obj )
    {
      return true;
    }
    if ( obj == null )
    {
      return false;
    }
    if ( getClass() != obj.getClass() )
    {
      return false;
    }
    
    final LtiAutoRegistrationBackchannelKey other = (LtiAutoRegistrationBackchannelKey) obj;
    return this.platform.equals( other.platform )     && 
            this.url.equals(  other.url );
  }
  
  
}
