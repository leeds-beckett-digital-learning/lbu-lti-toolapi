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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;

/**
 *
 * @author maber01
 */
public class JsonResult
{
  static final Logger logger = Logger.getLogger( JsonResult.class.getName() );
  
  private static final ObjectMapper objectmapper = new ObjectMapper();
  static
  {
    objectmapper.enable( SerializationFeature.INDENT_OUTPUT );
    objectmapper.disable( SerializationFeature.FAIL_ON_EMPTY_BEANS );
    objectmapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
  }
  
  
  boolean complete = false;
  boolean successful = false;
  String errorMessage;
  String contentType;
  String rawValue;
  Object result;

  public JsonResult( 
          StatusLine statusLine, 
          String contentType, 
          String rawValue,
          Class<?> successClass,
          Class<?> failClass )
  {
    if ( statusLine != null)
    {
      switch ( statusLine.getStatusCode() )
      {
        case HttpStatus.SC_OK:
          complete = true;
          successful = true;
          break;
        case HttpStatus.SC_BAD_REQUEST:
          complete = true;
          break;
        default:
          errorMessage = statusLine.getReasonPhrase();
          break;
      }
    }
    this.contentType = contentType;
    this.rawValue = rawValue;
    if ( !complete )
      return;
    try
    {
      if ( successful )
        result = objectmapper.readValue( rawValue, successClass );
      else
        result = objectmapper.readValue( rawValue, failClass );
    }
    catch ( Exception ex )
    {
      logger.log( Level.SEVERE, null, ex );
      logger.log( Level.SEVERE, rawValue );
      complete = false;
    }
  }

  
  
  public boolean isComplete()
  {
    return complete;
  }

  public boolean isSuccessful()
  {
    return successful;
  }

  public String getErrorMessage()
  {
    return errorMessage;
  }


  public String getContentType()
  {
    return contentType;
  }

  public String getRawValue()
  {
    return rawValue;
  }

  public Object getResult()
  {
    return result;
  }
}
