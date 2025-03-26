/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 */

import React, {useEffect} from 'react';
import type {PropsWithChildren} from 'react';
import {
  ScrollView,
  StatusBar,
  StyleSheet,
  Text,
  useColorScheme,
  View,
  NativeModules,
  Button,
  DeviceEventEmitter,
} from 'react-native';

import {
  Colors,
  DebugInstructions,
  Header,
  LearnMoreLinks,
  ReloadInstructions,
} from 'react-native/Libraries/NewAppScreen';

type SectionProps = PropsWithChildren<{
  title: string;
}>;

interface SmartCardModuleType {
  listTerminals: () => Promise<string[]>;
  connectToCard: () => Promise<string>;
}

function Section({children, title}: SectionProps): React.JSX.Element {
  const isDarkMode = useColorScheme() === 'dark';

  // BluetoothCardReader?.connectToCardTerminal('Terminal Name')
  //   .then(() => {
  //     console.log('Connected to card terminal');
  //     // Send commands to the card
  //     return BluetoothCardReader?.sendCommand('COMMAND');
  //   })
  //   .then((response: any) => {
  //     console.log('Command response:', response);
  //   })
  //   .catch((error: any) => {
  //     console.error('Error:', error);
  //   });

  return (
    <View style={styles.sectionContainer}>
      <Text
        style={[
          styles.sectionTitle,
          {
            color: isDarkMode ? Colors.white : Colors.black,
          },
        ]}>
        {title}
      </Text>
      <Text
        style={[
          styles.sectionDescription,
          {
            color: isDarkMode ? Colors.light : Colors.dark,
          },
        ]}>
        {children}
      </Text>
    </View>
  );
}

