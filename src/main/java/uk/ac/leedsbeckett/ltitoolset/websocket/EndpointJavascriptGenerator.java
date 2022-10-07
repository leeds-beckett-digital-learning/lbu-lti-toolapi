/*
 * Copyright 2022 maber01.
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
package uk.ac.leedsbeckett.ltitoolset.websocket;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.apache.commons.text.StringSubstitutor;
import org.reflections.Reflections;

/**
 * This class looks for methods of endpoints which are annotated with
 * EndpointMessageHandler.class and generates javascript for use by 
 * clients.
 * 
 * @author maber01
 */
public class EndpointJavascriptGenerator
{
  static String mainJS;
  static String clientNoPayload;
  static String clientPayload;
  
  static void loadResources() throws IOException
  {
    mainJS = IOUtils.resourceToString( "/uk/ac/leedsbeckett/ltitoolset/websocket/js/main.js", StandardCharsets.UTF_8 );
    clientNoPayload = IOUtils.resourceToString( "/uk/ac/leedsbeckett/ltitoolset/websocket/js/clientmessageclass_nopayload.js", StandardCharsets.UTF_8 );
    clientPayload = IOUtils.resourceToString( "/uk/ac/leedsbeckett/ltitoolset/websocket/js/clientmessageclass_payload.js", StandardCharsets.UTF_8 );
  }
  
  
  static String getJavaScriptClass( HandlerMethodRecord handler )
  {
    StringBuilder sba = new StringBuilder();
    StringBuilder sbb = new StringBuilder();
    boolean first=true;

    if ( handler.getParameterClass() != null )
    {
      Constructor<?>[] constructors = handler.getParameterClass().getConstructors();
      if ( constructors == null || constructors.length != 1 )
        return null;

      Annotation[][] anns = constructors[0].getParameterAnnotations();
      String names[] = new String[anns.length];
      for ( int i=0; i<anns.length; i++ )
      {
        for ( Annotation a : anns[i] )
          if ( a instanceof JsonProperty )
            names[i] = ((JsonProperty)a).value();
        if ( names[i] == null )
          return null;
      }

      for ( String n : names )
      {
        if ( first )
          first=false;
        else
        {
          sba.append( ", " );
          sbb.append( ", " );
        }
        sba.append( n );
        sbb.append( "\"" );
        sbb.append( n );
        sbb.append( "\": " );
        sbb.append( n );
      }
    }

    String part;
    if ( handler.getParameterClass() == null )
    {
      part = clientNoPayload.replace( "_SUBCLASS_", handler.getName() );
      part = part.replace( "_MESSAGETYPE_", handler.getName() );
      return part;        
    }
    
    part = clientPayload.replace( "_SUBCLASS_", handler.getName() );
    part = part.replace( "_MESSAGETYPE_", handler.getName() );
    part = part.replace( "_PAYLOADTYPE_", handler.getParameterClass().getName() );
    part = part.replace( "_PARAMETERS_",  sba.toString() );
    part = part.replace( "_PAYLOAD_",     sbb.toString() );
    return part;
  }

  /**
   * Running this main method will scan for endpoint classes and
   * generate javascript. The first parameter is the name of a base
   * Java package within which to scan. The second parameter is
   * the name of the output file.
   * 
   * @param args Expects an array of two parameters.
   */
  public static void main( String[] args )
  {
    System.out.println( "Endpoint Scanner starting..." );
    
    if ( args.length != 2 )
    {
      System.err.println( "Needs two parameter." );
      System.exit( 1 );
    }
    
    System.out.println( "Scan package     = " + args[0] );
    System.out.println( "Output file name = " + args[1] );
    
    try
    {
      loadResources();
    }
    catch ( IOException ex )
    {
      System.err.println( "Unable to load javascript templates from class path." );
      ex.printStackTrace();
      System.exit( 1 );
    }
    
    StringBuilder sb = new StringBuilder();
    
    Reflections r = new Reflections( args[0] );
    Set<Class<? extends ToolEndpoint>> set = r.getSubTypesOf( ToolEndpoint.class );
    for ( Class<? extends ToolEndpoint> c : set )
    {
      System.out.println( "Processing " + c.getCanonicalName() );
      try
      {
        for ( HandlerMethodRecord handler : ToolEndpoint.getHandlerMap( c ).values() )
          sb.append( getJavaScriptClass( handler ) );
      }
      catch ( Exception ex )
      {
        System.out.println( "Cannot process. " + ex.getMessage() ); 
        ex.printStackTrace();
        System.exit( 1 );
      }
    }

    String complete = mainJS.replace( "CLASSES", sb.toString() );
    
    try ( FileWriter fw = new FileWriter( args[1] ) )
    {
      fw.append( complete );
    }
    catch ( IOException ex )
    {
      System.err.println( "Unable to output javascript to file." );
      ex.printStackTrace();
      System.exit( 1 );
    }
    
    System.out.println( "Endpoint Scanner completed normally." );
  }
}
