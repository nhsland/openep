package com.marand.meds.rest.html;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.marand.ispek.common.Dictionary;
import com.marand.maf.core.JsonUtil;
import com.marand.maf.core.appinfo.GlobalAppMetaInfoProvider;
import com.marand.meds.app.config.security.OAuth2ClientConfigProvider;
import com.marand.meds.app.config.security.OAuth2ClientConfigProvider.OAuth2ClientConfig;
import com.marand.meds.app.prop.HtmlProperties;
import com.marand.thinkmed.html.common.HtmlDocumentBuilder;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Boris Marn.
 */
@RestController
@RequestMapping("/htmldocument")
public class HtmlDocumentController
{
  private static final String REST_CONTEXT_PATH = "/rest";
  private final HtmlProperties htmlProperties;

  @Autowired
  private Environment environment;

  @Autowired
  private OAuth2ClientConfigProvider oAuth2ClientConfigProvider;

  @Autowired
  public HtmlDocumentController(final HtmlProperties htmlProperties)
  {
    this.htmlProperties = htmlProperties;
  }

  @GetMapping(path = "view", produces = "text/html; charset=utf-8")
  public ResponseEntity<String> view(
      @RequestParam("config") final String config,
      @RequestParam(value = "dynamicData", required = false) final String dynamicData,
      @RequestParam(value = "username", required = false) final String username,
      @RequestParam(value = "debug", defaultValue = "false") final Boolean debug,
      // TODO: remove 'debug' parameter. It's deprecated.
      @RequestParam(value = "developmentMode", defaultValue = "false") final Boolean developmentMode)
  {
    return document(config, username, false, developmentMode || debug);
  }

  @GetMapping(path = "externalview")
  public ResponseEntity<String> externalView(@RequestParam("config") final String config)
  {
    final Config htmlConfig = JsonUtil.fromJson(config, Config.class);
    final String applicationType = htmlConfig.getApplicationType();
    final String language = htmlConfig.getLanguage();
    final String theme = htmlConfig.getTheme();
    final boolean isDebug = htmlConfig.isDebug();
    final String externalViewConfig =
        "{\"applicationType\":\"" + applicationType + "\",\"language\":\"" + language + "\",\"theme\":\"" + theme + "\",\"debug\":" + isDebug + ",\"header\":{\"url\":\"\",\"context\":\"rest\",\"controller\":\"medications\"},\"view\":\"externalcallview\",\"data\": " + config + "}";

    return document(externalViewConfig, null, true, false);
  }

