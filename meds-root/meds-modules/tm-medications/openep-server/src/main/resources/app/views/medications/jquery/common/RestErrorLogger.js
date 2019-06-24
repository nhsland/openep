/**
 * A simple Ajax based logger, which provides the ability to log uncaught exceptions to the server. There should be only one
 * instance per {@link app.views.common.AppView}. The logger is partially based on the concepts of Sentry.io's RavenJS and
 * the Log4Javascript project. See {@link app.views.medications.common.RestErrorLogger#_configureEventHandler} for more
 * details.
 *
 * There are built-in mechanisms which attempt to prevent the flooding of the backend server in case of cascading
 * exceptions or rapidly reoccurring exceptions that might continue to be thrown on a client inside an interval or loop.
 *
 * The exact behaviour is controller by {@link sendDelay}, {@link backoffThresholdCount} and
 * {@link backoffThresholdIntervalDuration}. One throttles the number of POST requests made to the server by combining
 * several uncaught exceptions into one request, while the other throttles the maximum number of exceptions logged in a
 * given interval. Once the interval expires, the logging continues. At this point this is a in-memory limit, meaning
 * that refreshing the page will reset the threshold counter. See {@link _enqueueException} for more details.
 *
 * @see https://github.com/getsentry/raven-js
 * @see http://log4javascript.org/
 */
