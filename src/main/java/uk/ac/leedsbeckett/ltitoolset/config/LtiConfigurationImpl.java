/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.ac.leedsbeckett.ltitoolset.config;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.leedsbeckett.lti.config.ClientLtiConfiguration;
import uk.ac.leedsbeckett.lti.config.ClientLtiConfigurationKey;
import uk.ac.leedsbeckett.lti.config.LtiConfiguration;
import uk.ac.leedsbeckett.lti.config.JsonKeyBuilder;
import uk.ac.leedsbeckett.lti.jwks.JwksSigningKeyResolver;
import uk.ac.leedsbeckett.ltitoolset.store.Store;

/**
 *
 * @author jon
 */
public class LtiConfigurationImpl extends Store<ClientLtiConfigurationKey,ClientLtiConfigurationImpl> implements LtiConfiguration
{
  static final Logger logger = Logger.getLogger( LtiConfigurationImpl.class.getName() );

  Path basepath;

  JwksSigningKeyResolver jwksResolver = null;  
  
  public LtiConfigurationImpl( Path basepath )
  {
    super( "lticonfigurationdatastore" );
    this.basepath = basepath;
    try
    {
      Files.createDirectories( basepath );
    }
    catch (IOException ex)
    {
      logger.log(Level.SEVERE, null, ex);
    }
  }
  
  public JwksSigningKeyResolver getJwksSigningKeyResolver()
  {
    return jwksResolver;
  }

  public void setJwksSigningKeyResolver( JwksSigningKeyResolver jwksResolver )
  {
    this.jwksResolver = jwksResolver;
  }

  public List<String> getAllJksUrls()
  {
    ArrayList<String> list = new ArrayList<>();
//    for ( IssuerLtiConfiguration iss : issuermap.values() )
//      for ( ClientLtiConfigurationImpl client : iss.clientmap.values() )
//      {
//        if ( client.getAuthJwksUrl() != null )
//          list.add( client.getAuthJwksUrl() );
//      }
    return list;
  }
  
  
  @Override
  public ClientLtiConfigurationImpl create( ClientLtiConfigurationKey key )
  {
    ClientLtiConfigurationImpl item = new ClientLtiConfigurationImpl();
    item.setKey( key );
    item.setClientId( key.getClientId() );
    item.setIssuerId( key.getIssuerName() );
    return item;
  }

  @Override
  public Class<ClientLtiConfigurationImpl> getEntryClass()
  {
    return ClientLtiConfigurationImpl.class;
  }

  @Override
  public Path getPath( ClientLtiConfigurationKey key )
  {
    Path issuer = basepath.resolve( URLEncoder.encode( key.getIssuerName(), StandardCharsets.UTF_8 ) );
    Path client = issuer.resolve( URLEncoder.encode( key.getClientId(), StandardCharsets.UTF_8 ) );
    logger.log(Level.INFO, "Path of LTI client config file: {0}", client.toString());
    return client;
  }

  @Override
  public ClientLtiConfiguration getClientLtiConfiguration( ClientLtiConfigurationKey clientkey )
  {
    ClientLtiConfigurationImpl clc = get( clientkey, false );
    if ( clc != null )
      clc.setJwksresolver( jwksResolver );
    return clc;
  }

