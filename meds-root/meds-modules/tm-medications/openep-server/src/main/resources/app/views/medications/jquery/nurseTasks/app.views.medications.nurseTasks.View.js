Class.define('app.views.medications.nurseTasks.View', 'app.views.common.AppView', {
  cls: "v-nurse-task-list-view",

  activeContextData: null,

  /** rest methods */
  statics: {
    ACTION_SAVE_CONTEXT: "SAVE_CONTEXT",
    SERVLET_PATH_LOAD_CARE_PROVIDERS: '/getCurrentUserCareProviders',
    ACTION_OPEN_PATIENT: 'openPatient'
  },

  /** privates: components */
  activeCommandConditionTask: null,
  _careProviderFilterEnabled: false,
  _restErrorLogger: null,

  Constructor: function ()
  {
    this.callSuper();

    var viewInitData = this.getViewInitData();

    this._restErrorLogger = new app.views.medications.common.RestErrorLogger({view: this});
    this.activeContextData = viewInitData && viewInitData.contextData ?
        JSON.parse(viewInitData.contextData) : this._createInitContextData();
    this._careProviderFilterEnabled = viewInitData && viewInitData.careProviderFilterEnabled === true;

    this._buildGui();

    var documentResizeDebouncedTask = this.getAppFactory().createDebouncedTask(
        "onDocumentResizeDebouncedTask",
        function(){
          tm.jquery.ComponentUtils.hideAllTooltips();
          tm.jquery.ComponentUtils.hideAllDropDownMenus();
        },
        0,
        500
    );

    var self = this;
    this.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function ()
    {
      $('.careprovider-filter-btn').css('z-index', 1);
      self._nurseTaskListModule = angular
          .module("tm.angularjs.gui.views.nurseTask", ['tm.angularjs.gui.modules.nurseTask'])
          .config(['tm.angularjs.common.tmcBridge.ViewProxyProvider', function(viewProxyProvider)
          {
            viewProxyProvider.setAppView(self);
          }])
          .value("SaveFilterToContext", self._getSaveFilterContextClosure())
          .value("OpenPatientClickHandler", self._getOpenPatientClickHandler())
          .value("GetTranslation", self.getTranslationClosure())
          .value("version", self.getAppVersion())
          .value("ActiveInitData", {
            isTablet: tm.jquery.ClientUserAgent.isTablet(),
            language: self.getViewLanguage(),
            careProviderIds: tm.jquery.Utils.isArray(self.activeContextData.careProviderIds) ?
                self.activeContextData.careProviderIds : [],
            applicationTypes: tm.jquery.Utils.isArray(self.activeContextData.applicationTypes) ?
                self.activeContextData.applicationTypes: [],
            careProviderFilterEnabled: self.isCareProviderFilterEnabled()
          })
          .value("ActiveUpdateData", {
            patientIds: self.isCareProviderFilterEnabled() ? null : []
          })
          .run(['$rootScope', '$http', function ($rootScope, $http)
          {
            $http.defaults.headers.post["Content-Type"] = "application/x-www-form-urlencoded";
            document.addEventListener("click", function(e){
              $rootScope.$broadcast("documentClicked", e);
            });
            document.addEventListener("keyup", function(e){
              if (e.keyCode === 27) {
                $rootScope.$broadcast("documentClicked", e, true);
              }
            });
          }]);


      angular.bootstrap($(".angular-nurse-tasks-container"), ['tm.angularjs.gui.views.nurseTask']);

      self._setWindowHandler();
    });

    this.on(tm.jquery.ComponentEvent.EVENT_TYPE_WINDOW_RESIZE, function(){
      documentResizeDebouncedTask.run();
    });
    if(this.isDevelopmentMode() === true)
    {
      setTimeout(function () {
        self.onViewCommand({update: {
          patientIds: [ '399302674', '399302689' ]
        }});
      }, 500);
    }
  },

  /**
   * Override the broken default render to element in {@link #_appFactory}, which is pointing to an intermediate
   * {@link app.views.common.AppExternalCallView} instead of our view, causing our styling to fail.
   * {@see app.views.common.AppFactory#createDefaultView} for more information.
   *
   * @override
   */
  afterInitialize: function ()
  {
    this.callSuper();

    var self = this;
    this.getAppFactory().getDefaultRenderToElement = function()
    {
      return self.dom;
    }
  },

  /**
   * @override to prevent swing app from overriding our dictionary - it should match our version.
   */
  getDictionaryMap: function ()
  {
    return this.getViewResources()
        .dictionary
        .values
        .reduce(
            function toMap(map, obj)
            {
              map[obj.key] = obj.value;
              return map;
            },
            {});
  },

  /**
   * @Override
   * @param command
   */
  onViewCommand: function (command)
  {
    var self = this;
    var appFactory = this.getAppFactory();

    if(tm.jquery.Utils.isEmpty(this.activeCommandConditionTask) === false)
    {
      this.activeCommandConditionTask.abort();
    }

    this.activeCommandConditionTask = appFactory.createConditionTask(
        function ()
        {
          self._onViewCommandImpl(command);
        },
        function ()
        {
          return self.isRendered(true) &&
              (angular.element(document.getElementById('nurse-task-list-controller')).scope() !== undefined);
        },
        50, 100
    );
  },

  _onViewCommandImpl: function (command)
  {
    tm.jquery.ComponentUtils.hideAllDropDownMenus();
    tm.jquery.ComponentUtils.hideAllTooltips();
    tm.jquery.ComponentUtils.hideAllDialogs();

    this.getLocalLogger().info("Recieved update command: ", command);

    if (command.hasOwnProperty('update'))
    {
      this.updateData(command.update);
    }
    else if (command.hasOwnProperty('refresh'))
    {
      this.refreshData();
    }
    else if (command.hasOwnProperty("clear"))
    {
      this.clearData();
    }
  },

  _buildGui: function ()
  {
    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0));

    var angContainer = new tm.jquery.Container({
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "100%"),
      cls: 'angular-nurse-tasks-container',
      html: '<div data-ng-controller="NurseListGridCtrl" class="nurse-task-list-controller" id="nurse-task-list-controller"> ' +
              '<div class="headerSection">' +
                '<aplication-type data-click-callback="applyApplicationTypeFilter" data-selected="getContextFilters" class="aplication-type-selector"></aplication-type>' +
                '<tm-careprovider-selector ng-if="isCareProviderFilterEnabled()" ' +
                    'data-confirm-callback="confirm" data-cancel-callback="cancel" data-selected="getContextCareproviders" ' +
                    'data-data-provider="careproviderSelectorDataProvider" ' +
                    'style="float: right; direction: rtl; margin-top: 5px; background: white; margin-right: 10px;">' +
                '</tm-careprovider-selector>' +
              '</div>'+
              '<div> ' +
                '<div style="height:100%"> ' +
                  '<nurse-task-list></nurse-task-list>' +
                '</div> ' +
              '</div> ' +
            '</div>'
    });

    this.add(angContainer);
  },

  _setWindowHandler : function ()
  {

    var resizeTaskListScroller = function () {
      $('.nurse-task-list').height($('.v-nurse-task-list-view').height() - 40);
    };
    this._attachWindowHandler(resizeTaskListScroller);
    setTimeout(function () {
      resizeTaskListScroller();
    }, 500);
  },

  _attachWindowHandler : function (resizeTaskListScroller) {

    window.onresize = function ()
    {
      if (window.resizeEditorHandler)
      {
        clearTimeout(window.resizeEditorHandler);
      }

      window.resizeEditorHandler = setTimeout(function ()
      {
        resizeTaskListScroller();
      }, 500);
    }
  },

  /**
   * @returns {{careProviderIds: Array, applicationTypes: Array}}
   * @private
   */
  _createInitContextData: function()
  {
    return {
      careProviderIds: [],
      applicationTypes: []
    };
  },

  /**
   * @param {Object} command
   */
  updateData: function (command)
  {
    this.getLocalLogger().info("Updating view data.");
    angular.element(document.getElementById('nurse-task-list-controller')).scope().$emit('updateData', command.data);
  },

  refreshData: function ()
  {
    this.getLocalLogger().info("Refreshing view data.");
    angular.element(document.getElementById('nurse-task-list-controller')).scope().$emit('refreshData');
  },

  clearData: function ()
  {
    this.getLocalLogger().info("Clearing view data.");
    angular.element(document.getElementById('nurse-task-list-controller')).scope().$emit('clearData');

  },

  getTranslationClosure : function ()
  {
    var self = this;
    return function (key)
    {
      return self.getDictionary(key);
    };
  },

  _getSaveFilterContextClosure : function ()
  {
    var self = this;
    return function (careProviderIds, applicationTypes)
    {
      if (careProviderIds)
      {
        self.activeContextData.careProviderIds = careProviderIds;
      }
      if (applicationTypes)
      {
        self.activeContextData.applicationTypes = applicationTypes;
      }
      self.saveContextData();
    };
  },

  _getOpenPatientClickHandler : function () {
    var self = this;
    return function (patientId, subview)
    {
      self.sendAction(app.views.medications.nurseTasks.View.ACTION_OPEN_PATIENT, {
        patientId: patientId, subview: subview});
    };
  },

  saveContextData: function()
  {
    this.sendAction(app.views.medications.nurseTasks.View.ACTION_SAVE_CONTEXT,
    {
      contextData: JSON.stringify(this.activeContextData)
    });
    console.log("Saved context:", this.activeContextData);
  },

  /**
   * @returns {boolean}
   */
  isCareProviderFilterEnabled: function()
  {
    return this._careProviderFilterEnabled === true;
  }
});
