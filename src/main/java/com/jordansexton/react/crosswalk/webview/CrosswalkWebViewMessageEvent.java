package com.jordansexton.react.crosswalk.webview;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.Event;
import com.facebook.react.uimanager.events.RCTEventEmitter;

class CrosswalkWebViewMessageEvent extends Event<CrosswalkWebViewMessageEvent> {

    public static final String EVENT_NAME = "bridgeMessage";
    private final String data;

    public CrosswalkWebViewMessageEvent (int viewTag, long timestampMs, String _data) {
        super(viewTag);
        data = _data;
    }

    @Override
    public String getEventName () {
        return EVENT_NAME;
    }

    @Override
    public void dispatch (RCTEventEmitter rctEventEmitter) {
        rctEventEmitter.receiveEvent(getViewTag(), getEventName(), serializeEventData());
    }

    private WritableMap serializeEventData () {
        WritableMap eventData = Arguments.createMap();
        eventData.putInt("target", getViewTag());
        eventData.putString("data", data);
        return eventData;
    }
}
