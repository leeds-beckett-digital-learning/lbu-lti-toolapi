/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.ac.leedsbeckett.ltitoolset.resources;

import java.io.Serializable;
import uk.ac.leedsbeckett.ltitoolset.store.Entry;

/**
 * A caching store entry keyed with ToolResourceKey and containing ToolResourceRecord
 * @author jon
 */
public class ToolResourceRecordEntry implements Entry<ToolResourceKey>, Serializable
{
  ToolResourceKey key;
  ToolResourceRecord record;

  /**
   * Create empty record with key.
   * @param key The key to use for the entry.
   */
  public ToolResourceRecordEntry( ToolResourceKey key )
  {
    this.key = key;
  }
  
  
  /**
   * Simple getter.
   * @return The key
   */
  @Override
  public ToolResourceKey getKey()
  {
    return key;
  }

  /**
   * Simple setter.
   * @param key The key.
   */
  @Override
  public void setKey( ToolResourceKey key )
  {
    if ( this.key != null )
      throw new IllegalArgumentException( "Not allowed to change ToolResourceRecordEntry key." );
    this.key = key;
  }

  /**
   * Simple getter.
   * @return The record
   */
  public ToolResourceRecord getRecord()
  {
    return record;
  }

  /**
   * Simple setter.
   * @param record The record.
   */
  public void setRecord( ToolResourceRecord record )
  {
    this.record = record;
  }
  
  /**
   * Option to initialize here.
   */
  @Override
  public void initialize()
  {
  }
}
