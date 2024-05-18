/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.ac.leedsbeckett.ltitoolset.resources;

import java.nio.file.Path;
import uk.ac.leedsbeckett.ltitoolset.store.Store;
import uk.ac.leedsbeckett.ltitoolset.util.UUIDToPath;

/**
 * This store keeps track of resources created by multiton tools. Each
 * resource references a tool and has a unique ID. 
 * 
 * @author jon
 */
public class ToolResourceStore extends Store<ToolResourceKey,ToolResourceRecordEntry>
{
  
  final Path basepath;
  

  /**
   * Creates a resource store in a given folder.
   * 
   * @param basepath The folder where data will be stored.
   */  
  public ToolResourceStore( Path basepath )
  {
    super( "toolresourcestore" );
    this.basepath = basepath;
  }
  
  
  /**
   * There might be many resource records in this store so, to avoid large number of
   * files in each directory the path is broken up into several levels.
   * 
   * @param key The key to convert.
   * @return The path of the file that will contain the data for this key.
   */
  @Override
  public Path getPath( ToolResourceKey key )
  {
    return basepath.resolve( UUIDToPath.getRelativePath( key.getResourceId() ) );
  }

  
  /**
   * Create a new entry using a given key.
   * @param key The key
   * @return The entry
   */
  @Override
  public ToolResourceRecordEntry create( ToolResourceKey key )
  {
    return new ToolResourceRecordEntry( key );
  }

  /**
   * Get the class of object this store works with.
   * @return The class of entries in this store.
   */
  @Override
  public Class<ToolResourceRecordEntry> getEntryClass()
  {
    return ToolResourceRecordEntry.class;
  }
}
