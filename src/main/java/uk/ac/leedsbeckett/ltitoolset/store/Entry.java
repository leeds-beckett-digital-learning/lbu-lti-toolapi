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
package uk.ac.leedsbeckett.ltitoolset.store;

/**
 * A template class for entries that will go in a cache backed store.
 * 
 * @author maber01
 * @param <K> The type of key used by the entry.
 */
public abstract interface Entry<K>
{
  /**
   * Get a key for this entry.
   * 
   * @return The key for this entry.
   */
  public K getKey();
  
  /**
   * Set the key for this entry before it is stored.
   * 
   * @param key The key value to set.
   */
  public void setKey( K key );
  
  /**
   * It is assumed that entries will need to be initialized so this will
   * be called.
   */
  public void initialize();
}
