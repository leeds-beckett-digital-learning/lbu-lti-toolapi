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
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import uk.ac.leedsbeckett.lti.services.data.ServiceStatus;

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
  
  
  boolean successful = false;
  String errorMessage;
  String contentType;
  String contentCharset;
  String rawValue;
  Object result;

  public JsonResult( 
          HttpResponse response,
          Class<?> successClass,
          Class<?> failClass ) throws IOException
  {
    StatusLine statusLine = response.getStatusLine();
    Header contenttypeheader = response.getEntity().getContentType();
    this.contentType = contenttypeheader.getValue();
    this.contentCharset = "ASCII";
    HeaderElement[] contenttypeelement = contenttypeheader.getElements();
    if ( contenttypeelement != null && contenttypeelement.length > 0 )
    {
      this.contentType = contenttypeelement[0].getName();
      NameValuePair charset = contenttypeelement[0].getParameterByName( "charset" );
      if ( charset != null )
        this.contentCharset = charset.getValue();
    }
    
    this.rawValue = IOUtils.toString( response.getEntity().getContent(), this.contentCharset );

    // Try interpreting raw value as JSON regardless of content type.
    Class<?>[] expectedClasses = new Class<?>[3];
    expectedClasses[0] = successClass;
    expectedClasses[1] = failClass;
    expectedClasses[2] = ServiceStatus.class;
    for ( Class<?> c : expectedClasses )
    {
      if ( c == null ) continue;
      try
      {
        result = objectmapper.readValue( rawValue, c );
        break;  // don't try other classes if this didn't throw exception
      }
      catch ( Exception ex )
      {
        logger.log( Level.SEVERE, null, ex );
        logger.log( Level.SEVERE, rawValue );
      }      
    }
    
    successful = statusLine != null && 
                 statusLine.getStatusCode() == HttpStatus.SC_OK &&
                 result != null; 
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
