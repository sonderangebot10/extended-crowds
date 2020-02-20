package fragment;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.IdRes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.example.johan_dp8ahsz.cs.R;

import java.util.ArrayList;

import activity.MainActivity;
import util.SystemUtils;


public class CreateHITFragment extends android.app.Fragment {

    // UI references
    LinearLayout mRadioGroupLayout;

    View mProgressView;
    View mHITFormView;

    // Single choice is selected by default
    int lastChecked = 0;

    ArrayList<String> HITTypes;
    ArrayList<String> className;
    ArrayList<String> HITPackage;


    public CreateHITFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view  =inflater.inflate(R.layout.fragment_create_hit, container,false);

        // Change the toolbar title to something appropriate.
        MainActivity.setToolbarTitle(getString(R.string.hit_task));

        mRadioGroupLayout = (LinearLayout) view.findViewById(R.id.create_hit_radiogroup_layout);
        mProgressView = view.findViewById(R.id.hit_progress);
        mHITFormView = view.findViewById(R.id.hit_form);

        lastChecked = 0;
        HITTypes = new ArrayList<>();
        className = new ArrayList<>();
        HITPackage = new ArrayList<>();

        ArrayList<String> tmp = SystemUtils.getHITtypesFromAssets("create_hit", getActivity());
        for (String s : tmp){
            String[] tokens = s.split(":");

            className.add(tokens[0]);
            HITPackage.add(tokens[1]);
            HITTypes.add(tokens[2]);
        }

        // Create radio buttons depending on defined Human Intelligence Task types
        createRadioButtons();

        // Change fragment depending on what radio button is checked
        Button continueButton = (Button) view.findViewById(R.id.hit_continue);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                android.app.Fragment fragment = new CreateHITTestFragment();
                Bundle args = new Bundle();
                args.putString("name", className.get(lastChecked % className.size()));
                args.putString("package", HITPackage.get(lastChecked % HITPackage.size()));
                fragment.setArguments(args);

                final android.app.FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.frame_container, fragment).commit();

            }
        });

        return view;
    }

    private void createRadioButtons(){


        RadioGroup mOptionsGroup = new RadioGroup(getActivity());
        mOptionsGroup.setOrientation(RadioGroup.VERTICAL);

        // Create Radio Buttons for RadioGroup
        for(String s : HITTypes){
            RadioButton rb = new RadioButton(getActivity());
            rb.setText(s);
            rb.setTextSize(20);
            rb.setTextColor(Color.WHITE);
            mOptionsGroup.addView(rb);
        }

        mOptionsGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                RadioButton rb = (RadioButton) radioGroup.findViewById(i);
                int radioButtonId = rb.getId();
                lastChecked = radioButtonId - 1;
        //        SystemUtils.displayToast(getActivity(), String.valueOf(lastChecked));
            }
        });

        mRadioGroupLayout.addView(mOptionsGroup);
    }

}
