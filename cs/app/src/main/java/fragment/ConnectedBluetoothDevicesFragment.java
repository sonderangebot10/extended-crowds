package fragment;

import android.app.Fragment;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.johan_dp8ahsz.cs.R;

import java.util.Optional;
import java.util.Set;

import interfaces.BluetoothService;
import service.BluetoothServiceImpl;


public class ConnectedBluetoothDevicesFragment extends Fragment {

    private static final String TAG = "BLUETOOTH_DEVICES_FRAG";
    private static final String SMART_WATCH_MAC_ADDRESS = "00:B5:D0:5C:A3:7A";

    private BluetoothService bluetoothService;
    private Button openBluetoothServerSocket;
    private TextView deviceName;


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bluetooth_devices, container, false);
        openBluetoothServerSocket = (Button) view.findViewById(R.id.init_bluetooth_server_socket);
        deviceName = view.findViewById(R.id.bluetooth_device_name);

        bluetoothService = new BluetoothServiceImpl(getActivity());

        Set<BluetoothDevice> pairedDevices = bluetoothService.findPairedDevices();
        pairedDevices.stream()
                .filter(d -> d.getAddress().equals(SMART_WATCH_MAC_ADDRESS)).findFirst()
                .ifPresent(device -> deviceName.setText(device.getName()));

        openBluetoothServerSocket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothService.createBluetoothServerConnection();
            }
        });


        return view;
    }
}
