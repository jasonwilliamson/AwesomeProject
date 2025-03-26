package com.AwesomeProject;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.acs.smartcardio.BluetoothSmartCard;
import com.acs.smartcardio.BluetoothTerminalManager;
import com.acs.smartcardio.TerminalTimeouts;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import java.util.List;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;
import com.awesomeproject.Hex;

public class BluetoothCardReaderModule extends ReactContextBaseJavaModule {
    private static final String MODULE_NAME = "BluetoothCardReader";
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PERMISSION_CODE = 2;
    private static final long SCAN_PERIOD = 5000;
    private Set<String> terminalNames = new HashSet<>();
    
    private ReactApplicationContext mReactContext;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothTerminalManager mManager;
    private TerminalFactory mFactory;
    private Handler mHandler;
    private CardTerminal mCardTerminal;
    private Card mCard;

    private void sendLogEvent(String logMessage) {
        mReactContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
            .emit("NativeLog", logMessage);
    }

    public BluetoothCardReaderModule(ReactApplicationContext reactContext) {
        super(reactContext);
        mReactContext = reactContext;
        mHandler = new Handler(Looper.getMainLooper());

        // Initialize Bluetooth adapter
        final BluetoothManager bluetoothManager =
                (BluetoothManager) reactContext.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            mBluetoothAdapter = bluetoothManager.getAdapter();
        }

        // Get the Bluetooth terminal manager
        mManager = BluetoothSmartCard.getInstance(reactContext).getManager();