  private ResponseEntity<String> document(
      final String config,
      final String usernameParam,
      final boolean isExternalCall,
      final boolean developmentMode)
  {
    AuthenticationType authenticationType = AuthenticationType.NONE;
    final String[] activeProfiles = environment.getActiveProfiles();
    if (Arrays.stream(activeProfiles).anyMatch(
        env -> env.equalsIgnoreCase("keycloak-auth") || env.equalsIgnoreCase("oauth2-auth")))
    {
      authenticationType = AuthenticationType.OAUTH2;
    }

    String appVersion = null;
    try
    {
      appVersion = GlobalAppMetaInfoProvider.getInstance().getAppMetaInfo().getAppVersion();
      final DateTime buildTimestamp = GlobalAppMetaInfoProvider.getInstance().getAppMetaInfo().getBuildTimestamp();
      if (buildTimestamp != null)
      {
        appVersion += "-" + buildTimestamp;
      }
    }
    catch (final Throwable th)
    {
      // do nothing //
    }

    final Config htmlConfig = JsonUtil.fromJson(config, Config.class);

    final Map<String, String> allStringsMap = Dictionary.toMap(new Locale(htmlConfig.getLanguage()));

    final HtmlDocumentBuilder hdb = new HtmlDocumentBuilder();
    final List<String> libFilePaths = new ArrayList<>();
    libFilePaths.add(
        REST_CONTEXT_PATH + (htmlProperties.isDevelopmentMode() || developmentMode
                             ? "/ui/htmlpresentation.js"
                             : "/ui/htmlpresentation.min.js") + "?version=" + appVersion);
    libFilePaths.add(
        REST_CONTEXT_PATH + (htmlProperties.isDevelopmentMode() || developmentMode
                             ? "/ui/htmlproxy.all.js"
                             : "/ui/htmlproxy.all.min.js") + "?version=" + appVersion);

    hdb.appendln("<!DOCTYPE html>");
    hdb.appendln("<html lang=\"en\">");
    hdb.appendln("<head>");
    hdb.appendln("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");
    hdb.appendln("<meta http-equiv=\"X-UA-Compatible\" content=\"IE=EDGE\"/>");

    for (final String libFilePath : libFilePaths)
    {
      hdb.appendln("<script type=\"text/javascript\" src=\"" + libFilePath + "\"></script>");
    }
    hdb.appendln("<script type=\"text/javascript\">");
    hdb.appendln("viewSettings = null;");

    hdb.appendln("function OnLoadHandler()");
    hdb.appendln("{");
    hdb.appendln("  DocumentReady();");
    hdb.appendln("}");
    hdb.appendln("");

    hdb.appendln("function DocumentReady()");
    hdb.appendln("{");

    hdb.appendln("  var localLogger = null;");

    // view action listener //
    hdb.appendln("  var viewActionListener = function(action)");
    hdb.appendln("  {");
    hdb.appendln("    var actionAsJSONString = action.toJsonString();");

    hdb.appendln("    if(action.name != ViewAction.ACTION_VIEW_LOG)");
    hdb.appendln("    {");
    hdb.appendln("      localLogger.debug(\"View action:\", actionAsJSONString);");
    hdb.appendln("    }");

    if (!developmentMode && !isExternalCall)
    {
      hdb.appendln("    AlertActionFunction(actionAsJSONString)");
    }

    hdb.appendln("  };");
    hdb.appendln("");

    // view config //
    hdb.appendln("  var viewConfig = " + config + ";");
    // view defaults //
    hdb.appendln("  var viewDefaults = {");
    hdb.appendln("    developmentMode: " + developmentMode + ",");
    hdb.appendln("    testMode: " + htmlProperties.isTestMode() + ",");
    if (isExternalCall)
    {
      hdb.appendln("    applicationType: '" + htmlConfig.getApplicationType() + "',"); // Html, Swing
    }
    else
    {
      hdb.appendln("    applicationType: '" + (developmentMode ? "Html" : "Swing") + "',"); // Html, Swing
    }
    // @Deprecated: remove this with Sencha
    hdb.appendln("    deviceType: '" + (developmentMode ? "Desktop" : "Swing") + "',"); // Desktop, Tablet, Phone
    // @Deprecated: remove this with Sencha
    hdb.appendln("    scrollbarType: 'Browser',"); // Browser, Touch
    hdb.appendln("    dictionary: function() {return " + null + ";}");
    //hdb.appendln("    dictionary: " + (debug ? JsonUtil.toJson(allStringsMap) : null));
    hdb.appendln("  };");
    hdb.appendln("");
    // view config //
    hdb.appendln("  var viewRenderElementId = null;");
    hdb.appendln("");
    hdb.appendln("  viewSettings = {");
    hdb.appendln("    viewConfig: viewConfig,");
    hdb.appendln("    viewDefaults: viewDefaults,");
    hdb.appendln("    viewRenderElementId: viewRenderElementId,");
    hdb.appendln("    viewActionListener: viewActionListener");
    hdb.appendln("  };");
    hdb.appendln("");

    // logger //
    hdb.appendln(
        "  var defaultRemoteLoggerImpl = ViewLoggerFactory.createDefaultRemoteLoggerImpl(viewActionListener, ViewLoggerFactory.REMOTE_MODE.NONE);");
    hdb.appendln("  var loggerLevel = " + developmentMode + " || viewConfig.debug === true ? ViewLogger.DEBUG : ViewLogger.INFO;");
    hdb.appendln("  ViewLoggerFactory.configure(loggerLevel, defaultRemoteLoggerImpl);");
    hdb.appendln("  localLogger = ViewLoggerFactory.getLocalLogger(null, 'htmlpresentation.ViewManager');");
    hdb.appendln("");

    // dom elements debugger //
    if (developmentMode || isExternalCall)
    {
      hdb.appendln("  setTimeout(function(){ViewManager.getInstance().applyDOMChangeDebugger(true);}, 2000);");
    }
    if (!developmentMode && !isExternalCall)
    {
      hdb.appendln("  var action = ViewAction.create('action.html.document.ready');");
      hdb.appendln("  alert(action.toJsonString());");
    }

    if (!developmentMode)
    {
      hdb.appendln("");
      hdb.appendln("  ViewGlobalSetting.setSetting(ViewGlobalSetting.APP_VERSION, '" + appVersion + "');");
    }

    if (developmentMode || isExternalCall)
    {
      hdb.appendln("");
      hdb.appendln("  document.title = viewConfig.header.controller + ' (' + viewConfig.view + ')';");
      hdb.appendln("  window.ViewConfigReady();");
    }

    hdb.appendln("}");
    hdb.appendln("");
    hdb.appendln("function ViewConfigReady()");
    hdb.appendln("{");

    // view //
    hdb.appendln("  function _createView() {");
    hdb.appendln("    ViewManager.getInstance().createView(viewSettings, function(view) {");
    if (developmentMode && authenticationType == AuthenticationType.OAUTH2)
    {
      hdb.appendln("      var originalOnAction = view.onAction;");
      hdb.appendln("      view.onAction = function(action, params) {");
      hdb.appendln("        if(action === ViewAction.ACTION_VIEW_AUTHENTICATION_EXCEPTION) {");
      hdb.appendln("          var viewSecurity = this.getViewSecurity();");
      hdb.appendln("          _oAuth2AuthenticationLogin(viewSecurity, function(authData) {");
      hdb.appendln("            viewSecurity.data = authData;");
      hdb.appendln("            view.authenticate(viewSecurity);");
      hdb.appendln("          });");
      hdb.appendln("        }");
      hdb.appendln("        originalOnAction.call(view, action, params);");
      hdb.appendln("      }");
    }
    hdb.appendln("    });");
    hdb.appendln("    window.viewSettings = null;");
    hdb.appendln("    delete window.viewSettings;");
    hdb.appendln("  }");

    hdb.appendln("  viewSettings.viewSecurity = viewSettings.viewSecurity ? viewSettings.viewSecurity : null;");

    if (developmentMode)
    {
      // oauth2 authentication integration //
      if (authenticationType == AuthenticationType.OAUTH2)
      {
        final OAuth2ClientConfig oAuth2ClientConfig = oAuth2ClientConfigProvider.get();
        if (oAuth2ClientConfig != null)
        {
          final String security = "{" +
              "type: '" + authenticationType.name().toLowerCase() + "'" + ", " +
              "clientId: '" + oAuth2ClientConfig.getClientId() + "'" + ", " +
              "url: '" + oAuth2ClientConfig.getTokenUri() + "'" + ", " +
              "data: null" +
              "}";
          hdb.appendln("  viewSettings.viewSecurity = " + security + ";");
        }
        else
        {
          hdb.appendln("  document.write('Not implemented!');");
        }
        hdb.appendln("");
        hdb.appendln("  function _oAuth2AuthenticationLogin(security, callback) {");
        hdb.appendln("    ViewOAuth2Authentication.login(security, '" + getDevelopmentUsername(
            usernameParam) + "', '" + getDevelopmentPassword() + "')");
        hdb.appendln("      .success(function(authData) {");
        hdb.appendln("        callback(authData);");
        hdb.appendln("      })");
        hdb.appendln("  }");
      }
      hdb.appendln("");
    }

    if (!isExternalCall && developmentMode && authenticationType == AuthenticationType.OAUTH2)
    {
      hdb.appendln("  _oAuth2AuthenticationLogin(viewSettings.viewSecurity, function(authData) {");
      hdb.appendln("    viewSettings.viewSecurity.data = authData;");
      hdb.appendln("    _createView();");
      hdb.appendln("  });");
    }
    else
    {
      hdb.appendln("  _createView();");
    }

    hdb.appendln("}");
    hdb.appendln("");

    hdb.appendln("</script>");

    hdb.appendln("</head>");

    hdb.appendln("<body onLoad='OnLoadHandler();' oncontextmenu='return false;'>");

    hdb.appendln("</body>");
    hdb.appendln("</html>");

    return ResponseEntity.ok()
        .contentType(MediaType.TEXT_HTML)
        .cacheControl(CacheControl.maxAge(-1, TimeUnit.SECONDS))
        .body(hdb.toString());
  }

  private String getDevelopmentUsername(final String usernameParam)
  {
    return usernameParam == null ? htmlProperties.getDevelopmentUser() : usernameParam;
  }

  private String getDevelopmentPassword()
  {
    return "test";
  }

  enum AuthenticationType
  {
    NONE,
    OAUTH2
  }

  public static final class Config
  {
    private String applicationType;
    private String language;
    private String theme;
    private boolean debug;

    public String getApplicationType()
    {
      return applicationType;
    }

    public String getLanguage()
    {
      return language;
    }

    public String getTheme()
    {
      return theme;
    }

    public boolean isDebug()
    {
      return debug;
    }
  }
}
