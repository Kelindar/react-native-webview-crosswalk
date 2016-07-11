package com.jordansexton.react.crosswalk.webview;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.SystemClock;
import android.webkit.ValueCallback;

import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.uimanager.UIManagerModule;
import com.facebook.react.uimanager.events.EventDispatcher;
import org.xwalk.core.XWalkNavigationHistory;
import org.xwalk.core.XWalkResourceClient;
import org.xwalk.core.XWalkView;

class CrosswalkWebView extends XWalkView implements LifecycleEventListener {

    private final Activity activity;
    private final EventDispatcher eventDispatcher;
    private final ResourceClient resourceClient;

    public CrosswalkWebView (ReactContext reactContext, Activity _activity) {
        super(reactContext, _activity);

        activity = _activity;
        eventDispatcher = reactContext.getNativeModule(UIManagerModule.class).getEventDispatcher();
        resourceClient = new ResourceClient(this);

        this.setResourceClient(resourceClient);
    }

    public Boolean getLocalhost () {
        return resourceClient.getLocalhost();
    }

    public void setLocalhost (Boolean localhost) {
        resourceClient.setLocalhost(localhost);
    }

    public void setInjectedJavaScript (String injectedJavascript) {
        resourceClient.setInjectedJavaScript(injectedJavascript);
    }

    @Override
    public void onHostResume() {
        resumeTimers();
        onShow();
    }

    @Override
    public void onHostPause() {
        pauseTimers();
        onHide();
    }

    @Override
    public void onHostDestroy() {
        onDestroy();
    }

    protected class ResourceClient extends XWalkResourceClient {
        private Boolean localhost = false;
        private String injectedJavascript = null;

        ResourceClient (XWalkView view) {
            super(view);
        }

        public Boolean getLocalhost () {
            return localhost;
        }

        public void setLocalhost (Boolean _localhost) {
            localhost = _localhost;
        }

        public void setInjectedJavaScript (String _injectedJavascript) {
            injectedJavascript = _injectedJavascript;
        }

        @Override
        public void onLoadFinished (XWalkView view, String url) {
            XWalkNavigationHistory navigationHistory = view.getNavigationHistory();
            eventDispatcher.dispatchEvent(
                new NavigationStateChangeEvent(
                    getId(),
                    SystemClock.uptimeMillis(),
                    view.getTitle(),
                    false,
                    url,
                    navigationHistory.canGoBack(),
                    navigationHistory.canGoForward()
                )
            );
            
            if (injectedJavascript != null) {
                view.load("javascript:(function() {\n" + injectedJavascript + ";\n})();", null);
            }
        }

        @Override
        public void onLoadStarted (XWalkView view, String url) {
            XWalkNavigationHistory navigationHistory = view.getNavigationHistory();
            String title = view.getTitle();
            
            // Check if it's a web view message
            if (title.startsWith("wvb://message")) {
                // If it's a bridge message, fetch the messages in flight and dispatch the event
                ValueCallback<String> callback = new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String jsonResult) {
                            //notifyCalled(jsonResult);
                            eventDispatcher.dispatchEvent(
                                new CrosswalkWebViewMessageEvent(
                                    jsonResult
                                )
                            );
                        }
                    };
                    
                view.evaluateJavascript("window.WebViewBridge.__fetch__();", callback);   
            } else {
                // it's an actual navigation change, dispatch the event
                eventDispatcher.dispatchEvent(
                    new NavigationStateChangeEvent(
                        getId(),
                        SystemClock.uptimeMillis(),
                        view.getTitle(),
                        true,
                        url,
                        navigationHistory.canGoBack(),
                        navigationHistory.canGoForward()
                    )
                );
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading (XWalkView view, String url) {
            Uri uri = Uri.parse(url);
            if (uri.getScheme().equals(CrosswalkWebViewManager.JSNavigationScheme)) {
                onLoadFinished(view, url);
                return true;
            }
            else if (getLocalhost()) {
                if (uri.getHost().equals("localhost")) {
                    return false;
                }
                else {
                    overrideUri(uri);
                    return true;
                }
            }
            else if (uri.getScheme().equals("http") || uri.getScheme().equals("https")) {
                return false;
            }
            else {
                overrideUri(uri);
                return true;
            }
        }

        private void overrideUri (Uri uri) {
            Intent action = new Intent(Intent.ACTION_VIEW, uri);
            activity.startActivity(action);
        }
    }
}
