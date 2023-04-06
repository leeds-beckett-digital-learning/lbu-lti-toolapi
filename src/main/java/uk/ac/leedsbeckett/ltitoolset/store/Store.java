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

package uk.ac.leedsbeckett.ltitoolset.store;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;

/**
 * A store of resources which can be retrieved using keys.At present all
 * resources stay in the store until the store is garbage collected after the
 * web application shuts down.All resources are lost entirely at shut down
 * in this demo. A proper implementation would store data on file or in a 
 * database and would purge memory of resources that haven't been used for a
 * while.
 * 
 * @author jon
 * @param <K> The key class.
 * @param <T> The entry class.
 */
public abstract class Store<K,T extends Entry<K>>
{
  static final Logger logger = Logger.getLogger(Store.class.getName() );
  private static final ObjectMapper objectmapper = new ObjectMapper();
  static
  {
    objectmapper.enable( SerializationFeature.INDENT_OUTPUT );
    objectmapper.disable( SerializationFeature.FAIL_ON_EMPTY_BEANS );
    objectmapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
  }
  
  Cache<K,T> cache;
  
  /**
   * Create a store for the stated type of entry, type of key and give it a
   * name.
   * 
   * @param name The name of the store.
   */
  public Store( String name )
  {
    logger.log(Level.FINE, "Caching provider class {0}", Caching.getCachingProvider().getClass().getName() );
    CacheManager manager = Caching.getCachingProvider().getCacheManager();
    MutableConfiguration<K,T> config = new MutableConfiguration<K,T>()
        .setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(Duration.FIVE_MINUTES));
    cache = manager.createCache( name, config );
  }
  
  /**
   * Create an empty entry using a key.
   * 
   * @param key The key to use.
   * @return A new entry.
   */
  public abstract T create( K key );
  
  /**
   * Get a class of the type that matches entries.
   * 
   * @return The class of entries.
   */
  public abstract Class<T> getEntryClass();
  
  /**
   * Get a path into the file system where the data entry will be stored
   * based on the contents of the key.
   * 
   * @param key The key.
   * @return A file system path where the object should be stored.
   */
  public abstract Path getPath( K key );
    
  
  /**
   * Find a resource keyed by platform ID and resource ID with option to
   * create the resource if it doesn't exist yet.
   * 
   * @param key The unique key of the entry.
   * @param create Set true if the resource should be created if it doesn't already exist.
   * @return The resource or null if it wasn't found and creation wasn't requested.
   */
  public synchronized T get( K key, boolean create )
  {
    T r = cache.get( key );
    
    if ( r != null )
    {
      logger.log( Level.FINE, "Found in cache - {0}", key.toString() );
      return r;      
    }
    
    logger.log( Level.FINE, "Not in cache - {0}", key.toString() );
    try
    {
      r = load( key );
      if ( r != null )
      {
        logger.log( Level.FINE, "Loaded so caching - {0}", key.toString() );
        cache.put( key, r );
        if ( !cache.containsKey( key ) )
          logger.log( Level.SEVERE, "But key is still not in the cache {0}", key.toString() );
        return r;
      }
      
      if ( create )
      {
        logger.log( Level.FINE, "Created and saved - {0}", key.toString() );
        r = create( key );
        // an entirely new resource so set it up
        r.initialize();
        save( key, r );
        // save also caches the record so we are done now
        return r;
      }
    }
    catch (IOException ex)
    {
      logger.log(Level.SEVERE, null, ex);
      return null;
    }  
    
    return null;
  }
    
  /**
   * Save the data to disk and update the cache.
   * 
   * @param entry An entry to store and update in the cache.
   * @throws IOException Thrown if there is a problem storing the data.
   */
  public void update( T entry ) throws IOException
  {
    if ( entry.getKey() == null )
      throw new IllegalArgumentException( "Cannot update resource that lacks a key." );
    save( entry.getKey(), entry );
  }

  /**
   * Load and entry from disk.
   * 
   * @param key The unique key to the entry.
   * @return The loaded record or null if it doesn't exist.
   * @throws IOException Thrown if there is a fault loading the data.
   */  
  T load( K key ) throws IOException
  {
    Path filepath = getPath( key );
    if ( Files.exists( filepath ) )
    {
      logger.log( Level.FINE, "Loading data {0}", filepath );
      return objectmapper.readValue( filepath.toFile(), getEntryClass() );
    }
    return null;
  }
  
  /**
   * Save a data record against a key.
   * 
   * @param key The key of the record.
   * @param r The data record to save.
   * @throws IOException Thrown if a problem occurs saving data.
   */
  void save( K key, T r ) throws IOException
  {
    Path filepath = getPath( key );
    Files.createDirectories( filepath.getParent() );
    logger.log( Level.FINE, "Saving data to {0}", filepath );
    objectmapper.writeValue( filepath.toFile(), r );
    cache.put( key, r );
    if ( !cache.containsKey(key) )
      logger.log( Level.SEVERE, "Put resource in cache but key is not present {0}", key.toString() );
  }  
}
