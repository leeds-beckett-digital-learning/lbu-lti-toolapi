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

import uk.ac.leedsbeckett.lti.services.LtiServiceScopeSet;

/**
 *
 * @author maber01
 */
public class LtiBackchannelKey extends BackchannelKey
{
  private final Class<? extends LtiBackchannel> type;
  private final String url;
  private final String scopespec;

  public LtiBackchannelKey( String platform, Class<? extends LtiBackchannel> type, String url, LtiServiceScopeSet scopeSet )
  {
    super( platform );
    this.type = type;
    this.url = url;
    this.scopespec = scopeSet.getScopeSpecification();
  }

  public Class<? extends LtiBackchannel> getType()
  {
    return type;
  }

  public String getUrl()
  {
    return url;
  }

  public String getScopespec()
  {
    return scopespec;
  }
  
  @Override
  public int hashCode()
  {
    return platform.hashCode() | type.hashCode() | url.hashCode() | scopespec.hashCode();
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
    
    final LtiBackchannelKey other = (LtiBackchannelKey) obj;
    return this.platform.equals( other.platform )     && 
            this.type.equals(  other.type ) &&
            this.url.equals(  other.url ) &&
            this.scopespec.equals(  other.scopespec );
  }
  
  
}
