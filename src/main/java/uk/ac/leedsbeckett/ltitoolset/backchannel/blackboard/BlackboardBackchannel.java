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
package uk.ac.leedsbeckett.ltitoolset.backchannel.blackboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import uk.ac.leedsbeckett.ltitoolset.backchannel.Backchannel;
import uk.ac.leedsbeckett.ltitoolset.backchannel.JsonResult;
import uk.ac.leedsbeckett.ltitoolset.backchannel.OAuth2Token;
import uk.ac.leedsbeckett.ltitoolset.backchannel.blackboard.data.GetCoursesV3Results;
import uk.ac.leedsbeckett.ltitoolset.backchannel.blackboard.data.RestExceptionMessage;

/**
 * One instance in the web application for each blackboard learn platform
 * we communicate with.
 * 
 * @author maber01
 */
public class BlackboardBackchannel extends Backchannel
{
  static final Logger logger = Logger.getLogger(BlackboardBackchannel.class.getName() );
  
  
  String platform;
  String username;
  String password;
  
  OAuth2Token blackboardAuthToken;

  public BlackboardBackchannel( BlackboardBackchannelKey key, String username, String password )
  {
    this.platform = key.getPlatform();
    this.username = username;
    this.password = password;
  }
  
  
  public synchronized OAuth2Token getAuthToken()
  {
    if ( blackboardAuthToken != null )
      return blackboardAuthToken;
        
    try
    {
      String url = "https://" + platform + "/learn/api/public/v1/oauth2/token";
      JsonResult jresult = this.postBlackboardRestTokenRequest( url, username, password );
      
      if ( jresult.isSuccessful() )
      {
        blackboardAuthToken = (OAuth2Token)jresult.getResult();
        return blackboardAuthToken;
      }
      return null;
    }
    catch ( IOException ex )
    {
      logger.log( Level.SEVERE, "IOException while trying to fetch auth token.", ex );
      return null;
    }
  }
 
  
  
  
  public JsonResult getV3Courses( String courseId, String name )
  {
    if ( StringUtils.isBlank( courseId ) && StringUtils.isBlank( name ) )
      return null;
    
    OAuth2Token t = getAuthToken();
    String token = t.getAccessToken();
    String target = "https://" + platform + "/learn/api/public/v3/courses";
    ArrayList<NameValuePair> params = new ArrayList<>();
    
    if ( !StringUtils.isBlank( courseId ) ) params.add( new BasicNameValuePair( "courseId", name ) );
    if ( !StringUtils.isBlank( name     ) ) params.add( new BasicNameValuePair( "name", name ) );
    
    try
    {
      return getBlackboardRest( target, token, params, GetCoursesV3Results.class, RestExceptionMessage.class );
    }
    catch ( IOException ex )
    {
    }
    return null;
  }

}
