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
package uk.ac.leedsbeckett.ltitoolset;

import uk.ac.leedsbeckett.ltitoolset.websocket.ToolEndpointSessionRecordPredicate;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.annotation.HandlesTypes;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import javax.websocket.server.ServerContainer;
import org.apache.commons.lang3.StringUtils;
import uk.ac.leedsbeckett.lti.config.LtiConfiguration;
import uk.ac.leedsbeckett.lti.state.LtiStateStore;
import uk.ac.leedsbeckett.ltitoolset.annotations.ToolMapping;
import uk.ac.leedsbeckett.ltitoolset.annotations.ToolSetMapping;
import uk.ac.leedsbeckett.ltitoolset.servlet.ToolLaunchServlet;
import uk.ac.leedsbeckett.ltitoolset.servlet.ToolLoginServlet;
import uk.ac.leedsbeckett.ltitoolset.websocket.ToolEndpoint;
import uk.ac.leedsbeckett.ltitoolset.websocket.ToolEndpointSessionRecord;

/**
 * There is a one to one relationship between instances of this and 
 * servlet contexts. It is used as a central hub to connext LTI launch
 * requests with specific tool implementations. It implements
 * ServletContainerInitializer so it will be instantiated by the server
 * (e.g. tomcat) when the web app is loaded. It scans for tool implementations
 * and maps them onto LTI launch parameters.
 * 
 * @author maber01
 */
@HandlesTypes( {ToolMapping.class, ToolSetMapping.class} )
public class ToolCoordinator implements ServletContainerInitializer
{
  static final Logger logger = Logger.getLogger( ToolCoordinator.class.getName() );
  static final String KEY = ToolCoordinator.class.getCanonicalName();
  
  /**
   * A map that helps connect WebSocketContainers with ServletContexts
   */
  static HashMap<WebSocketContainer,ServletContext> wsContextMap = new HashMap<>();

  /**
   * A static method that will retrieve an instance from an attribute of
   * a ServletContext.
   * 
   * @param context The ServletContext from which to fetch the instance.
   * @return The instance or null if not found.
   */
  public static ToolCoordinator get( ServletContext context )
  {
    return (ToolCoordinator)context.getAttribute( KEY );
  }

  /**
   * A static method that will retrieve an instance by fetching a ServletContext
   * associated with a WebSocketContainer. Instances have to create that 
   * association when they start up.
   * 
   * @param wsContainer The WebSocketContainer for the endpoint that wants a ToolCoordinator.
   * @return The instance for the endpoint.
   */
  public static ToolCoordinator get( WebSocketContainer wsContainer )
  {
    ServletContext context = wsContextMap.get( wsContainer );
    if ( context == null )
      return null;
    return get( context );
  }

  
  
  private final HashMap<ToolKey,Tool> toolMap = new HashMap<>();  
  private final HashMap<ToolKey,ToolMapping> toolMappingMap = new HashMap<>();  
  private final LtiConfiguration lticonfig = new LtiConfiguration();
  private LtiStateStore<ToolSetLtiState> ltistatestore;
  private ToolSetMapping toolSetMapping = null;

  // WebSocket Endpoint related stuff
  private final HashMap<ResourceKey,HashMap<String,ToolEndpointSessionRecord>> wssessionlistmap = new HashMap<>();
  private final HashMap<String,ToolEndpointSessionRecord> allWsSessions = new HashMap<>();
  OpenSessionPredicate opensessionpredicate = new OpenSessionPredicate();

