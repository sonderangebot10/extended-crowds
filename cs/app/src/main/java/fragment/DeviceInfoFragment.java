package fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.example.johan_dp8ahsz.cs.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import util.CustomListViewAdapter;
import util.Item;
import util.SystemUtils;


public class DeviceInfoFragment extends android.app.Fragment {

    TextView mSensorsTot;
    ListView mSensorList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view  = inflater.inflate(R.layout.fragment_device_info, container,false);

        mSensorsTot = (TextView) view.findViewById(R.id.sensortot);
        mSensorList = (ListView) view.findViewById(R.id.sensor_list);

        ArrayList<String> mySensors = SystemUtils.getDeviceSensors(getActivity());
        Collections.sort(mySensors);
        String[] sensorArray = mySensors.toArray(new String[0]);
        List<Item> sensors = new LinkedList<>();

        for(int i = 0; i < sensorArray.length; i++){
            sensors.add(new Item(sensorArray[i]));
        }

        //ArrayAdapter adapter = new ArrayAdapter(getActivity() , android.R.layout.simple_list_item_1, sensors);
        mSensorList.setAdapter(new CustomListViewAdapter(getActivity(), sensors));

        return view;
    }

}
