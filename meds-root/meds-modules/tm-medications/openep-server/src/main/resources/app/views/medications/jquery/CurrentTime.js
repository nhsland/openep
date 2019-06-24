Class.define('CurrentTime', 'tm.jquery.Object', {

      /** statics */
      statics: {
        _offset: null,

        get: function()
        {
          if (this._offset)
          {
            var clientTimeInMillis = new Date().getTime();
            return new Date(clientTimeInMillis + this._offset);
          }
          else
          {
            return new Date();
          }
        },

        setOffset: function(offset)
        {
          this._offset = offset;
        }
      }
    }
);