  /**
   * A service record in the META-INF resource of the API jar file fill ensure
   * that the web application container (e.g. tomcat) will load this class and
   * call this method.
   * 
   * @param c Set of classes chosen based on HandlesTypes annotations.
   * @param ctx The servlet context.
   * @throws ServletException Thrown to abort the whole web application. 
   */
  @Override
  public void onStartup( Set<Class<?>> c, ServletContext ctx ) throws ServletException
  {
    logger.info( "ToolCoordinator starting up..." );
    logger.log( Level.INFO, "Context = {0}", ctx );

    // Put this coordinator in a place where servlets can find it
    ctx.setAttribute( this.getClass().getName(), this );
    
    // Spec. says that web socket ServerContainer will be found in this attribute:
    // ServerContainer is a subclass of websocketcontainer specialised to servers.
    // We are assuming that there is a one to one relationship between the servercontainer
    // and the servlet context. This can then provide a mechanism for endpoints
    // to access this instance.
    WebSocketContainer sc = (WebSocketContainer)ctx.getAttribute( ServerContainer.class.getName() );
    if ( sc != null )
      wsContextMap.put( sc, ctx );

    logger.info( "ToolCoordinator mapped web socket container against context." );
    
    // Process the set of classes
    processClasses( c, ctx );
    if ( toolSetMapping == null )
      logger.log( Level.SEVERE, "No tool set mapping was found so cannot set up LTI login and launch servlets" );
    else
      initServlets( ctx );
    
    initLtiConfiguration( ctx );
    initLtiStateStore();
  }

  /**
   * Process the list of classes one by one.
   * 
   * @param c The set.
   * @param ctx The servlet context.
   */
  private void processClasses(  Set<Class<?>> c, ServletContext ctx )
  {
    if ( c == null )
    {
      logger.log( Level.WARNING, "No classes of interest found." );
      return;
    }
    
    for ( Class<?> cl : c )
    {
      logger.log( Level.INFO, "My initalizer found this class: {0}", cl.getName() );
      processToolMapping( cl, cl.getAnnotationsByType( ToolMapping.class ), ctx );
      processToolSetMapping( cl, cl.getAnnotationsByType( ToolSetMapping.class ) );
    }    
  }
  
  /**
   * Process one of the classes that were scanned.
   * 
   * @param clasz The class to examine.
   * @param mappings The ToolMapping annotations, if any, that were found.
   * @param ctx The servlet context.
   */
  private void processToolMapping( Class<?> clasz, ToolMapping[] mappings, ServletContext ctx )
  {
    if ( mappings == null || mappings.length == 0 )
      return;
    
    if ( mappings.length > 1 )
    {
      logger.log( Level.SEVERE, "This class, {0} has multiple ToolMapping annotations.", clasz.getName() );
      return;
    }
    
    if ( !Tool.class.isAssignableFrom( clasz ) )
    {
      logger.log( Level.SEVERE, "This tool class, {0} is not an implementation of Tool interface. Ignoring all of them.", clasz.getName() );
      return;
    }
    
    ToolMapping mapping = mappings[0];
    
    try
    {
      ToolKey key = new ToolKey( mapping );
      Tool tool = (Tool) clasz.getDeclaredConstructor().newInstance();
      toolMap.put( key, tool );
      toolMappingMap.put( key, mapping );
      tool.init( ctx );
    }
    catch ( IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException ex )
    {
      logger.log( Level.SEVERE, "This tool class, {0} could not be instantiated.", clasz.getName() );
      logger.log( Level.SEVERE, "Exception thrown.", ex );
    }          
  }
  
  /**
   * Process one of the classes that were scanned.
   * 
   * @param clasz The class to examine.
   * @param mappings The ToolSetMapping annotations, if any, that were found.
   */
  private void processToolSetMapping( Class<?> clasz, ToolSetMapping[] mappings )
  {
    if ( mappings == null || mappings.length == 0 )
      return;
    
    if ( mappings.length > 1 )
    {
      logger.log( Level.SEVERE, "This class, {0} has multiple ToolSetMapping annotations. Ignoring all of them.", clasz.getName() );
      return;
    }
    
    toolSetMapping = mappings[0];    
  }
  
