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
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import org.reflections.Reflections;
import uk.ac.leedsbeckett.ltitoolset.websocket.annotations.EndpointJavascriptProperties;

/**
 * This class looks for methods of endpoints which are annotated with
 * EndpointMessageHandler.class and generates javascript for use by 
 * clients.
 * 
 * @author maber01
 */
public class EndpointJavascriptGenerator
{
  static String endpointJS;
  static String toolEndpointJS;
  static String clientNoPayload;
  static String clientPayload;
  
  /**
   * Load some javascript templates from resources.
   * 
   * @throws IOException Thrown if a file is missing from resource files.
   */
  static void loadResources() throws IOException
  {
    endpointJS = IOUtils.resourceToString( "/uk/ac/leedsbeckett/ltitoolset/websocket/js/endpoint.js", StandardCharsets.UTF_8 );
    toolEndpointJS = IOUtils.resourceToString( "/uk/ac/leedsbeckett/ltitoolset/websocket/js/toolendpoint.js", StandardCharsets.UTF_8 );
    clientNoPayload = IOUtils.resourceToString( "/uk/ac/leedsbeckett/ltitoolset/websocket/js/clientmessageclass_nopayload.js", StandardCharsets.UTF_8 );
    clientPayload = IOUtils.resourceToString( "/uk/ac/leedsbeckett/ltitoolset/websocket/js/clientmessageclass_payload.js", StandardCharsets.UTF_8 );
  }
  
  /**
   * Build a javascript class definition for a client originated message and
   * base that on properties of a handler method from a tool endpoint class.
   * 
   * @param handler
   * @param prefix
   * @return 
   */
  static String getJavaScriptClass( HandlerMethodRecord handler, String prefix )
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
   * For a given subclass of ToolMessageName, compute a list of javascript
   * object representations representing the constants in the enum.
   * 
   * @param clasz The class to work on.
   * @return Some javascript
   */
  static String getJavascriptServerMessages( Class<? extends ToolMessageName> clasz )
  {
    StringBuilder sb = new StringBuilder();
    ToolMessageName[] names = clasz.getEnumConstants();
    boolean started = false;
    for ( ToolMessageName name : names )
    {
      if ( started )
        sb.append( ",\n" );
      started = true;
      sb.append( "{\n" );
      sb.append( "  name:\"" );
      sb.append( name.getName() );
      sb.append( "\",\n" );
      sb.append( "  class:\"" );
      sb.append( name.getPayloadClass().toString() );
      sb.append( "\"\n}" );
    }
    sb.append( "\n" );
    
    return sb.toString();
  }
  
  /**
   * Running this main method will scan for endpoint classes and
   * generate javascript. The first parameter is the name of a base
   * Java package within which to scan. The second parameter is
   * the name of the directory to which files will be saved.
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
    System.out.println( "Output directory = " + args[1] );
    
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
    
    
    Reflections r = new Reflections( args[0] );
    Set<Class<? extends ToolEndpoint>> set = r.getSubTypesOf( ToolEndpoint.class );
    for ( Class<? extends ToolEndpoint> c : set )
    {
      // Skip any abstract class that is just a superclass of a tool endpoint.
      if ( Modifier.isAbstract( c.getModifiers() ) )
        continue;
      
      StringBuilder classes = new StringBuilder();
      System.out.println( "Processing " + c.getCanonicalName() );
      Annotation[] ejpanns  = c.getAnnotationsByType( EndpointJavascriptProperties.class );
      if ( ejpanns == null || ejpanns.length != 1 )
      {
        System.out.println( "ToolEndpoint lacks EndpointJavascriptProperties annotation. " ); 
        System.exit( 1 );
      }
      EndpointJavascriptProperties ejp = (EndpointJavascriptProperties)ejpanns[0];
      Class<? extends ToolMessageName> namesclass = ToolMessageName.forName( ejp.messageEnum() );
      if ( namesclass == null )
      {
        System.out.println( "Could not find this enum class: " + ejp.messageEnum() ); 
        System.exit( 1 );
      }
      
      
      try
      {
        for ( HandlerMethodRecord handler : ToolEndpoint.getHandlerMap( c ).values() )
          classes.append( getJavaScriptClass( handler, ejp.prefix() ) );
      }
      catch ( Exception ex )
      {
        System.out.println( "Cannot process. " + ex.getMessage() ); 
        ex.printStackTrace();
        System.exit( 1 );
      }
      
      String complete = toolEndpointJS.replace( "CLASSES", classes.toString() );
      complete = complete.replace( "ENDPOINTEXPORT", ejp.module() );
      complete = complete.replace( "SERVERMESSAGENAMES", getJavascriptServerMessages( namesclass ) );
      try ( FileWriter fw = new FileWriter( args[1] + "/" + ejp.module() + ".js" ) )
      {
        fw.append( complete );
      }
      catch ( IOException ex )
      {
        System.err.println( "Unable to output javascript to file." );
        ex.printStackTrace();
        System.exit( 1 );
      }
    }

    try ( FileWriter fw = new FileWriter( args[1] + "/endpoint.js" ) )
    {
      fw.append( endpointJS );
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
