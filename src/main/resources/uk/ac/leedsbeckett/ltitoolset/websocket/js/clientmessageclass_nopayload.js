
lib._SUBCLASS_Message = class extends lib.ClientMessage 
{ 
  constructor()
  {
    super( "_MESSAGETYPE_", null );
    this.payload = null;
  }
};
    
