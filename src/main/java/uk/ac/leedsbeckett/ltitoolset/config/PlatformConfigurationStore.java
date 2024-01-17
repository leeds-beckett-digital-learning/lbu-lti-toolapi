/*
 * Copyright 2024 maber01.
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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.logging.Logger;
import uk.ac.leedsbeckett.ltitoolset.store.Store;

/**
 *
 * @author maber01
 */
public class PlatformConfigurationStore extends Store<String,PlatformConfigurationEntry>
{
  static final Logger logger = Logger.getLogger(PlatformConfigurationStore.class.getName() );

  Path basepath;
  
  public PlatformConfigurationStore( Path basepath )
  {
    super( "platforms" );
    this.basepath = basepath;
  }  
  
  public PlatformConfiguration getPlatformConfiguration( String key )
  {
    PlatformConfigurationEntry entry = this.get( key, false );
    if ( entry == null ) return null;
    return entry.getPlatformConfiguration();
  }
  
  @Override
  public PlatformConfigurationEntry create( String key )
  {
    return new PlatformConfigurationEntry( key );
  }

  @Override
  public Class<PlatformConfigurationEntry> getEntryClass()
  {
    return PlatformConfigurationEntry.class;
  }

  @Override
  public Path getPath( String key )
  {
    return basepath.resolve( URLEncoder.encode( key, StandardCharsets.UTF_8 ) );
  }

}