  /**
   * Deploy the LTI login and launch servlets based on annotations from the
   * toolSetMapping annotation.
   * 
   * @param ctx The servlet context within which the servlets will be deployed.
   */
  private void initServlets( ServletContext ctx )
  {
    ServletRegistration loginReg  = ctx.addServlet( "ToolLoginServlet",  ToolLoginServlet.class );
    ServletRegistration launchReg = ctx.addServlet( "ToolLaunchServlet", ToolLaunchServlet.class );
    
    loginReg.addMapping(  toolSetMapping.loginUrl()  );
    launchReg.addMapping( toolSetMapping.launchUrl() );
  }
  
  /**
   * Find a tool mapping object based on a toolkey composed of type and name.
   * 
   * @param key The key.
   * @return The tool mapping or null;
   */
  public ToolMapping getToolMapping( ToolKey key )
  {
    return toolMappingMap.get( key );
  }
  
  /**
   * Find a tool mapping object based on type and name.
   * 
   * @param type The tool type.
   * @param name The tool name.
   * @return The tool mapping or null;
   */
  public ToolMapping getToolMapping( String type, String name )
  {
    return toolMappingMap.get( new ToolKey( type, name ) );
  }
  
  /**
   * Find a tool object based on a toolkey composed of type and name.
   * 
   * @param key The key.
   * @return The tool mapping or null;
   */
  public Tool getTool( ToolKey key )
  {
    return toolMap.get( key );
  }
  
  /**
   * Find a tool object based on type and name.
   * 
   * @param type The tool type.
   * @param name The tool name.
   * @return The tool mapping or null;
   */
  public Tool getTool( String type, String name )
  {
    return toolMap.get( new ToolKey( type, name ) );
  }


