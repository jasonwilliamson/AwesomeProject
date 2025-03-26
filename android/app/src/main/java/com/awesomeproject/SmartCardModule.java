package com.AwesomeProject;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;

import javax.smartcardio.*;
import java.util.List;

public class SmartCardModule extends ReactContextBaseJavaModule {
    public SmartCardModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "SmartCardModule";
    }

    @ReactMethod
    public void listTerminals(Promise promise) {
        try {
            TerminalFactory factory = TerminalFactory.getDefault();
            List<CardTerminal> terminals = factory.terminals().list();
            promise.resolve(terminals.toString());
        } catch (CardException e) {
            promise.reject("CardException", e);
        }
    }

    @ReactMethod
    public void connectToCard(Promise promise) {
        try {
            TerminalFactory factory = TerminalFactory.getDefault();
            List<CardTerminal> terminals = factory.terminals().list();
            if (terminals.isEmpty()) {
                promise.reject("NoTerminals", "No card terminals found");
                return;
            }
            CardTerminal terminal = terminals.get(0);
            Card card = terminal.connect("T=0");
            promise.resolve("Connected to card: " + card.toString());
        } catch (Exception e) {
            promise.reject("ConnectionError", e);
        }
    }
}