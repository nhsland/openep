Class.define('app.views.medications.timeline.TherapyTimelineUtils', 'tm.jquery.Object', {
      /** statics */
      statics: {
        /* limits the redraw, only triggered after 50px move */
        overrideTimelinePanMove: function(timeline)
        {
          timeline.removeAllListeners('panmove');
          timeline.on('panmove', function(event)
          {
            // See Range.prototype._onDrag in VisJS's source on why we return if no event is passed.
            // After the handler is done it emits a 'panmove' again, and they avoid the circular handling by not providing
            // the original event.
            if (!event)
            {
              return;
            }

            var self = this;
            this.range.props.touch.dragging = true;

            var direction = this.range.options.direction;
            var delta = direction === 'horizontal' ? event.deltaX : event.deltaY;
            delta -= this.range.deltaDifference;

            // we ignore moving for each 25px to reduce the amount of redraws, but since fast swipes can cause
            // an overload in redraws, we also add a small timer delay, to reduce the amount of redraws when
            // velocity is high .. could probably use hammer.js's velocity in the px calculation?
            if (Math.abs(delta - this.range.previousDelta) > 25)
            {
              clearTimeout(timeline._moveTimer);
              timeline._moveTimer = setTimeout(function()
              {
                self.range._onDrag.call(self.range, event);
              }, 5);
            }
          });
        },
        /* zoom only works when using the ALT key */
        overrideTimelineOnMouseWheel: function(timeline)
        {
          timeline.removeAllListeners('mousewheel');
          timeline.on('mousewheel', function(event)
          {
            if (event.altKey === true)
            {
              this.range._onMouseWheel.call(this.range, event);
            }
          });
        }
      }
    }
);
