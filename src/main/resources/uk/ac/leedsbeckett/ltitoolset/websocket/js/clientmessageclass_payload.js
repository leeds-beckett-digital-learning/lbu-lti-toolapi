
lib._SUBCLASS_Message = class extends lbultitoolapi.ClientMessage 
{ 
  constructor( _PARAMETERS_ )
  {
    super( "_MESSAGETYPE_", "_PAYLOADTYPE_" );
    this.payload = { _PAYLOAD_ };
  }
};
    