function App(): React.JSX.Element {
  const SmartCardModule = NativeModules.SmartCardModule as SmartCardModuleType;
  const {BluetoothCardReader} = NativeModules;

  const isDarkMode = useColorScheme() === 'dark';

  useEffect(() => {
    const subscription = DeviceEventEmitter.addListener(
      'NativeLog',
      (logMessage: string) => {
        console.log('Native Log:', logMessage);
      },
    );

    // Clean up the subscription on unmount
    return () => {
      subscription.remove();
    };
  }, []);

  console.log('SmartCardModule:', SmartCardModule);
  SmartCardModule?.listTerminals()
    .then((terminals: string[]) => console.log('Terminals:', terminals))
    .catch((error: unknown) => console.error('Error:', error));

  SmartCardModule?.connectToCard()
    .then(message => console.log(message))
    .catch((error: unknown) => console.error('Error:', error));

  const backgroundStyle = {
    backgroundColor: isDarkMode ? Colors.darker : Colors.lighter,
  };

  const enableBluetooth = async () => {
    try {
      await BluetoothCardReader.enableBluetooth();
      console.log('Bluetooth enabled');
    } catch (error) {
      console.error('Error enabling Bluetooth:', error);
    }
  };

  const requestPermissions = async () => {
    try {
      await BluetoothCardReader.requestPermissions();
      console.log('Permissions granted');
    } catch (error) {
      console.error('Error requesting permissions:', error);
    }
  };

  const scanForCardTerminals = async () => {
    try {
      console.log('Scanning for card terminals...');
      const terminalName = await BluetoothCardReader.scanForCardTerminals();
      console.log('Connected to terminal:', terminalName);
    } catch (error) {
      console.error('Error scanning for card terminals:', error);
    }
  };

  const listCardTerminals = async () => {
    try {
      const terminals = await BluetoothCardReader.listTerminals();
      console.log('Terminals:', terminals);
    } catch (error) {
      console.error('Error listing card terminals:', error);
    }
  };

  const connectToCardTerminal = async (protocol: string) => {
    try {
      await BluetoothCardReader.connectToCardTerminal(protocol);
      console.log('Connected to card terminal');
    } catch (error) {
      console.error('Error connecting to card terminal:', error);
    }
  };

  const disconnectFromCardTerminal = async () => {
    try {
      await BluetoothCardReader.disconnectFromCardTerminal();
      console.log('Disconnected from card terminal');
    } catch (error) {
      console.error('Error disconnecting from card terminal:', error);
    }
  };

  const sendCommand = async (command: string) => {
    try {
      const response = await BluetoothCardReader.sendCommand(command);
      console.log('Command response:', response);
    } catch (error) {
      console.error('Error sending command:', error);
    }
  };

  const setTerminalTimeouts = async (
    connectionTimeout: number,
    powerTimeout: number,
    protocolTimeout: number,
    apduTimeout: number,
    controlTimeout: number,
  ) => {
    try {
      await BluetoothCardReader.setTerminalTimeouts(
        connectionTimeout,
        powerTimeout,
        protocolTimeout,
        apduTimeout,
        controlTimeout,
      );
      console.log('Terminal timeouts set');
    } catch (error) {
      console.error('Error setting terminal timeouts:', error);
    }
  };

  const getBatteryStatus = async () => {
    try {
      const batteryStatus = await BluetoothCardReader.getBatteryStatus();
      console.log('Battery status:', batteryStatus);
    } catch (error) {
      console.error('Error getting battery status:', error);
    }
  };

  const getBatteryLevel = async () => {
    try {
      const batteryLevel = await BluetoothCardReader.getBatteryLevel();
      console.log('Battery level:', batteryLevel);
    } catch (error) {
      console.error('Error getting battery level:', error);
    }
  };

  const getDeviceInfo = async (type: number) => {
    try {
      const deviceInfo = await BluetoothCardReader.getDeviceInfo(type);
      console.log('Device info:', deviceInfo);
    } catch (error) {
      console.error('Error getting device info:', error);
    }
  };

  // const scanForTerminals = () => {
  //   BluetoothCardReader.scanForCardTerminals()
  //     .then((terminalName: string) => {
  //       Alert.alert('Terminal Found', `Connected to terminal: ${terminalName}`);
  //     })
  //     .catch((error: any) => {
  //       Alert.alert(
  //         'Error',
  //         error.message || 'An error occurred while scanning for terminals',
  //       );
  //     });
  // };

  /*
   * To keep the template simple and small we're adding padding to prevent view
   * from rendering under the System UI.
   * For bigger apps the reccomendation is to use `react-native-safe-area-context`:
   * https://github.com/AppAndFlow/react-native-safe-area-context
   *
   * You can read more about it here:
   * https://github.com/react-native-community/discussions-and-proposals/discussions/827
   */
  const safePadding = '5%';

  return (
    <View style={backgroundStyle}>
      <StatusBar
        barStyle={isDarkMode ? 'light-content' : 'dark-content'}
        backgroundColor={backgroundStyle.backgroundColor}
      />
      <ScrollView style={backgroundStyle}>
        <View style={{paddingRight: safePadding}}>
          <Header />
        </View>
        <View
          style={{
            backgroundColor: isDarkMode ? Colors.black : Colors.white,
            paddingHorizontal: safePadding,
            paddingBottom: safePadding,
          }}>
          <Section title="Step One">
            <View
              style={{flex: 1, justifyContent: 'center', alignItems: 'center'}}>
              <Text>Welcome to the Bluetooth Card Reader App</Text>
              <View>
                <Button title="Enable Bluetooth" onPress={enableBluetooth} />
                <Button
                  title="Request Permissions"
                  onPress={requestPermissions}
                />
                <Button
                  title="Scan for Card Terminals"
                  onPress={scanForCardTerminals}
                />
                <Button
                  title="List Card Terminals"
                  onPress={listCardTerminals}
                />
                <Button
                  title="Connect to Card Terminal"
                  onPress={() => connectToCardTerminal('T=1')}
                />
                <Button
                  title="Disconnect from Card Terminal"
                  onPress={disconnectFromCardTerminal}
                />
                <Button
                  title="Send Command"
                  onPress={() => sendCommand('00A4040007A0000000031010')}
                />
                <Button
                  title="Set Terminal Timeouts"
                  onPress={() =>
                    setTerminalTimeouts(5000, 5000, 5000, 5000, 5000)
                  }
                />
                <Button title="Get Battery Status" onPress={getBatteryStatus} />
                <Button title="Get Battery Level" onPress={getBatteryLevel} />
                <Button
                  title="Get Device Info"
                  onPress={() => getDeviceInfo(1)}
                />
              </View>
            </View>
          </Section>
          <Section title="See Your Changes">
            <ReloadInstructions />
          </Section>
          <Section title="Debug">
            <DebugInstructions />
          </Section>
          <Section title="Learn More">
            Read the docs to discover what to do next:
          </Section>
          <LearnMoreLinks />
        </View>
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  sectionContainer: {
    marginTop: 32,
    paddingHorizontal: 24,
  },
  sectionTitle: {
    fontSize: 24,
    fontWeight: '600',
  },
  sectionDescription: {
    marginTop: 8,
    fontSize: 18,
    fontWeight: '400',
  },
  highlight: {
    fontWeight: '700',
  },
});

export default App;
