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
package uk.ac.leedsbeckett.ltitoolset.backchannel.services;

import java.io.IOException;
import java.security.PrivateKey;
import uk.ac.leedsbeckett.lti.services.nrps.LtiNamesRoleServiceClaim;
import uk.ac.leedsbeckett.ltitoolset.backchannel.BackchannelKey;
import uk.ac.leedsbeckett.ltitoolset.backchannel.JsonResult;
import uk.ac.leedsbeckett.ltitoolset.backchannel.LtiBackchannel;
import uk.ac.leedsbeckett.ltitoolset.backchannel.LtiBackchannelKey;
import uk.ac.leedsbeckett.ltitoolset.backchannel.OAuth2Token;

/**
 *
 * @author maber01
 */
public class LtiNrpsBackchannel extends LtiBackchannel
{
  String nrpsUrl;
  
  public LtiNrpsBackchannel( BackchannelKey key, String authtokenurl, String clientid, String signingkeyid, PrivateKey signingkey )
  {
    super( key, authtokenurl, clientid, signingkeyid, signingkey );
    LtiBackchannelKey ltikey = (LtiBackchannelKey)key;
    nrpsUrl = ltikey.getUrl();
  }
  public JsonResult getNamesRoles() throws IOException
  {
    OAuth2Token t = getPlatformAuthToken( LtiNamesRoleServiceClaim.SCOPE );
    return getNamesRoles( nrpsUrl,  t.getAccessToken() );
  }    
}
