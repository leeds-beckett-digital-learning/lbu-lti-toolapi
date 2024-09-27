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
import uk.ac.leedsbeckett.lti.services.ags.LtiAssessmentAndGradesServiceClaim;
import uk.ac.leedsbeckett.lti.services.ags.data.LineItem;
import uk.ac.leedsbeckett.lti.services.ags.data.Score;
import uk.ac.leedsbeckett.ltitoolset.backchannel.BackchannelKey;
import uk.ac.leedsbeckett.ltitoolset.backchannel.JsonResult;
import uk.ac.leedsbeckett.ltitoolset.backchannel.LtiBackchannel;
import uk.ac.leedsbeckett.ltitoolset.backchannel.LtiBackchannelKey;
import uk.ac.leedsbeckett.ltitoolset.backchannel.OAuth2Token;

/**
 *
 * @author maber01
 */
public class LtiAgsBackchannel extends LtiBackchannel
{
  String agsUrl;
  
  public LtiAgsBackchannel( BackchannelKey key, String authtokenurl, String clientid, String signingkeyid, PrivateKey signingkey )
  {
    super( key, authtokenurl, clientid, signingkeyid, signingkey );
    LtiBackchannelKey ltikey = (LtiBackchannelKey)key;
    agsUrl = ltikey.getUrl();
  }

  public JsonResult getLineItems() throws IOException
  {
    OAuth2Token t = getPlatformAuthToken( LtiAssessmentAndGradesServiceClaim.SCOPE_LINEITEM_READONLY );
    return getLineItems( agsUrl,  t.getAccessToken() );
  }  
  
  public JsonResult postLineItem( LineItem lineItem ) throws IOException
  {
    OAuth2Token t = getPlatformAuthToken( LtiAssessmentAndGradesServiceClaim.SCOPE_LINEITEM );
    return postLineItem( agsUrl,  t.getAccessToken(), lineItem );
  }  
  
  public JsonResult postScores( LineItem lineItem, Score score ) throws IOException
  {
    OAuth2Token t = getPlatformAuthToken( LtiAssessmentAndGradesServiceClaim.SCOPE_SCORE );
    String url = lineItem.getId() + "/scores";
    return postScores( url,  t.getAccessToken(), score );
  }  
  
  
}