        // Get the terminal factory
        mFactory = BluetoothSmartCard.getInstance(reactContext).getFactory();
    }

    @Override
    public String getName() {
        return MODULE_NAME;
    }

    @ReactMethod
    public void enableBluetooth(Promise promise) {
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            getCurrentActivity().startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            promise.resolve(true);
        } else {
            promise.resolve(true);
        }
    }

   @ReactMethod
    public void requestPermissions(Promise promise) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(mReactContext, Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(mReactContext, Manifest.permission.BLUETOOTH_SCAN)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getCurrentActivity(),
                        new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN},
                        REQUEST_PERMISSION_CODE);
            } else {
                promise.resolve(true);
            }
        } else {
            promise.resolve(true);
        }
    }

    // @ReactMethod
    // public void scanForCardTerminals(Promise promise) {
    //     mHandler.post(() -> {
    //         try {
    //             List<CardTerminal> terminals = mFactory.terminals().list();
    //             if (!terminals.isEmpty()) {
    //                 mCardTerminal = terminals.get(0);
    //                 promise.resolve(mCardTerminal.getName());
    //             } else {
    //                 promise.reject("NO_TERMINALS", "No card terminals found");
    //             }
    //         } catch (CardException e) {
    //             promise.reject("SCAN_ERROR", e.getMessage());
    //         }
    //     });
    // }

    @ReactMethod
    public void scanForCardTerminals(Promise promise) {
        mHandler.post(() -> {
            try {
                //terminalNames.clear(); // Clear previous scan results

                // Start the scan
                Log.d("BluetoothCardReaderModule", "Scanning for card terminals...");
                sendLogEvent("Scanning for card terminals...");
                mManager.startScan(4, terminal -> {
                    // Add the terminal to the list (if applicable in your context)
                    // runOnUiThread(() -> mTerminalAdapter.addTerminal(terminal)); 
                    sendLogEvent("Found terminal: " + terminal.getName());
                    Log.d("BluetoothCardReaderModule", "Found terminal: " + terminal.getName());
                    // Resolve the promise with the terminal name
                    promise.resolve(terminal.getName());
                });
                sendLogEvent("Scanning for card terminals complete");
                Log.d("BluetoothCardReaderModule", "Scanning for card terminals complete");
                // Stop the scan after a period
                mHandler.postDelayed(() -> {
                    sendLogEvent("Stopping scan...");
                    mManager.stopScan();
                    // Re-enable the scan button (if applicable in your context)
                    // mScanButton.setEnabled(true);
                }, SCAN_PERIOD);

            } catch (Exception e) {
                sendLogEvent("Error scanning for card terminals: " + e.getMessage());
                // Reject the promise if an error occurs
                promise.reject("SCAN_ERROR", e.getMessage());
            }
        });
    }

    @ReactMethod
    public void listTerminals(Promise promise) {
        try {
            sendLogEvent("Listing card terminals...");
            TerminalFactory factory = TerminalFactory.getDefault();
            sendLogEvent("Getting terminals...");
            List<CardTerminal> terminals = factory.terminals().list();
            sendLogEvent("Terminals: " + terminals.toString());
            promise.resolve(terminals.toString());
            sendLogEvent("Card terminals listed");
        } catch (CardException e) {
            sendLogEvent("Error listing card terminals: " + e.getMessage());
            promise.reject("CardException", e);
        }
    }

    @ReactMethod
    public void connectToCardTerminal(String protocol, Promise promise) {
        mHandler.post(() -> {
            if (mCardTerminal != null) {
                try {
                    mCard = mCardTerminal.connect(protocol);
                    promise.resolve(true);
                } catch (CardException e) {
                    promise.reject("CONNECT_ERROR", e.getMessage());
                }
            } else {
                promise.reject("NO_TERMINAL", "No card terminal selected");
            }
        });
    }

    @ReactMethod
    public void disconnectFromCardTerminal(Promise promise) {
        mHandler.post(() -> {
            if (mCard != null) {
                try {
                    mCard.disconnect(false);
                    mCard = null;
                    promise.resolve(true);
                } catch (CardException e) {
                    promise.reject("DISCONNECT_ERROR", e.getMessage());
                }
            } else {
                promise.resolve(true);
            }
        });
    }

    @ReactMethod
    public void sendCommand(String command, Promise promise) {
        mHandler.post(() -> {
            if (mCard != null) {
                try {
                    CardChannel channel = mCard.getBasicChannel();
                    CommandAPDU commandAPDU = new CommandAPDU(Hex.toByteArray(command));
                    ResponseAPDU responseAPDU = channel.transmit(commandAPDU);
                    promise.resolve(Hex.toHexString(responseAPDU.getBytes()));
                } catch (CardException e) {
                    promise.reject("COMMAND_ERROR", e.getMessage());
                }
            } else {
                promise.reject("NO_CARD", "No card connected");
            }
        });
    }

    @ReactMethod
    public void setTerminalTimeouts(int connectionTimeout, int powerTimeout, int protocolTimeout,
                                    int apduTimeout, int controlTimeout, Promise promise) {
        mHandler.post(() -> {
            if (mCardTerminal != null) {
                TerminalTimeouts timeouts = mManager.getTimeouts(mCardTerminal);
                timeouts.setConnectionTimeout(connectionTimeout);
                timeouts.setPowerTimeout(powerTimeout);
                timeouts.setProtocolTimeout(protocolTimeout);
                timeouts.setApduTimeout(apduTimeout);
                timeouts.setControlTimeout(controlTimeout);
                promise.resolve(true);
            } else {
                promise.reject("NO_TERMINAL", "No card terminal selected");
            }
        });
    }

    @ReactMethod
    public void getBatteryStatus(Promise promise) {
        mHandler.post(() -> {
            if (mCardTerminal != null) {
                try {
                    int batteryStatus = mManager.getBatteryStatus(mCardTerminal, 10000);
                    promise.resolve(batteryStatus);
                } catch (CardException e) {
                    promise.reject("BATTERY_STATUS_ERROR", e.getMessage());
                }
            } else {
                promise.reject("NO_TERMINAL", "No card terminal selected");
            }
        });
    }

    @ReactMethod
    public void getBatteryLevel(Promise promise) {
        mHandler.post(() -> {
            if (mCardTerminal != null) {
                try {
                    int batteryLevel = mManager.getBatteryLevel(mCardTerminal, 10000);
                    promise.resolve(batteryLevel);
                } catch (CardException e) {
                    promise.reject("BATTERY_LEVEL_ERROR", e.getMessage());
                }
            } else {
                promise.reject("NO_TERMINAL", "No card terminal selected");
            }
        });
    }

    @ReactMethod
    public void getDeviceInfo(int type, Promise promise) {
        mHandler.post(() -> {
            if (mCardTerminal != null) {
                try {
                    String deviceInfo = mManager.getDeviceInfo(mCardTerminal, type, 10000);
                    promise.resolve(deviceInfo);
                } catch (CardException e) {
                    promise.reject("DEVICE_INFO_ERROR", e.getMessage());
                }
            } else {
                promise.reject("NO_TERMINAL", "No card terminal selected");
            }
        });
    }

    // private Activity getCurrentActivity() {
    //     return mReactContext.getCurrentActivity();
    // }
}