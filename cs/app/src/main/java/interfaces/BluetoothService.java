package interfaces;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import java.util.Set;

public interface BluetoothService {

    boolean isBluetoothSupported();

    void enableBluetooth();

    Set<BluetoothDevice> findPairedDevices();

    void discoverDevice();

    void stopDiscovery();

    void createBluetoothServerConnection();

    void closeBluetoothServerConnection();
}
