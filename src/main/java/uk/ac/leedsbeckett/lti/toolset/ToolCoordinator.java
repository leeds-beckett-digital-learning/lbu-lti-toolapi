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
package uk.ac.leedsbeckett.lti.toolset;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
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
import uk.ac.leedsbeckett.lti.toolset.annotations.ToolMapping;
import uk.ac.leedsbeckett.lti.toolset.annotations.ToolSetMapping;
import uk.ac.leedsbeckett.lti.toolset.servlet.ToolLaunchServlet;
import uk.ac.leedsbeckett.lti.toolset.servlet.ToolLoginServlet;

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
   * @param wsContainer
   * @return 
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
  private final HashMap<ResourceKey,CopyOnWriteArraySet<Session>> wssessionlistmap = new HashMap<>();
  ClosedSessionPredicate closedsessionpredicate = new ClosedSessionPredicate();
  
  @Override
  public void onStartup( Set<Class<?>> c, ServletContext ctx ) throws ServletException
  {
    logger.info( "ToolCoordinator started." );

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

    // Process the set of classes
    processClasses( c, ctx );
    if ( toolSetMapping == null )
      logger.log( Level.SEVERE, "No tool set mapping was found so cannot set up LTI login and launch servlets" );
    else
      initServlets( ctx );
    
    initLtiConfiguration( ctx );
    initLtiStateStore();
  }

  private void processClasses(  Set<Class<?>> c, ServletContext ctx )
  {
    for ( Class<?> cl : c )
    {
      logger.log( Level.INFO, "My initalizer found this class: {0}", cl.getName() );
      processToolMapping( cl, cl.getAnnotationsByType( ToolMapping.class ), ctx );
      processToolSetMapping( cl, cl.getAnnotationsByType( ToolSetMapping.class ) );
    }    
  }
  
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
  
  private void initServlets( ServletContext ctx )
  {
    ServletRegistration loginReg  = ctx.addServlet( "ToolLoginServlet",  ToolLoginServlet.class );
    ServletRegistration launchReg = ctx.addServlet( "ToolLaunchServlet", ToolLaunchServlet.class );
    
    loginReg.addMapping(  toolSetMapping.loginUrl()  );
    launchReg.addMapping( toolSetMapping.launchUrl() );
  }
  
  public ToolMapping getToolMapping( ToolKey key )
  {
    return toolMappingMap.get( key );
  }
  
  public ToolMapping getToolMapping( String type, String name )
  {
    return toolMappingMap.get( new ToolKey( type, name ) );
  }
  
  public Tool getTool( ToolKey key )
  {
    return toolMap.get( key );
  }
  
  public Tool getTool( String type, String name )
  {
    return toolMap.get( new ToolKey( type, name ) );
  }


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
  
  public LtiStateStore<ToolSetLtiState> getLtiStateStore()
  {
    return ltistatestore;
  }
  
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
  
  public LtiConfiguration getLtiConfiguration()
  {
    return lticonfig;
  }  
  
  
  public void addWsSession( ResourceKey key, Session session )
  {
    synchronized ( wssessionlistmap )
    {
      CopyOnWriteArraySet<Session> set = wssessionlistmap.get( key );
      if ( set == null )
      {
        set = new CopyOnWriteArraySet<>();
        wssessionlistmap.put( key, set );
      }
      set.add( session );
    }
  }
  
  public void removeWsSession( ResourceKey key, Session session )
  {
    synchronized ( wssessionlistmap )
    {
      CopyOnWriteArraySet<Session> set = wssessionlistmap.get( key );
      if ( set == null ) return;
      set.remove( session );
    }
  }
  
  public Set<Session> getWsSessionsForResource( ResourceKey key )
  {
    synchronized ( wssessionlistmap )
    {
      CopyOnWriteArraySet<Session> set = wssessionlistmap.get( key );
      if ( set == null ) return null;
      set.removeIf( closedsessionpredicate );
      return set;
    }  
  }
  
  class ClosedSessionPredicate implements Predicate<Session>
  {
    @Override
    public boolean test( Session t )
    {
      return !t.isOpen();
    }
    
  }
  
}
