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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import uk.ac.leedsbeckett.ltitoolset.backchannel.HttpClient;
import uk.ac.leedsbeckett.ltitoolset.backchannel.JsonResult;
import uk.ac.leedsbeckett.ltitoolset.backchannel.blackboard.data.GetCoursesV3Results;
import uk.ac.leedsbeckett.ltitoolset.backchannel.blackboard.data.RestExceptionMessage;

/**
 * One instance in the web application for each blackboard learn platform
 * we communicate with.
 * 
 * @author maber01
 */
public class BlackboardPlatform
{
  static final Logger logger = Logger.getLogger(BlackboardPlatform.class.getName() );
  
  
  String platform;
  BlackboardRestTokenStore blackboardresttokenstore;

  public BlackboardPlatform( String platform, BlackboardRestTokenStore blackboardresttokenstore )
  {
    this.platform = platform;
    this.blackboardresttokenstore = blackboardresttokenstore;
    
  }
  
  public JsonResult getV3Courses( String courseId, String name )
  {
    if ( StringUtils.isBlank( courseId ) && StringUtils.isBlank( name ) )
      return null;
    
    String token = blackboardresttokenstore.getPlatformToken( platform );
    String target = "https://" + platform + "/learn/api/public/v3/courses";
    ArrayList<NameValuePair> params = new ArrayList<>();
    
    if ( !StringUtils.isBlank( courseId ) ) params.add( new BasicNameValuePair( "XXXcourseId", name ) );
    if ( !StringUtils.isBlank( name     ) ) params.add( new BasicNameValuePair( "XXXname", name ) );
    
    try
    {
      return HttpClient.getBlackboardRest( target, token, params, GetCoursesV3Results.class, RestExceptionMessage.class );
    }
    catch ( IOException ex )
    {
    }
    return null;
  }

}
