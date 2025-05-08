/*
 * Copyright 2025 maber01.
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
package uk.ac.leedsbeckett.ltitoolset.blobex;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import uk.ac.leedsbeckett.ltitoolset.util.ThreeStringKey;
import uk.ac.leedsbeckett.ltitoolset.util.TwoStringKey;

/**
 *
 * @author maber01
 */
public class BlobExchanger
{
  public static final String HEADER_BLOBID    = "X-LBU-BlobId";
  public static final String HEADER_NONCE     = "X-LBU-Nonce";
  public static final String HEADER_SESSIONID = "X-LBU-SessionId";
  
  final Random random = new Random( System.currentTimeMillis() );

  String servletUrl;

  final HashMap<String,String> nonceTable = new HashMap<>();
  final HashMap<BlobRecordKey,BlobRecord> blobTable = new HashMap<>();

  long lastPruning = 0L;
  
  public String getServletUrl()
  {
    return servletUrl;
  }

  public void setServletUrl( String servletUrl )
  {
    this.servletUrl = servletUrl;
  }
  
  public synchronized String registerSession( String sessionId )
  {
    byte[] noncebytes = new byte[32];
    random.nextBytes( noncebytes );
    BigInteger bignonce = new BigInteger( 1, noncebytes );
    StringBuilder sb = new StringBuilder();
    sb.append( bignonce.toString( 16 ) );
    while ( sb.length() < 64 )
      sb.append( "0" );
    String nonce = sb.toString();
    nonceTable.put( sessionId, nonce );
    return nonce;
  }
  
  public synchronized boolean isCorrectNonce( String sessionId, String nonce )
  {
    if ( nonce == null ) return false;
    return nonce.equals( nonceTable.get( sessionId ) );
  }
  
  public void putBlob( String sessionId, String blobId, byte[] data )
          throws BlobExException
  {
    BlobRecordKey key = new BlobRecordKey( sessionId, blobId );
    if ( blobTable.containsKey( key ) )
      throw new BlobExException( "Blob key already exists in exchanger table." );
    BlobRecord record = new BlobRecord( key, data );
    blobTable.put( key, record );
  }
  
  public synchronized byte[] getBlob( String sessionId, String blobId )
  {
    BlobRecordKey key = new BlobRecordKey( sessionId, blobId );
    BlobRecord record = blobTable.get( key );
    if ( record == null ) return null;
    // Can only get once - remove right away
    blobTable.remove( key );
    conditionalPruneBlobs();
    return record.getData();
  }

  private synchronized void conditionalPruneBlobs()
  {
    long now = System.currentTimeMillis();
    if ( (now-lastPruning) > 10000 )
      pruneBlobs();
    lastPruning = now;
  }
  
  private synchronized void pruneBlobs()
  {
    long now = System.currentTimeMillis();
    LinkedList<BlobRecordKey> keysToDelete = new LinkedList<>();
    for ( BlobRecord r : blobTable.values() )
      if ( (now - r.timestamp) > 5000 )
        keysToDelete.add( r.getKey() );
    for ( BlobRecordKey key : keysToDelete )
      blobTable.remove( key );
  }
  
  class BlobRecordKey extends TwoStringKey
  {
    public BlobRecordKey( 
            @JsonProperty("sessionId") String sessionId, 
            @JsonProperty("blobId")    String blobId )
    {
      super( sessionId, blobId );
    }
    
    public String getSessionId()
    {
      return getA();
    }

    public String getBlobId()
    {
      return getB();
    }
  }
  
  class BlobRecord
  {
    BlobRecordKey key;
    byte[] data;
    long timestamp;

    public BlobRecord( BlobRecordKey key, byte[] data )
    {
      this.key = key;
      this.data = data;
      timestamp = System.currentTimeMillis();
    }

    public BlobRecordKey getKey()
    {
      return key;
    }

    public byte[] getData()
    {
      return data;
    }    
  }
}
