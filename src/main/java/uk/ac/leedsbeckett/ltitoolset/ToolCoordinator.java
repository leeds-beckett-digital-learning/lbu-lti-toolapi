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

import uk.ac.leedsbeckett.ltitoolset.resources.PlatformResourceKey;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import uk.ac.leedsbeckett.ltitoolset.websocket.ToolEndpointSessionRecordPredicate;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.annotation.HandlesTypes;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import javax.websocket.server.ServerContainer;
import org.apache.commons.lang3.StringUtils;
import uk.ac.leedsbeckett.lti.claims.LtiClaims;
import uk.ac.leedsbeckett.lti.config.LtiConfiguration;
import uk.ac.leedsbeckett.lti.registration.LtiToolConfiguration;
import uk.ac.leedsbeckett.lti.registration.LtiToolConfigurationCustomParameters;
import uk.ac.leedsbeckett.lti.registration.LtiToolConfigurationMessage;
import uk.ac.leedsbeckett.lti.registration.LtiToolRegistration;
import uk.ac.leedsbeckett.lti.state.LtiStateStore;
import uk.ac.leedsbeckett.ltitoolset.annotations.ToolFunctionality;
import uk.ac.leedsbeckett.ltitoolset.annotations.ToolInformation;
import uk.ac.leedsbeckett.ltitoolset.annotations.ToolMapping;
import uk.ac.leedsbeckett.ltitoolset.annotations.ToolSetMapping;
import uk.ac.leedsbeckett.ltitoolset.backchannel.Backchannel;
import uk.ac.leedsbeckett.ltitoolset.backchannel.BackchannelBackgroundWorker;
import uk.ac.leedsbeckett.ltitoolset.backchannel.BackchannelKey;
import uk.ac.leedsbeckett.ltitoolset.backchannel.BackchannelOwner;
import uk.ac.leedsbeckett.ltitoolset.backchannel.JwksBackchannel;
import uk.ac.leedsbeckett.ltitoolset.backchannel.JwksBackchannelKey;
import uk.ac.leedsbeckett.ltitoolset.backchannel.LtiAutoRegistrationBackchannel;
import uk.ac.leedsbeckett.ltitoolset.backchannel.LtiAutoRegistrationBackchannelKey;
import uk.ac.leedsbeckett.ltitoolset.backchannel.LtiBackchannel;
import uk.ac.leedsbeckett.ltitoolset.backchannel.LtiBackchannelKey;
import uk.ac.leedsbeckett.ltitoolset.backchannel.blackboard.BlackboardConfiguration;
import uk.ac.leedsbeckett.ltitoolset.backchannel.blackboard.BlackboardBackchannel;
import uk.ac.leedsbeckett.ltitoolset.backchannel.blackboard.BlackboardBackchannelKey;
import uk.ac.leedsbeckett.ltitoolset.config.LtiConfigurationImpl;
import uk.ac.leedsbeckett.ltitoolset.config.PlatformConfiguration;
import uk.ac.leedsbeckett.ltitoolset.config.PlatformConfigurationStore;
import uk.ac.leedsbeckett.ltitoolset.config.RegistrationConfiguration;
import uk.ac.leedsbeckett.ltitoolset.config.RegistrationConfigurationStore;
import uk.ac.leedsbeckett.ltitoolset.config.ToolConfiguration;
import uk.ac.leedsbeckett.ltitoolset.jwks.JwksStore;
import uk.ac.leedsbeckett.ltitoolset.resources.ToolResourceStore;
import uk.ac.leedsbeckett.ltitoolset.servlet.AutoRegServlet;
import uk.ac.leedsbeckett.ltitoolset.servlet.ToolJwksServlet;
import uk.ac.leedsbeckett.ltitoolset.servlet.ToolLaunchServlet;
import uk.ac.leedsbeckett.ltitoolset.servlet.ToolLoginServlet;
import uk.ac.leedsbeckett.ltitoolset.websocket.MultitonToolEndpoint;
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
public class ToolCoordinator implements ServletContainerInitializer, BackchannelOwner
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

  private String contextPath;
  
  private final HashMap<ToolKey,Tool> toolMap = new HashMap<>();  
  private final HashMap<ToolKey,ToolMapping> toolMappingMap = new HashMap<>();  
  private LtiConfigurationImpl lticonfig;
  private final ToolConfiguration toolconfig = new ToolConfiguration();
  private LtiStateStore<ToolSetLtiState> ltistatestore;
  private ToolSetMapping toolSetMapping = null;

  // Service call signing stuff:
  private String kid;
  private RSAPublicKey publicKey;
  private String myJwks;
  private PrivateKey privateKey;
  
  // WebSocket Endpoint related stuff
  private final HashMap<PlatformResourceKey,HashMap<String,ToolEndpointSessionRecord>> wssessionlistmap = new HashMap<>();
  private final HashMap<String,ToolEndpointSessionRecord> allWsSessions = new HashMap<>();
  OpenSessionPredicate opensessionpredicate = new OpenSessionPredicate();

  // Blackboard specific
  private boolean usingBlackboardRest = false;
  private BlackboardConfiguration blackboardconfig;

  private final BackchannelBackgroundWorker bcbworker = new BackchannelBackgroundWorker();
  private final HashMap<BackchannelKey,Backchannel> backchannelmap = new HashMap<>();

  private JwksStore jwksStore;

  private PlatformConfigurationStore platformConfigurationStore;
  private RegistrationConfigurationStore registrationConfigurationStore;

  private ToolResourceStore toolResourceStore;
  
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
   
    contextPath = ctx.getContextPath();
    
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

    // This has to be early because the web proxy setting is needed in 
    // other initialization stuff.
    initToolConfiguration( ctx );
    
    initRegAndPlatformConfig( ctx );
    initJkwsStore( ctx );
    initToolResourceStore( ctx );
    initLtiConfiguration( ctx );
    initLtiStateStore();
    initServiceKeyPairs();
    
    if ( usingBlackboardRest )
      initBlackboardRest( ctx );
    
    ctx.addListener( new InnerServletContextListener() );
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
      if ( tool.usesBlackboardRest() )
        usingBlackboardRest = true;
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
  
  public String getToolSetName()
  {
    if ( toolSetMapping != null )
      return toolSetMapping.name();
    return "Unknown LTI Tools";
  }
  
  /**
   * Deploy the LTI login and launch servlets based on annotations from the
   * toolSetMapping annotation.
   * 
   * @param ctx The servlet context within which the servlets will be deployed.
   */
  private void initServlets( ServletContext ctx )
  {
    ServletRegistration loginReg  = ctx.addServlet( "ToolLoginServlet",   ToolLoginServlet.class );
    ServletRegistration launchReg = ctx.addServlet( "ToolLaunchServlet",  ToolLaunchServlet.class );
    ServletRegistration jwksReg   = ctx.addServlet( "ToolJwksServlet",    ToolJwksServlet.class );
    ServletRegistration ariReg    = ctx.addServlet("AutoRegInitServlet", AutoRegServlet.class );
    
    loginReg.addMapping(  toolSetMapping.loginUrl()       );
    launchReg.addMapping( toolSetMapping.launchUrl()      );
    jwksReg.addMapping(   toolSetMapping.jwksUrl()        );
    ariReg.addMapping(    toolSetMapping.autoRegUrl() );
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
   * Find a tool mapping object based on type and id.
   * 
   * @param type The tool type.
   * @param id The tool id.
   * @return The tool mapping or null;
   */
  public ToolMapping getToolMapping( String type, String id )
  {
    return toolMappingMap.get( new ToolKey( type, id ) );
  }
  
  /**
   * Get a list of the keys of all the tools.
   * 
   * @return A set of keys to the tools.
   */
  public Set<ToolKey> getToolKeys()
  {
    return this.toolMap.keySet();
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
   * Find a tool object based on type and id.
   * 
   * @param type The tool type.
   * @param id The tool id.
   * @return The tool mapping or null;
   */
  public Tool getTool( String type, String id )
  {
    return toolMap.get( new ToolKey( type, id ) );
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
  
  private void initRegAndPlatformConfig( ServletContext context )
  {
    registrationConfigurationStore = new RegistrationConfigurationStore( Paths.get( context.getRealPath( "/WEB-INF/registrations/" ) ) );
    platformConfigurationStore     = new PlatformConfigurationStore( Paths.get( context.getRealPath( "/WEB-INF/platforms/" ) ) );
  }
  
  /** 
   * Called after launch request has been validated.Allows tool set to veto specific
   *. issuers or platform names.
   * 
   * @param lticlaims The incoming validated claims.
   * @param state Our current tool set state.
   * @return Is the requesting platform allowed to launch?
   * @throws uk.ac.leedsbeckett.ltitoolset.LaunchDisallowedException Exception indicates reason for denial of access.
   */
  public boolean isPlatformAllowedLaunch( LtiClaims lticlaims, ToolSetLtiState state )
          throws LaunchDisallowedException
  {
    return isPlatformAllowedLaunchOrDeepLink( lticlaims, state, false );
  }
  
  /** 
   * Called after launch request has been validated.Allows tool set to veto specific
   * issuers or platform names.
   * 
   * @param lticlaims The incoming validated claims.
   * @param state Our current tool set state.
   * @return Is the requesting platform allowed to deep link?
   * @throws uk.ac.leedsbeckett.ltitoolset.LaunchDisallowedException Exception indicates reason for denial of access.
   */
  public boolean isPlatformAllowedDeepLink( LtiClaims lticlaims, ToolSetLtiState state )
          throws LaunchDisallowedException
  {
    return isPlatformAllowedLaunchOrDeepLink( lticlaims, state, true );
  }
  
  /** 
   * Called after launch request has been validated. Allows tool set to veto specific
   * issuers or platform names.
   * 
   * @param lticlaims
   * @param state
   * @return If no exception thrown returns true
   * @throws uk.ac.leedsbeckett.ltitoolset.LaunchDisallowedException 
   */
  private boolean isPlatformAllowedLaunchOrDeepLink( LtiClaims lticlaims, ToolSetLtiState state, boolean deeplink )
          throws LaunchDisallowedException
  {
    if ( registrationConfigurationStore == null )
      throw new LaunchDisallowedException( "No registration store available" );

    String issuer = lticlaims.getIssuer();
    
    RegistrationConfiguration regconf = registrationConfigurationStore.getRegistrationConfiguration( issuer );
    if ( regconf == null )
      throw new LaunchDisallowedException( "No registration configuration found." );
    
    if ( !regconf.isRegistrationAllowed() )
      throw new LaunchDisallowedException( "Registration entry says registration not allowed." );
    
    if ( deeplink && !regconf.isDeepLinkingAllowed() )
      throw new LaunchDisallowedException( "Deep linking not allowed for this registered authorisation server." );
    
    if ( regconf.isAnyPlatformAllowed() )
      return true;
    
    if ( platformConfigurationStore == null )
      throw new LaunchDisallowedException( "No platform configuration store available." );

    String platformurl = null;
    String platformguid = null;
    if ( lticlaims.getLtiToolPlatform() != null )
    {
      platformurl = lticlaims.getLtiToolPlatform().getUrl();
      platformguid = lticlaims.getLtiToolPlatform().getGuid();
    }

    if ( platformurl == null && platformguid == null )
      throw new LaunchDisallowedException( deeplink?
              "No platform url or guid provided in deep linking request.":
              "No platform url or guid provided in launch request." );
    
    PlatformConfiguration platconf = platformConfigurationStore.getPlatformConfigurationByUrl( issuer, platformurl );
    if ( platconf == null )
    {
      platconf = platformConfigurationStore.getPlatformConfigurationByGuid( issuer, platformguid );
      if ( platconf == null )
        throw new LaunchDisallowedException( "Specified platform not found in configuration store." );
    }
      
    if ( !platconf.isLtiLaunchAllowed() )
      throw new LaunchDisallowedException( "Platform is not allowed to run launches or deep linking requests." );
    
    return true;
  }
  
  /**
   * Load the LTI configuration file from a standard location.
   * 
   * @param context 
   */
  private void initJkwsStore( ServletContext context )
  {
    // Don't bother releasing it because this object lasts forever (almost)
    JwksBackchannel jwksbc = (JwksBackchannel)getBackchannel( this, JwksBackchannelKey.getInstance(), null );
    jwksStore = new JwksStore( Paths.get( context.getRealPath( "/WEB-INF/jwks/" ) ), jwksbc );
  }

  public JwksStore getJwksStore()
  {
    return jwksStore;
  }
  
  private void initToolResourceStore( ServletContext context )
  {
    toolResourceStore = new ToolResourceStore( Paths.get( context.getRealPath( "/WEB-INF/resources/" ) ) );
  }
  
  public ToolResourceStore getToolResourceStore()
  {
    return toolResourceStore;
  }
  
  public RegistrationConfigurationStore getRegistrationConfigurationStore()
  {
    return registrationConfigurationStore;
  }
  
  /**
   * Load the LTI configuration file from a standard location.
   * 
   * @param context 
   */
  private void initLtiConfiguration( ServletContext context )
  {
    String configpath = context.getRealPath( "/WEB-INF/lti/" );
    logger.log( Level.INFO, "Loading LTI configuration from: {0}", configpath );
    
    if ( !StringUtils.isEmpty( configpath ) )
    {
      lticonfig = new LtiConfigurationImpl( Paths.get( context.getRealPath( "/WEB-INF/lti/" ) ) );
      jwksStore.startRefreshing();
      lticonfig.setJwksSigningKeyResolver( jwksStore );
    }
  }  
  
  /**
   * Load the LTI configuration file from a standard location.
   * 
   * @param context 
   */
  private void initToolConfiguration( ServletContext context )
  {
    String configpath = context.getRealPath( "/WEB-INF/toolconfig.json" );
    logger.log( Level.INFO, "Loading Tool configuration from: {0}", configpath );
    if ( !StringUtils.isEmpty( configpath ) )
    {
      toolconfig.load( configpath );
      logger.log( Level.INFO, "Raw configuration: {0}", toolconfig.getRawConfiguration() );
    }
  }  
  
  /**
   * Load the LTI configuration file from a standard location.
   * 
   * @param context 
   */
  private void initBlackboardRest( ServletContext context )
  {
    String configpath = context.getRealPath( "/WEB-INF/blackboard.json" );
    logger.log( Level.INFO, "Loading Blackboard configuration from: {0}", configpath );
    if ( !StringUtils.isEmpty( configpath ) )
    {
      blackboardconfig = new BlackboardConfiguration();
      blackboardconfig.load( configpath );
    }
  }  
  
  public String getLaunchUrl()
  {
    return "https://" + toolconfig.getHostName() + this.contextPath + toolSetMapping.launchUrl();
  }
  
  public LtiToolRegistration createToolRegistration()
  {
    String uribase = "https://" + toolconfig.getHostName() + this.contextPath;
    LtiToolRegistration toolreg = new LtiToolRegistration();
    LtiToolConfiguration config = new LtiToolConfiguration();
    LtiToolConfigurationCustomParameters customparams = new LtiToolConfigurationCustomParameters();
        
    toolreg.setApplicationType( "web" );
    toolreg.setGrantTypes( new String[] {"implicit", "client_credentials" } );
    toolreg.setInitiateLoginUri( uribase + toolSetMapping.loginUrl() );
    toolreg.setRedirectUris( new String[] { uribase + toolSetMapping.launchUrl() } );
    toolreg.setClientName( getToolSetName() );
    toolreg.setJwksUri( uribase + toolSetMapping.jwksUrl() );
    toolreg.setTokenEndpointAuthMethod( "private_key_jwt" );
    toolreg.setLtiToolConfiguration( config );
    
    config.setDomain( toolconfig.getHostName() );
    config.setTargetLinkUri( uribase + toolSetMapping.launchUrl() );
    config.setClaims( new String[] {"iss", "sub","name", "given_name", "family_name"} );
    config.setDescription( "This description should be customised by the app." );
    config.setCustomParameters( customparams );

    LtiToolConfigurationMessage[] messages = new LtiToolConfigurationMessage[1];
    messages[0] = new LtiToolConfigurationMessage();
    messages[0].setType( "LtiDeepLinkingRequest" );
    messages[0].setTargetLinkUri( uribase + toolSetMapping.launchUrl() );
    messages[0].setLabel( getToolSetName() + " Deep Link" );
    config.setMessages( messages );
    
    return toolreg;
  }
  
  
  
  
  public Backchannel getBackchannel( BackchannelOwner owner, BackchannelKey key, ToolSetLtiState state )
  {    
    synchronized ( backchannelmap )
    {
      Backchannel b = backchannelmap.get( key );
      if ( b != null )
      {
        b.addOwner( owner );
        return b;
      }
      
      if ( key instanceof BlackboardBackchannelKey )
      {
        if ( !usingBlackboardRest )
        {
          logger.log( Level.WARNING, "A Blackboard platform was requested but no tools declared a need for one." );
          return null;
        }
        b = new BlackboardBackchannel( (BlackboardBackchannelKey)key, blackboardconfig.getId(), blackboardconfig.getSecret() );
      }
      
      if ( key instanceof LtiBackchannelKey )
      {
        b = new LtiBackchannel( 
                key, 
                lticonfig.getClientLtiConfiguration( state.getClientKey() ).getAuthTokenUrl(),
                state.getClientId(),
                this.kid,
                this.privateKey );
      }
      
      if ( key instanceof LtiAutoRegistrationBackchannelKey )
      {
        b = new LtiAutoRegistrationBackchannel( key );
      }
      
      if ( key instanceof JwksBackchannelKey )
      {
        b = new JwksBackchannel();
      }

      if ( b != null )
      {
        backchannelmap.put( key, b );
        bcbworker.registerBackchannel( b );
        if ( !StringUtils.isBlank( toolconfig.getBackchannelProxy() ) )
        {
          logger.log(Level.INFO, "Setting proxy to {0}", toolconfig.getBackchannelProxy());
          b.setHttpsProxyUrl( toolconfig.getBackchannelProxy() );
        }
        b.setDevelopmentTrustAllServersMode( toolconfig.isDevelopmentTrustAllServersMode() );
        b.addOwner( owner );
      }      
 
      return b;
    }
  }
  
  public void releaseBackchannels( BackchannelOwner owner )
  {
    synchronized ( backchannelmap )
    {
      Backchannel b;
      ArrayList<BackchannelKey> removalList = new ArrayList<>();
      for ( BackchannelKey key : backchannelmap.keySet() )
      {
        b = backchannelmap.get( key );
        if ( b.isOwner( owner ) )
          b.removeOwner( owner );
        if ( !b.hasOwners() )
          removalList.add( key );
      }
      for ( BackchannelKey key : removalList )
      {
        b = backchannelmap.get( key );
        backchannelmap.remove( key );
        bcbworker.unregisterBackchannel( b );
      }
    }    
  }
  
  
  /**
   * Get the LTI configuration.
   * @return The config object.
   */
  public LtiConfigurationImpl getLtiConfiguration()
  {
    return lticonfig;
  }  
  
  /**
   * Simple implementation for time being - generate one key pair at startup
   * and don't bother storing it. Make a JSON representation of the public key.
   */
  private void initServiceKeyPairs()
  {
    // Use JJWT library to create a suitable key pair for the signing algo
    // we intend to use.
    KeyPair pair = Keys.keyPairFor( SignatureAlgorithm.RS256 );
    if ( pair.getPublic() instanceof RSAPublicKey )
    {
      logger.log( Level.FINE, "Setting up RSA key pair for use." );
      this.kid = UUID.randomUUID().toString();
      this.publicKey = (RSAPublicKey) pair.getPublic();
      this.privateKey = pair.getPrivate();
      
      Map<String, Object> values = new HashMap<>();
      values.put("kty", publicKey.getAlgorithm()); // getAlgorithm() returns kty not algorithm
      values.put("kid", kid );
      values.put("n", Base64.getUrlEncoder().encodeToString(publicKey.getModulus().toByteArray()));
      values.put("e", Base64.getUrlEncoder().encodeToString(publicKey.getPublicExponent().toByteArray()));
      values.put("alg", "RS256");
      values.put("use", "sig");
      
      ObjectMapper mapper = new ObjectMapper();
      try      
      {
        myJwks = "{\"keys\":[" + mapper.writerWithDefaultPrettyPrinter().writeValueAsString( values ) + "]}";
        logger.log(Level.FINE, myJwks );
      }
      catch ( JsonProcessingException ex )
      {
        logger.log( Level.SEVERE, "Failed to convert public key to JSON.", ex );
      }
    }
  }
  
  public String getServiceJwks()
  {
    if ( myJwks != null ) return myJwks;
    return "{\"keys\":[]}";
  }
  
  public PublicKey getPublicKey()
  {
    return publicKey;
  }
  
  public PrivateKey getPrivateKey()
  {
    return privateKey;
  }
  
  public String getKeyId()
  {
    return kid;
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
      for ( PlatformResourceKey key : wssessionlistmap.keySet() )
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
  public void addWsSession( MultitonToolEndpoint endpoint, Session session )
  {
    synchronized ( wssessionlistmap )
    {
      if ( endpoint == null )
        throw new IllegalArgumentException( "Endpoint was null" );
      if ( endpoint.getToolState() == null )
        throw new IllegalArgumentException( "Endpoint toolstate was null" );
      PlatformResourceKey key = endpoint.getToolState().getResourceKey();
      if ( endpoint.getToolState().getResourceKey() == null )
        throw new IllegalArgumentException( "Endpoint toolstate resource key was null" );
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
  public void removeWsSession( MultitonToolEndpoint endpoint )
  {
    synchronized ( wssessionlistmap )
    {
      allWsSessions.remove( endpoint.getStateid() );
      PlatformResourceKey key = endpoint.getToolState().getResourceKey();
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
  public Set<Session> getWsSessionsForResource( PlatformResourceKey key )
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
  
  public class InnerServletContextListener implements ServletContextListener
  {
    @Override
    public void contextInitialized( ServletContextEvent sce )
    {
    }

    @Override
    public void contextDestroyed( ServletContextEvent sce )
    {
      logger.fine( "Serlvet context is being destroyed." );
      jwksStore.stopRefreshing();
    }
  }
}
