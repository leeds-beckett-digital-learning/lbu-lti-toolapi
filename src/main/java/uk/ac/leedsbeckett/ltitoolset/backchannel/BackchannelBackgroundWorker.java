/*
 * Copyright 2023 maber01.
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
package uk.ac.leedsbeckett.ltitoolset.backchannel;

import java.util.HashSet;

/**
 * Object is intended to refresh auth tokens in the background by
 * monitoring token timeouts. One instance per web app to work on all
 * backchannels in turn.
 * 
 * @author maber01
 */
public class BackchannelBackgroundWorker extends Thread
{
  private final HashSet<Backchannel> backchannelSet = new HashSet<>();
  
  public void registerBackchannel( Backchannel backchannel )
  {
    synchronized ( backchannelSet )
    {
      backchannelSet.add( backchannel );
    }
  }
  
  public void unregisterBackchannel( Backchannel backchannel )
  {
    synchronized ( backchannelSet )
    {
      backchannelSet.remove( backchannel );
    }
  }
  
  @Override
  public void run()
  {
  }  
}
