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


/*

===================================
  Generated script, do not edit. 
===================================

*/

import lbultitoolapi from "./endpoint.js";

const ENDPOINTEXPORT = (function () {

  let servermessagenames =
  [
SERVERMESSAGENAMES
  ];
  
  
  let lib = new Object();

  lib.ToolSocket = class extends lbultitoolapi.ToolSocket
  {
    validateHandler()
    {
      console.log( "Validating handler" );
      if ( this.handler.open instanceof Function )
        this.handler.open.validated = true;
      else
        console.warn( "Handler lacks open function." );
      
      for ( let i=0; i<servermessagenames.length; i++ )
      {
        let funcname = 'handle'+servermessagenames[i].name;
        if ( this.handler[funcname] instanceof Function )
          this.handler[funcname].validated = true;
        else
          console.warn( "Handler lacks handler function, " + funcname + "." );          
      }
      
      for ( let o in this.handler )
      {
        if ( this.handler[o] instanceof Function )
        {
          if ( !this.handler[o].validated )
            console.warn( "Handler has additional inessential function, " + o + "." );          
        }
      }
    }
  };
  
  CLASSES

  return lib;
})();


export default ENDPOINTEXPORT;
