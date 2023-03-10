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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import uk.ac.leedsbeckett.ltitoolset.config.ToolConfiguration;

/**
 *
 * @author maber01
 */
public class BlackboardConfiguration
{
  static final Logger logger = Logger.getLogger(BlackboardConfiguration.class.getName() );

  String strpathconfig;  
  String rawconfig;


  String id;
  String secret;

  public String getId()
  {
    return id;
  }

  public String getSecret()
  {
    return secret;
  }
  
  /**
   * Load a configuration file in JSON format.
   * 
   * @param strpathconfig The file name (path) to load.
   */
  public void load( String strpathconfig )
  {
    this.strpathconfig = strpathconfig;
    
    try
    {
      rawconfig = FileUtils.readFileToString( new File( strpathconfig ), StandardCharsets.UTF_8 );      
      ObjectMapper mapper = new ObjectMapper();
      JsonFactory factory = mapper.getFactory();
      JsonParser parser = factory.createParser( rawconfig );
      JsonNode node = mapper.readTree(parser);
      if ( node.isObject() )
      {
        JsonNode restnode = node.get( "rest" );
        JsonNode authnode = null;
        if ( restnode != null && restnode.isContainerNode() )
          authnode = restnode.get( "auth" );
        if ( authnode != null && authnode.isContainerNode() )
        {
          id = authnode.get( "id" ).asText();
          secret = authnode.get( "secret" ).asText();
        }
        logger.fine( "ToolConfiguration loading." );
      }
    }
    catch ( FileNotFoundException ex )
    {
      Logger.getLogger(ToolConfiguration.class.getName() ).log( Level.SEVERE, null, ex );
    }
    catch ( IOException ex )
    {
      Logger.getLogger(ToolConfiguration.class.getName() ).log( Level.SEVERE, null, ex );
    }
    catch ( Throwable th )
    {
      Logger.getLogger(ToolConfiguration.class.getName() ).log( Level.SEVERE, null, th );
    }
  }
}
