'use strict';

import React, { PropTypes } from 'react';
import ReactNative, { requireNativeComponent, View, StyleSheet } from 'react-native';

var {
    addons: { PureRenderMixin },
    NativeModules: { UIManager, CrosswalkWebViewManager: { JSNavigationScheme } }
} = ReactNative;

var resolveAssetSource = require('react-native/Libraries/Image/resolveAssetSource');


var WEBVIEW_REF = 'crosswalkWebView';

var CrosswalkWebView = React.createClass({
    mixins:    [PureRenderMixin],
    statics:   { JSNavigationScheme },
    propTypes: {
        injectedJavaScript:      PropTypes.string,
        localhost:               PropTypes.bool.isRequired,
        onBridgeMessage:         PropTypes.func,
        onError:                 PropTypes.func,
        onNavigationStateChange: PropTypes.func,
        source:                  PropTypes.oneOfType([
            PropTypes.shape({
                uri: PropTypes.string,  // URI to load in WebView
            }),
            PropTypes.shape({
                html: PropTypes.string, // static HTML to load in WebView
            }),
            PropTypes.number,           // used internally by React packager
        ]),
        url:                     PropTypes.string,
        ...View.propTypes
    },
    getDefaultProps () {
        return {
            localhost: false
        };
    },
    render () {
        var source = this.props.source || {};
        if (this.props.url) {
            source.uri = this.props.url;
        }
        return (
            <NativeCrosswalkWebView
                { ...this.props }
                onError={ this.onError }
                onBridgeMessage={ this.onBridgeMessage }
                onNavigationStateChange={ this.onNavigationStateChange }
                ref={ WEBVIEW_REF }
                source={ resolveAssetSource(source) }/>
        );
    },
    getWebViewHandle () {
        return ReactNative.findNodeHandle(this.refs[WEBVIEW_REF]);
    },
    onNavigationStateChange (event) {
        var { onNavigationStateChange } = this.props;
        if (onNavigationStateChange) {
            onNavigationStateChange(event.nativeEvent);
        }
    },
    onBridgeMessage (event) {
        var { onBridgeMessage } = this.props;
        if (onBridgeMessage) {
            onBridgeMessage(event.nativeEvent);
        }
    },
    onError (event) {
        var { onError } = this.props;
        if (onError) {
            onError(event.nativeEvent);
        }
    },
    goBack () {
        UIManager.dispatchViewManagerCommand(
            this.getWebViewHandle(),
            UIManager.CrosswalkWebView.Commands.goBack,
            null
        );
    },
    goForward () {
        UIManager.dispatchViewManagerCommand(
            this.getWebViewHandle(),
            UIManager.CrosswalkWebView.Commands.goForward,
            null
        );
    },
    reload () {
        UIManager.dispatchViewManagerCommand(
            this.getWebViewHandle(),
            UIManager.CrosswalkWebView.Commands.reload,
            null)
    },
    send (message) {
        UIManager.dispatchViewManagerCommand(
            this.getWebViewHandle(),
            UIManager.CrosswalkWebView.Commands.send,
            [message]
        );
    }
});

var NativeCrosswalkWebView = requireNativeComponent('CrosswalkWebView', CrosswalkWebView);

var styles = StyleSheet.create({
  container: {
    flex: 1,
  },
});

export default CrosswalkWebView;