Class.define('app.views.medications.common.RestErrorLogger', 'tm.jquery.Object', {
  statics: {
    SERVER_METHOD_NAME: '/webAppError',
    /* indicates if the logger was attached, since we can't make the constructor private and implement the singleton pattern */
    ATTACHED: false,
    /* keeps a copy of the original window handlers in case we get constructed twice */
    ORIGINAL_ON_ERROR_HANDLER: undefined,
    ORIGINAL_ON_UNLOAD_HANDLER: undefined
  },
  /** @type app.views.common.AppView */
  view: null,
  /** @type number */
  backoffThresholdIntervalDuration: 60 * 60 * 1000, /* 1h */
  /**
   * The maximum number of errors to report within the {@link backoffThresholdIntervalDuration}. Serves as a mechanism to
   * prevent flooding the server with error reports.
   * @type number
   */
  backoffThresholdCount: 25,
  /** @type number */
  sendDelay: 5000,
  /** @type Array<string|RegExp> */
  ignoreUrls: null,
  /** @type Array<string|RegExp> */
  ignoreErrors: null,
  /** @type Array<string|RegExp> */
  ignoreStackTraces: null,

  /** @type number|undefined */
  _sendTimer: undefined,
  /** @type Array<app.views.medications.common.RestErrorLogger.WebAppError> */
  _errorQueue: null,
  /** @type boolean */
  _sending: false,
  _lastException: null,
  /** @type number */
  _reportedErrorCount: 0,
  /** @type Date|null */
  _thresholdIntervalStart: null,
  /** @type RegExp|null */
  _ignoreUrlsRegExp: null,
  /** @type RegExp|null */
  _ignoreErrorsRegExp: null,
  /** @type RegExp|null */
  _ignoreStackTracesRegExp: null,
  /** @type app.views.medications.common.RestErrorLogger.AppExceptionUtils */
  _appExceptionUtils: null,

  Constructor: function(config)
  {
    this.callSuper(config);
    this._appExceptionUtils = new app.views.medications.common.RestErrorLogger.AppExceptionUtils({});
    this._errorQueue = [];

    this.ignoreUrls = tm.jquery.Utils.isArray(this.ignoreUrls) ? this.ignoreUrls : [];
    this.ignoreErrors = tm.jquery.Utils.isArray(this.ignoreErrors) ? this.ignoreErrors : [];
    this.ignoreStackTraces = tm.jquery.Utils.isArray(this.ignoreStackTraces) ? this.ignoreStackTraces : [];
    this._appendDefaultIgnoreFilters();

    this._ignoreUrlsRegExp = this.ignoreUrls.length > 0 ? this._joinRegExp(this.ignoreUrls) : null;
    this._ignoreErrorsRegExp = this.ignoreErrors.length > 0 ? this._joinRegExp(this.ignoreErrors) : null;
    this._ignoreStackTracesRegExp = this.ignoreStackTraces.length > 0  ? this._joinRegExp(this.ignoreStackTraces) : null;

    this._configureEventHandler();
  },

  /**
   * Configures recommended ignored urls and errors. The majority of rules were taken from Raven.js's source code and
   * https://docs.sentry.io/clients/javascript/tips/.
   * @private
   */
  _appendDefaultIgnoreFilters: function()
  {
    this.ignoreUrls.push(/graph\.facebook\.com/i);
    this.ignoreUrls.push(/connect\.facebook\.net\/en_US\/all\.js/i);
    this.ignoreUrls.push(/extensions\//i); // Chrome extensions
    this.ignoreUrls.push(/^chrome:\/\//i); // Chrome extensions
    this.ignoreUrls.push(/127\.0\.0\.1:4001\/isrunning/i);
    this.ignoreUrls.push(/webappstoolbarba\.texthelp\.com\//i);
    this.ignoreUrls.push(/metrics\.itunes\.apple\.com\.edgesuite\.net\//i);

    // "Script error." is hard coded into browsers for errors that it can't read.
    // this is the result of a script being pulled in from an external domain and CORS.
    this.ignoreErrors.push(/^Script error\.?$/);
    this.ignoreErrors.push(/^Javascript error: Script error\.? on line 0$/);
    // community recommendations
    this.ignoreErrors.push('top.GLOBALS');
    this.ignoreErrors.push('originalCreateNotification');
    this.ignoreErrors.push('canvas.contentDocument');
    this.ignoreErrors.push('MyApp_RemoveAllHighlights');
    this.ignoreErrors.push('http://tt.epicplay.com');
    this.ignoreErrors.push('Can\'t find variable: ZiteReader');
    this.ignoreErrors.push('jigsaw is not defined');
    this.ignoreErrors.push('ComboSearch is not defined');
    this.ignoreErrors.push('http://loading.retry.widdit.com/');
    this.ignoreErrors.push('atomicFindClose');
    this.ignoreErrors.push('fb_xd_fragment');
    this.ignoreErrors.push('bmi_SafeAddOnload');
    this.ignoreErrors.push('EBCallBackMessageReceived');
    this.ignoreErrors.push('conduitPage');

    // Ignoring errors thrown from inside FireBug Lite used in the jxBrowser environment. Suspected due to Chromium setting
    // the window.console to null when the window closes, and Firebug improperly handling it. The thrown errors are too
    // vague ("Cannot read property 'Console' of null", "Cannot read property 'persistedState' of null") to be considered
    // safe to match by the error message.
    this.ignoreStackTraces.push(/\/libs\/firebug-lite.js/);
  },

  /**
   * Bind to the window's onerror and onbeforeunload callback methods. If any already exist, the original function is
   * kept and called with the same arguments once we are done, so that we don't disrupt the inner workings of our
   * {@link app.views.common.AppView} composition.
   * Since it's possible that our view gets constructed multiple times (apparently caused by retry logic when
   * loading our JS files), we keep a reference to the original window handlers and reuse them, effectively throwing
   * our previous instance away.
   * @private
   */
  _configureEventHandler: function()
  {
    if (!app.views.medications.common.RestErrorLogger.ATTACHED)
    {
      app.views.medications.common.RestErrorLogger.ORIGINAL_ON_ERROR_HANDLER = window.onerror;
      app.views.medications.common.RestErrorLogger.ORIGINAL_ON_UNLOAD_HANDLER = window.onbeforeunload;
    }

    if (app.views.medications.common.RestErrorLogger.ATTACHED)
    {
      console.warn('An instance of app.views.medications.common.RestErrorLogger was already constructed. Attaching the ' +
          'new instance as the new error event handler.');
    }

    var self = this;
    window.onerror = function(msg, file, line, col, error)
    {
      self._enqueueException(self._mapErrorToAppException(error));

      if (!!app.views.medications.common.RestErrorLogger.ORIGINAL_ON_ERROR_HANDLER)
      {
        app.views.medications.common.RestErrorLogger.ORIGINAL_ON_ERROR_HANDLER.apply(this, arguments);
      }
    };
    window.onbeforeunload = function()
    {
      clearTimeout(self._sendTimer);
      if (self._errorQueue.length > 0)
      {
        self._processQueue();
      }
      if (!!app.views.medications.common.RestErrorLogger.ORIGINAL_ON_UNLOAD_HANDLER)
      {
        app.views.medications.common.RestErrorLogger.ORIGINAL_ON_UNLOAD_HANDLER.apply(this, arguments);
      }
    };

    app.views.medications.common.RestErrorLogger.ATTACHED = true;
  },

  /**
   * The exception queuing provides two basic mechanisms intended to prevent server flooding:
   * - the exception will be ignored if it matches the last exception intended to be queued,
   * - the exception will be ignored if the number of exceptions in the given threshold interval is greater than allowed
   *
   * The threshold interval starts when the first exception is added to the queue. Once the duration of the interval
   * passes, the error count is reset and a new interval will begin.
   *
   * @param {app.views.common.exception.AppException} exception
   * @private
   */
  _enqueueException: function(exception)
  {
    if (!(exception instanceof app.views.common.exception.AppException))
    {
      console.warn('Tried to queue something other than an AppException.', exception);
    }

    if (this._isRepeatedException(exception) || this._isIgnoredUrl(exception) ||
        this._isIgnoredException(exception) || this._isIgnoredStackTrace(exception))
    {
      return;
    }

    this._lastException = exception;

    if (!this._thresholdIntervalStart ||
        (new Date() - this._thresholdIntervalStart >= this.backoffThresholdIntervalDuration))
    {
      this._thresholdIntervalStart = new Date(); //
      this._reportedErrorCount = 0;
    }
    this._reportedErrorCount++;

    if (this._reportedErrorCount <= this.backoffThresholdCount)
    {
      this._errorQueue.push(
          app.views.medications.common.RestErrorLogger.WebAppError.fromAppException(this.view, exception));
      this._scheduleSend();
    }
  },

  /**
   * Attempts to map a given JS Error object to {@link app.views.common.exception.AppException}. Code shamelessly
   * taken from {@link app.views.common.AppNotifier#error}.
   * @param {Error|Object|undefined} error
   * @return {app.views.common.AppNotifier|undefined}
   * @private
   */
  _mapErrorToAppException: function(error){
    var exception = undefined;
    if (error instanceof Error)
    {
      // reason: try {} catch(ex) //
      exception = app.views.common.exception.AppException.create(
          null,
          null,
          null,
          null,
          error.message,
          error.stack);
    }
    else if (error && error.type === "error")
    {
      // reason: window.onError //
      var originalEvent = error.originalEvent;
      var originalError = originalEvent.error;
      exception = app.views.common.exception.AppException.create(
          -1,
          originalEvent.filename,
          originalEvent.lineno,
          originalEvent.colno,
          // Webkit fix //
          originalError ? originalError.message : originalEvent.message,
          originalError ? originalError.stack : originalEvent.message);
    }
    else
    {
      exception = app.views.common.exception.AppException.create(
          null,
          null,
          null,
          null,
          error,
          null);
    }
    return exception;
  },

  /**
   * Schedule the next exception logging as a timer, allowing additional exceptions to be queued during the short interval,
   * to throttle the number of Ajax requests.
   * @private
   */
  _scheduleSend: function()
  {
    var self = this;
    if (!this._sending && this._errorQueue.length > 0)
    {
      this._sending = true;
      // clearing the timeout should not be necessary, but it doesn't hurt either
      clearTimeout(this._sendTimer);
      this._sendTimer = setTimeout(
          function()
          {
            self._processQueue();
          },
          this.sendDelay);
    }
  },

  /**
   * Process the currently queued exceptions and report them to the server. In case of a failure, the exceptions are
   * returned to the queue.
   * @private
   */
  _processQueue: function()
  {
    try
    {
      var payload = this._errorQueue.splice(0);
      var self = this;
      var url = this.view.getViewModuleUrl() + app.views.medications.common.RestErrorLogger.SERVER_METHOD_NAME;
      var params = {
        errors: JSON.stringify(payload)
      };

      this.view.sendPostRequest(
          url,
          params,
          function onSuccess()
          {
            self._sending = false;
          },
          function onFailure()
          {
            self._handlePayloadProcessingError(payload);
            // The call to console.warn might throw an exception inside the jxBrowser environment, due to issues with
            // Firebug Lite, so leave the line at the end so it doesn't prevent other code from executing.
            console.warn('Failed to save the last exception to the server. Reattaching to the queue.');
          });
    }
    catch (error)
    {
      this._handlePayloadProcessingError(payload);
      // The call to console.warn might throw an exception inside the jxBrowser environment, due to issues with
      // Firebug Lite, so leave the line at the end so it doesn't prevent other code from executing.
      console.warn('Failed to report the last exception to the server. Reattaching to the queue.', error.message);
    }
  },

  /**
   *
   * @param {app.views.medications.common.dto.WebAppError[]} payload
   * @private
   */
  _handlePayloadProcessingError: function(payload)
  {
    if (payload)
    {
      this._reattachFailedPayloadToQueue(payload);
    }
    this._sending = false;
    this._scheduleSend(); // something went wrong, reschedule a send to be safe
  },

  /**
   *
   * @param {Array<app.views.medications.common.dto.WebAppError>} payload
   * @private
   */
  _reattachFailedPayloadToQueue: function(payload)
  {
    if (!payload || payload.length < 0)
    {
      return;
    }

    while (payload.length > 0)
    {
      this._errorQueue.unshift(payload.pop());
    }
  },

  /***
   * Compares the given exception with the {@link RestErrorLogger#_lastException}. Prevents the spam of the same exception
   * in case something breaks in a interval or loop.
   *
   * @param {app.views.common.exception.AppException} current
   * @return {boolean}
   * @private
   */
  _isRepeatedException: function(current)
  {
    var last = this._lastException;

    if (!last || current.getMessage() !== last.getMessage())
    {
      return false;
    }

    return this._appExceptionUtils.isSameException(current, last);
  },

  /**
   * @param {app.views.common.exception.AppException} exception
   * @return {boolean}
   * @private
   */
  _isIgnoredException: function(exception)
  {
    if (!this._ignoreErrorsRegExp)
    {
      return false;
    }
    return this._ignoreErrorsRegExp.test(exception.getMessage());
  },

  /**
   * @param {app.views.common.exception.AppException} exception
   * @return {boolean}
   * @private
   */
  _isIgnoredUrl: function(exception)
  {
    if (!exception.getUrl() || !this._ignoreErrorsRegExp)
    {
      return false;
    }

    return this._ignoreUrlsRegExp.test(exception.getUrl());
  },

  /**
   * @param {app.views.common.exception.AppException} exception
   * @return {boolean}
   * @private
   */
  _isIgnoredStackTrace: function(exception)
  {
    if (!exception.getStackTrace() || !this._ignoreStackTracesRegExp)
    {
      return false;
    }

    return this._ignoreStackTracesRegExp.test(exception.getStackTrace());
  },

  /**
   * Combines the array of regular expressions and strings into one large regexp.
   * @see https://github.com/getsentry/sentry-javascript/blob/master/packages/raven-js/src/utils.js
   * @param patterns
   * @return {RegExp}
   * @private
   */
  _joinRegExp: function(patterns)
  {
    var sources = [],
        i = 0,
        len = patterns.length,
        pattern;

    for (; i < len; i++)
    {
      pattern = patterns[i];
      if (tm.jquery.Utils.isString(pattern))
      {
        // If it's a string, we need to escape it
        // Taken from: https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide/Regular_Expressions
        sources.push(pattern.replace(/([.*+?^=!:${}()|\[\]\/\\])/g, '\\$1'));
      }
      else if (pattern && pattern.source)
      {
        // If it's a regexp already, we want to extract the source
        sources.push(pattern.source);
      }
      // Intentionally skip other cases
    }
    return new RegExp(sources.join('|'), 'i');
  }
});
/**
 * Simple compare utilities. Keeping the jsClass in the same file for easier import in other views.
 */
Class.define('app.views.medications.common.RestErrorLogger.AppExceptionUtils', 'tm.jquery.Object', {
  /**
   * Returns true if either a OR b is truthy, but not both
   * @param {*} a
   * @param {*} b
   * @return {boolean}
   */
  isOnlyOneTruthy: function(a, b)
  {
    return !!(!!a ^ !!b);
  },

  /**
   * Compares the exceptions the message, which is considered the quickest possible compare, otherwise the stack traces are
   * compared.
   * @param {app.views.common.exception.AppException} ex1
   * @param {app.views.common.exception.AppException} ex2
   * @return {boolean}
   */
  isSameException: function(ex1, ex2)
  {
    if (this.isOnlyOneTruthy(ex1, ex2)) return false;

    if (ex1.getMessage() !== ex2.getMessage()) return false;

    return this.isSameStacktrace(ex1.getStackTrace(), ex2.getStackTrace());
  },

  /**
   * Compares stack trace by text content, since there aren't any frames present after the TMC jQuery framework
   * is done with converting.
   * @param {String} stack1
   * @param {String} stack2
   * @return {boolean}
   */
  isSameStacktrace: function(stack1, stack2)
  {
    if (this.isOnlyOneTruthy(stack1, stack2)) return false;

    return stack1 === stack2;
  }
});
/**
 * Simple mapping of the java based WebAppErrorDto class, which is the payload for the error reporting method.
 */
Class.define('app.views.medications.common.RestErrorLogger.WebAppError', 'tm.jquery.Object', {
  /** @type string */
  loggerName: null,
  /** @type string */
  message: null,
  /** @type string */
  details: null,

  statics: {
    fromAppException: function(view, exception)
    {
      var userName = view.getUsername();
      var str = exception.getMessage();
      var details = [];

      details.push('client-time: ' + new Date().toISOString());
      if (userName)
      {
        details.push('user: ' + userName)
      }
      details.push('user-agent: ' + exception.getUserAgent());
      if (!tm.jquery.Utils.isEmpty(exception.getStatus()))
      {
        details.push('status: ' + this.getStatus());
      }
      if (!tm.jquery.Utils.isEmpty(exception.getFilename()))
      {
        details.push('file: ' + this.getFilename());
      }
      if (!tm.jquery.Utils.isEmpty(exception.getLine()))
      {
        details.push('line: ' + this.getLine());
      }
      if (!tm.jquery.Utils.isEmpty(exception.getColumn()))
      {
        details.push('column: ' + this.getColumn());
      }
      str += ' (' + details.join(', ') + ')';

      return new app.views.medications.common.RestErrorLogger.WebAppError({
        loggerName: view.getClassName(),
        message: str,
        details: exception.getStackTrace()
      });
    }
  },

  /* constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
  }
});