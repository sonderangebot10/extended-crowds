package fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.johan_dp8ahsz.cs.R;


public class CreateTaskFragment extends android.app.Fragment {

    Button sensingButton;
    Button hitButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view  =inflater.inflate(R.layout.fragment_create_task, container,false);

        sensingButton   = (Button) view.findViewById(R.id.cstb);
        hitButton       = (Button) view.findViewById(R.id.chtb);

        // Set click listeners
        sensingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final android.app.FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.frame_container, new CreateSensingFragment(), "Sensing Task");
                ft.commit();
            }
        });

        hitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final android.app.FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.frame_container, new CreateHITFragment(), "Human Intelligence Task");
                ft.commit();
            }
        });

        return view;
    }

}
