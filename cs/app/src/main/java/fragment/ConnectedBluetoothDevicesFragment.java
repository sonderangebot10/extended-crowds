package fragment;

import android.app.Fragment;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
        setNameOfPairedDevice();

        openBluetoothServerSocket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothService.createBluetoothServerConnection();
            }
        });

        final SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_name_container);
        swipeLayout.setOnRefreshListener(() -> {
            setNameOfPairedDevice();
            swipeLayout.setRefreshing(false);
        });


        return view;
    }

    private void setNameOfPairedDevice() {
        BluetoothDevice pairedDevice = getPairedDevice();
        if (pairedDevice != null) {
            deviceName.setText(pairedDevice.getName());
        } else {
            deviceName.setText("unknown");
        }
    }

    private BluetoothDevice getPairedDevice() {
        Set<BluetoothDevice> pairedDevices = bluetoothService.findPairedDevices();
        return pairedDevices.stream()
                .filter(d -> d.getAddress().equals(SMART_WATCH_MAC_ADDRESS)).findFirst().orElse(null);
    }
}