  /**
   * Set up an LTI state store
   * 
   */
  public void initLtiStateStore()
  {
    Cache<String, ToolSetLtiState> cache;
    CacheManager manager = Caching.getCachingProvider().getCacheManager();
    MutableConfiguration<String, ToolSetLtiState> cacheconfig = 
            new MutableConfiguration<String, ToolSetLtiState>()
                    .setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(Duration.ONE_HOUR));
    cache = manager.createCache( "appltistate", cacheconfig );
    ltistatestore = new LtiStateStore<>( cache, new ToolSetLtiStateSupplier() ); 
  }
  
  /**
   * Get the LTI state store.
   * @return The store.
   */
  public LtiStateStore<ToolSetLtiState> getLtiStateStore()
  {
    return ltistatestore;
  }
  
  /**
   * Load the LTI configuration file from a standard location.
   * 
   * @param context 
   */
  private void initLtiConfiguration( ServletContext context )
  {
    String configpath = context.getRealPath( "/WEB-INF/config.json" );
    logger.log( Level.INFO, "Loading LTI configuration from: {0}", configpath );
    if ( !StringUtils.isEmpty( configpath ) )
    {
      lticonfig.load( configpath );
      logger.log( Level.INFO, "Raw configuration: {0}", lticonfig.getRawConfiguration() );
    }
  }  
  
  /**
   * Get the LTI configuration.
   * @return The config object.
   */
  public LtiConfiguration getLtiConfiguration()
  {
    return lticonfig;
  }  
  
  private void appendSessionLog( StringBuilder sb, ToolEndpointSessionRecord record )
  {
    sb.append( "    sid = " )
      .append( record.getEndpoint().getStateid() )
      .append( " Resource Key = " )
      .append( record.getEndpoint().getToolState().getResourceKey() )
      .append( "\n" );
  }
  
  private void logWsSessions()
  {
    StringBuilder sb = new StringBuilder();
    synchronized ( wssessionlistmap )
    {
      sb.append( "All Sessions: \n" );
      for ( String sid : allWsSessions.keySet() )
      {
        ToolEndpointSessionRecord record = allWsSessions.get( sid ); 
        appendSessionLog( sb, record );
      }
      sb.append( "Sessions by resource key: \n" );
      for ( ResourceKey key : wssessionlistmap.keySet() )
      {
        sb.append( " Resource key: " ).append( key.toString() ).append( "\n" );
        HashMap<String,ToolEndpointSessionRecord> map = wssessionlistmap.get( key );
        for ( ToolEndpointSessionRecord record : map.values() )
          appendSessionLog( sb, record );
      }
    }    
    logger.fine( sb.toString() );
  }
  
  
  /**
   * Keep track of a web socket session.That may be needed so that 
 messages can be multicast to all client endpoints associated with
 a specific resource.
   * 
   * @param endpoint The endpoint that has just been opened.
   * @param session The session to add.
   */
  public void addWsSession( ToolEndpoint endpoint, Session session )
  {
    synchronized ( wssessionlistmap )
    {
      ResourceKey key = endpoint.getToolState().getResourceKey();
      HashMap<String,ToolEndpointSessionRecord> set = wssessionlistmap.get( key );
      if ( set == null )
      {
        set = new HashMap<>();
        wssessionlistmap.put( key, set );
      }
      set.put(endpoint.getStateid(), new ToolEndpointSessionRecord( endpoint, session ) );
      allWsSessions.put(endpoint.getStateid(), new ToolEndpointSessionRecord( endpoint, session ) );
      if ( logger.isLoggable( Level.FINE ) )
        logWsSessions();
    }
  }
  
  /**
   * Remove a web socket session because it has shut down.
   * 
   * @param endpoint The endpoint that has just been stopped.
   */
  public void removeWsSession( ToolEndpoint endpoint )
  {
    synchronized ( wssessionlistmap )
    {
      allWsSessions.remove( endpoint.getStateid() );
      ResourceKey key = endpoint.getToolState().getResourceKey();
      HashMap<String,ToolEndpointSessionRecord> set = wssessionlistmap.get( key );
      if ( set != null )
        set.remove( endpoint.getStateid() );
      if ( logger.isLoggable( Level.FINE ) )
        logWsSessions();
    }
  }
  
  /**
   * Get a set of web socket sessions that have been registered against
   * a specific platform resource.Probably the intention is to multicast
   * a message to them all.
   * 
   * @param predicate A predicate to select sessions.
   * @return The set.
   */
  public Set<Session> getWsSessions( ToolEndpointSessionRecordPredicate predicate )
  {
    StringBuilder sb = new StringBuilder();
    sb.append( "Debugging output: \n" );
    synchronized ( wssessionlistmap )
    {
      HashSet<Session> sessions = new HashSet<>();
      for ( ToolEndpointSessionRecord record : allWsSessions.values() )
      {
        sb.append( "Checking: \n" );
        this.appendSessionLog( sb, record );
        if ( opensessionpredicate.test( record.getSession() ) )
        {
          sb.append( "OPEN \n" );
          if ( predicate.test( record ) )
          {
            sb.append( "PASSED TEST\n" );
            sessions.add( record.getSession() );
          }
        }
      }
      logger.fine( sb.toString() );
      return sessions;
    }      
  }
  
  /**
   * Get a set of web socket sessions that have been registered against
   * a specific platform resource. Probably the intention is to multicast
   * a message to them all.
   * 
   * @param key The key of the specific platform resource.
   * @return The set.
   */
  public Set<Session> getWsSessionsForResource( ResourceKey key )
  {
    StringBuilder sb = new StringBuilder();
    sb.append( "Debugging output: \n" );
    synchronized ( wssessionlistmap )
    {
      HashMap<String,ToolEndpointSessionRecord> set = wssessionlistmap.get( key );
      if ( set == null ) return null;
      HashSet<Session> sessions = new HashSet<>();
      for ( ToolEndpointSessionRecord record : set.values() )
      {
        sb.append( "Checking: \n" );
        this.appendSessionLog( sb, record );
        if ( opensessionpredicate.test( record.getSession() ) )
        {
          sb.append( "ADDED\n" );
          sessions.add( record.getSession() );
        }
      }
      logger.fine( sb.toString() );
      return sessions;
    }  
  }
  
  /**
   * A handy utility for use with Set.removeIf()
   * 
   */
  class OpenSessionPredicate implements Predicate<Session>
  {
    @Override
    public boolean test( Session t )
    {
      return t.isOpen();
    }
  }
}
