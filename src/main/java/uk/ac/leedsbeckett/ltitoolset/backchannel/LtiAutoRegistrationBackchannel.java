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

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.io.IOException;
import java.security.PrivateKey;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.leedsbeckett.lti.registration.ConsumerConfiguration;
import uk.ac.leedsbeckett.lti.registration.LtiToolRegistration;

/**
 *
 * @author maber01
 */
public class LtiAutoRegistrationBackchannel extends Backchannel
{
  static final Logger logger = Logger.getLogger(LtiAutoRegistrationBackchannel.class.getName() );
  
  final String openidconfigurl;
            
  public LtiAutoRegistrationBackchannel( BackchannelKey key )
  {
    LtiAutoRegistrationBackchannelKey ltiautoregkey = (LtiAutoRegistrationBackchannelKey)key;
    openidconfigurl = ltiautoregkey.url;
  }
    
  public ConsumerConfiguration getOpenIdConfiguration() throws IOException
  {
    String raw = this.getPublicText( openidconfigurl, null );
    return new ConsumerConfiguration( raw );
  }
  
  public LtiToolRegistration postToolRegistration( String url, String token, LtiToolRegistration reg ) throws IOException
  {
    JsonResult jr = this.postJsonObject( url, token, reg, LtiToolRegistration.class, null );
    if ( jr.isSuccessful() )
    {
      logger.info( jr.getRawValue() );
      return (LtiToolRegistration)jr.getResult();
    }
    return null;
  }
}
