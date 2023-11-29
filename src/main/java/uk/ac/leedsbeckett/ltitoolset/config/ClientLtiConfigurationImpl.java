/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.ac.leedsbeckett.ltitoolset.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import java.io.Serializable;
import java.security.Key;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.leedsbeckett.lti.config.ClientLtiConfiguration;
import uk.ac.leedsbeckett.lti.config.ClientLtiConfigurationKey;
import uk.ac.leedsbeckett.lti.config.KeyConfiguration;
import uk.ac.leedsbeckett.lti.jwks.JwksSigningKeyResolver;
import uk.ac.leedsbeckett.ltitoolset.store.Entry;

/**
 * Represents the configuration of an LTI 1.3 client. I.e. information about
 * an LTI tool which has been advertised to the rest of the world.
 *
 * @author jon
 */
public class ClientLtiConfigurationImpl implements Entry<ClientLtiConfigurationKey>, ClientLtiConfiguration, Serializable
{
  static final Logger logger = Logger.getLogger(ClientLtiConfigurationImpl.class.getName() );
  
  ClientLtiConfigurationKey entrykey;
  
  String issuerId;
  String clientId;
  String authLoginUrl;
  String authTokenUrl;
  String authJwksUrl;
  HashMap<String, KeyConfiguration> keyConfigurationMap;
  String[] deploymentIds;

  JwksSigningKeyResolver jwksresolver;

  @JsonIgnore
  public JwksSigningKeyResolver getJwksresolver()
  {
    return jwksresolver;
  }

  @JsonIgnore
  public void setJwksresolver( JwksSigningKeyResolver jwksresolver )
  {
    this.jwksresolver = jwksresolver;
  }
  
  @Override
  public String getIssuerId()
  {
    return issuerId;
  }

  public void setIssuerId( String issuerId )
  {
    if ( this.issuerId != null )
      throw new IllegalArgumentException( "Cannot change issuer ID of a client config." );
    this.issuerId = issuerId;
  }
  
  @Override
  public String getClientId()
  {
    return clientId;
  }

  public void setClientId( String clientId )
  {
    if ( this.clientId != null )
      throw new IllegalArgumentException( "Cannot change client ID of a client config." );
    this.clientId = clientId;
  }

  @Override
  public String getAuthLoginUrl()
  {
    return authLoginUrl;
  }

  public void setAuthLoginUrl( String authLoginUrl )
  {
    this.authLoginUrl = authLoginUrl;
  }

  @Override
  public String getAuthTokenUrl()
  {
    return authTokenUrl;
  }

  public void setAuthTokenUrl( String authTokenUrl )
  {
    this.authTokenUrl = authTokenUrl;
  }

  @Override
  public String getAuthJwksUrl()
  {
    return authJwksUrl;
  }

  public void setAuthJwksUrl( String authJwksUrl )
  {
    this.authJwksUrl = authJwksUrl;
  }

  public HashMap<String, KeyConfiguration> getKeyConfigurationMap()
  {
    return keyConfigurationMap;
  }

  public void setKeyConfigurationMap( HashMap<String, KeyConfiguration> keyConfigurationMap )
  {
    this.keyConfigurationMap = keyConfigurationMap;
  }
  
  @Override
  public KeyConfiguration getKeyConfiguration( String kid )
  {
    if ( keyConfigurationMap == null )
      return null;
    return keyConfigurationMap.get( kid );
  }

  public void putKeyConfiguration( KeyConfiguration kc )
  {
    if ( keyConfigurationMap == null )
      keyConfigurationMap = new HashMap<>();
    keyConfigurationMap.put( kc.getKid(), kc );
  }

  @Override
  public String[] getDeploymentIds()
  {
    return deploymentIds;
  }

  public void setDeploymentIds( String[] deploymentIds )
  {
    this.deploymentIds = deploymentIds;
  }

  
  
  @Override
  public Key resolveSigningKey( JwsHeader jh, Claims claims )
  {
    return resolveSigningKey( jh );
  }

  @Override
  public Key resolveSigningKey( JwsHeader jh, String string )
  {
    return resolveSigningKey( jh );
  }

  private Key resolveSigningKey( JwsHeader jh )
  {
    logger.log( Level.FINE, "Looking for key with ID {0}", jh.getKeyId() );
    KeyConfiguration kc = this.getKeyConfiguration( jh.getKeyId() );
    if ( kc != null )
    {
      if ( kc.isEnabled() )
        return kc.getKey();
      logger.log( Level.INFO, "LTI signing key found but is not enabled in configuration." );
      return null;
    }
    if ( jwksresolver == null )
    {
      logger.log( Level.SEVERE, "Key not found in client LTI configuration. No Jwks resolver." );
      return null;
    }
    if ( authJwksUrl == null )
    {
      logger.log( Level.SEVERE, "No JWKS URL to search for keys." );
      return null;
    }
    logger.log( Level.FINE, "Looking for key in Jwks resolver." );
    return jwksresolver.resolveSigningKey( authJwksUrl, jh.getKeyId() );
  }

  @Override
  @JsonIgnore
  public ClientLtiConfigurationKey getKey()
  {
    return entrykey;
  }

  @Override
  @JsonIgnore
  public void setKey( ClientLtiConfigurationKey key )
  {
    entrykey = key;
  }

  @Override
  public void initialize()
  {
    keyConfigurationMap = new HashMap<>();
  }
  
}