  @Override
  public ClientLtiConfiguration getClientLtiConfiguration( String issuername, String client_id )
  {
    ClientLtiConfigurationKey clientkey = new ClientLtiConfigurationKey( issuername, client_id );
    return getClientLtiConfiguration( clientkey );
  }
  
//  /**
//   * Load a configuration file in JSON format.
//   * 
//   * @param strpathconfig The file name (path) to load.
//   */
//  public void load( String strpathconfig )
//  {
//    this.strpathconfig = strpathconfig;
//    issuermap.clear();
//    
//    try
//    {
//      rawconfig = FileUtils.readFileToString( new File( strpathconfig ), StandardCharsets.UTF_8 );      
//      ObjectMapper mapper = new ObjectMapper();
//      JsonFactory factory = mapper.getFactory();
//      JsonParser parser = factory.createParser( rawconfig );
//      JsonNode node = mapper.readTree(parser);
//      if ( node.isObject() )
//      {
//        logger.fine( "LtiConfiguration loading base JSON object." );
//        JsonNode issuersnode = node.get( "issuers" );
//        if ( issuersnode!=null && issuersnode.isArray() )
//          for ( Iterator<JsonNode> it = issuersnode.elements(); it.hasNext(); )
//            loadIssuer( it.next() );
//      }
//    }
//    catch ( FileNotFoundException ex )
//    {
//      Logger.getLogger(LtiConfiguration.class.getName() ).log( Level.SEVERE, null, ex );
//    } catch ( IOException ex )
//    {
//      Logger.getLogger(LtiConfiguration.class.getName() ).log( Level.SEVERE, null, ex );
//    }
//  }
  
//  /**
//   * Load an issuer from a node within the JSON file.
//   * 
//   * @param issuernode The node containing an issuer.
//   */
//  void loadIssuer( JsonNode issuernode )
//  {
//    String name = issuernode.get( "name" ).asText();
//    logger.log(Level.FINE, "LtiConfiguration.loadIssuer() name = {0}", name );
//    JsonNode clients = issuernode.get( "clients" );
//    IssuerLtiConfiguration issuer = new IssuerLtiConfiguration( name );
//    issuermap.put( name, issuer );
//    if ( !clients.isArray() )
//      return;
//    for ( Iterator<JsonNode> it = clients.elements(); it.hasNext(); )
//    {
//      JsonNode clientnode = it.next();
//      if ( clientnode.isObject() )
//        loadClient( issuer, clientnode );
//    }
//  }
  
  /**
   * Load a client (tool) from within an issuer (e.g. blackboard.com) configuration.
   * 
   * @param issuer The issuer to load into.
   * @param clientnode The JSON node containing the config.
   */
//  void loadClient( IssuerLtiConfiguration issuer, JsonNode clientnode )
//  {
//    String clientid = clientnode.get( "client_id" ).asText();
//    logger.log(Level.FINE, "LtiConfiguration.loadClient() client_id = {0}", clientid );
//    ClientLtiConfigurationImpl client = issuer.createClientLtiConfiguration( clientid );
//    
//    client.setDefault(        clientnode.get( "default" ).asBoolean()          );
//    client.setAuthLoginUrl(   clientnode.get( "auth_login_url" ).asText()      );
//    client.setAuthTokenUrl(   clientnode.get( "auth_token_url" ).asText()      );
//    client.setAuthJwksUrl(    clientnode.get( "auth_jwks_url" ).asText()      );
//
//    if ( clientnode.has( "keys" ) )
//    {
//      JsonNode keysnode = clientnode.get( "keys" );
//      if ( keysnode.isArray() )
//      {
//        for ( Iterator<JsonNode> it = keysnode.elements(); it.hasNext(); )
//        {
//          JsonNode knode = it.next();
//          if ( !knode.isObject() )
//            continue;
//          try
//          {
//            client.putKeyConfiguration( JsonKeyBuilder.build( knode ) );
//          }
//          catch ( Exception e )
//          {
//            logger.log( Level.SEVERE, "Unable to load public key for this client.", e );
//          }
//        }
//      }
//    }    
//    JsonNode depnodea = clientnode.get( "deployment_ids" );
//    ArrayList<String> list = new ArrayList<>();
//    if ( depnodea.isArray() )
//    {
//      for ( Iterator<JsonNode> it = depnodea.elements(); it.hasNext(); )
//      {
//        JsonNode depnode = it.next();
//        if ( depnode.isTextual() && !StringUtils.isEmpty( depnode.asText() ) )
//          list.add( depnode.asText() );
//      }
//    }
//    client.setDeploymentIds( list.toArray( new String[list.size()] ) );
//    issuer.putClientLtiConfiguration( client );
//  }   


  
}
