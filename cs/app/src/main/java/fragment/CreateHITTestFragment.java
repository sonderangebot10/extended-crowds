package fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.johan_dp8ahsz.cs.R;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CreateHITTestFragment extends android.app.Fragment{


    //UI references
    private View mProgressView;
    private View mCreateHitForm;
    private LinearLayout mMainLayout;
    View[] list;

    private String hitPackage, name;
    private Bundle mBundle;

    public CreateHITTestFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBundle = savedInstanceState;
        Log.e("HUEHUHEUEH", String.valueOf(mBundle == null));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_create_hit_test, container, false);

        // Fetch arguments passed to this fragment
        Bundle args = this.getArguments();
        if(args != null){
            name = args.getString("name");
            hitPackage = args.getString("package");
        }

        initializeStuffThatIsAllwaysGonnaGetInitialized(view);

        try {
            // Get the class from its name
            Class<?> myClass = Class.forName(hitPackage + "." + name );
            Object instance = myClass.newInstance();
            // Specify what method to call with Method name and its parameter types
            Method method = instance.getClass().getMethod("createUI", mMainLayout.getClass(),
                    list.getClass(), getActivity().getClass());
            // Invoke the specified method on the class object with the arguments
            method.invoke(instance, mMainLayout, list, getActivity());
        } catch ( java.lang.InstantiationException | IllegalAccessException | ClassNotFoundException
                | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
            Log.e("Teeststs", e.toString());
        }

        return view;
    }

    private void initializeStuffThatIsAllwaysGonnaGetInitialized(View view) {
        mProgressView = view.findViewById(R.id.create_hit_test_progress);
        mCreateHitForm = view.findViewById(R.id.create_hit_test_form);
        mMainLayout = (LinearLayout) view.findViewById(R.id.create_hit_test_main);

        list = new View[]{mProgressView, mCreateHitForm};
    }
}
