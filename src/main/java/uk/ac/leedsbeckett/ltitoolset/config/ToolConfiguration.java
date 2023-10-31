/*
 * Copyright 2022 Leeds Beckett University.
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

package uk.ac.leedsbeckett.ltitoolset.config;

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


/**
 * Represents the configuration of an LTI tool. Loads from a JSON file.
 * Likely to be much changed in the future. Currently contains a number of
 * issuer configs which each contain a number of client configs.
 * 
 * @author jon
 */
public class ToolConfiguration
{
  static final Logger logger = Logger.getLogger(ToolConfiguration.class.getName() );

  String strpathconfig;  
  String rawconfig;
  String hostName;
    
  String backchannelProxy = null;
  boolean developmentTrustAllServersMode = false;

  /**
   * The original JSON formatted text that was most recently loaded.
   * 
   * @return JSON formatted text.
   */
  public String getRawConfiguration()
  {
    return rawconfig;
  }
  
  /**
   * The file name (path) that was last used to load configuration.
   * 
   * @return Path to file.
   */
  public String getConfigFileName()
  {
    return strpathconfig;
  }

  public String getBackchannelProxy()
  {
    return backchannelProxy;
  }  

  public boolean isDevelopmentTrustAllServersMode()
  {
    return developmentTrustAllServersMode;
  }

  public String getHostName()
  {
    return hostName;
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
        logger.fine( "ToolConfiguration loading." );
        if ( node.has( "backchannelProxy" ) )
        {
          JsonNode n = node.get( "backchannelProxy" );
          this.backchannelProxy = n.asText();
        }
        if ( node.has( "developmentTrustAllServersMode" ) )
        {
          JsonNode n = node.get( "developmentTrustAllServersMode" );
          this.developmentTrustAllServersMode = n.asBoolean( false );
        }
        if ( node.has( "hostName" ) )
        {
          JsonNode n = node.get( "hostName" );
          this.hostName = n.asText( "localhost" );
        }
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
