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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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
  static final String JS_SUPERCLASS = 
    "/*===================================\n" +
    "     Generated script, do not edit.  \n" +
    "  ===================================*/\n" +
    "\n\nclass Message\n" +
    "{\n" +
    "  constructor( messageType, payloadType )\n" +
    "  {\n" +
    "    this.id = nextid++;\n" +
    "    this.messageType = messageType?messageType:null;\n" +
    "    this.payloadType = payloadType?payloadType:null;\n" +
    "    this.replyToId   = null;\n" +
    "    this.payload     = null;\n" +
    "  }\n" +
    "  \n" +
    "  toString()\n" +
    "  {\n" +
    "    var str = \"toolmessageversion1.0\\n\";\n" +
    "    str += \"id:\" + this.id + \"\\n\";\n" +
    "    if ( this.replyToId )\n" +
    "      str += \"replytoid:\" + this.replyToId + \"\\n\";\n" +
    "    if ( this.messageType )\n" +
    "      str += \"messagetype:\" + this.messageType + \"\\n\";\n" +
    "    if ( this.payloadType && this.payload )\n" +
    "    {\n" +
    "      str += \"payloadtype:\" + this.payloadType + \"\\npayload:\\n\" ;\n" +
    "      str += JSON.stringify( this.payload );\n" +
    "    }\n" +
    "    return str;\n" +
    "  }\n" +
    "}\n\n\n";
          
  static final String JS_TEMPLATE = 
    "class ${classname}Message extends Message\n" + 
    "{\n" + 
    "  constructor()\n" + 
    "  {\n" + 
    "    super( \"${messagetype}\", null );\n" + 
    "    this.payload = null;\n" + 
    "  }\n" + 
    "}\n\n";

  static final String JS_TEMPLATE_PAYLOAD = 
    "class ${classname}Message extends Message\n" + 
    "{\n" + 
    "  constructor( ${parameters} )\n" + 
    "  {\n" + 
    "    super( \"${messagetype}\", \"${payloadtype}\" );\n" + 
    "    this.payload = { ${payload} };\n" + 
    "  }\n" + 
    "}\n\n";


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

    Map<String,String> map = new HashMap<>();
    StringSubstitutor sub = new StringSubstitutor( map );
    map.put( "classname",   handler.getName() );
    map.put( "messagetype", handler.getName() );
    if ( handler.getParameterClass() == null )
      return sub.replace( JS_TEMPLATE );        

    map.put( "payloadtype", handler.getParameterClass().getName() );
    map.put( "parameters",  sba.toString() );
    map.put( "payload",     sbb.toString() );
    return sub.replace( JS_TEMPLATE_PAYLOAD );
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
    System.out.println( "Endpoint Scanner running..." );
    
    if ( args.length != 2 )
    {
      System.err.println( "Needs two parameter." );
      System.exit( 1 );
    }
    
    System.out.println( "Scan package     = " + args[0] );
    System.out.println( "Output file name = " + args[1] );
    StringBuilder sb = new StringBuilder();
    
    sb.append( JS_SUPERCLASS );
    
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
      }
    }

    try ( FileWriter fw = new FileWriter( args[1] ) )
    {
      fw.append( sb.toString() );
    }
    catch ( IOException ex )
    {
      System.err.println( "Unable to output javascript to file." );
      System.exit( 1 );
    }
    
    System.out.println( "Endpoint Scanner completed." );
  }
}
