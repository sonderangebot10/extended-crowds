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

/**
 * Created by johan on 2017-10-20.
 */

public class AssignedTaskTestFragment extends android.app.Fragment{


    private LinearLayout mMainLayout;
    private View[] list;

    private String hitPackage, name, question, id, position;
    private String[] options;
    android.app.FragmentManager fm;

    public AssignedTaskTestFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_assigned_task_test, container, false);



        // Fetch arguments passed to this fragment
        Bundle args = this.getArguments();
        if(args != null){
            name = args.getString("name");
            hitPackage = args.getString("package");

            id = args.getString("id");
            question = args.getString("question");
            options = args.getStringArray("options");
            position = args.getString("position");
        }

        initializeStuffThatIsAllwaysGonnaGetInitialized(view);


        try {
            // Get the class from its name
            Class<?> mClass = Class.forName(hitPackage + "." + name );
            Object instance = mClass.newInstance();

            Object[] args1 = {new Object[]{fm}, concat(new String[]{id, question, position}, options)};
            callRemote(instance, "setUIRelatedData", args1);

            Object[] args2 = {mMainLayout, list, getActivity()};
            callRemote(instance, "createUI", args2);

        } catch ( java.lang.InstantiationException | IllegalAccessException | ClassNotFoundException
                | NoSuchMethodException e) {
            e.printStackTrace();
            Log.e("Teeststs", e.toString());
        } catch (InvocationTargetException e){
            Log.e("Teeststs", e.getCause().toString());

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Teeststs", e.toString());
        }

        return view;
    }

    private void initializeStuffThatIsAllwaysGonnaGetInitialized(View view) {
        View mProgressView = view.findViewById(R.id.assigned_task_progress);
        View mCreateHitForm = view.findViewById(R.id.assigned_task_form);
        mMainLayout = (LinearLayout) view.findViewById(R.id.assigned_task_main);
        fm = getFragmentManager();
        list = new View[]{mProgressView, mCreateHitForm};
    }


    private Object callRemote(Object instance, String sMethod, Object... arguments) throws Exception {
        Class<?>[] argumentTypes = createArgumentTypes(arguments);
        Method method = instance.getClass().getMethod(sMethod, argumentTypes );
        Object[] argumentsWithSession = createArguments(arguments);
        return method.invoke(instance, argumentsWithSession);
    }

    private Object[] createArguments(Object[] arguments) {
        Object[] args = new Object[arguments.length];
        System.arraycopy(arguments, 0, args, 0, arguments.length);
        return args;
    }

    private Class<?>[] createArgumentTypes(Object[] arguments) {
        Class[] types = new Class[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            types[i] = arguments[i].getClass();
        }
        return types;
    }

    /**
     * Helper function that concatenates two arrays
     * @param a first array
     * @param b second array
     * @return returns the an array where a & b are concatenated
     */
    private String[] concat(String[] a, String[] b) {
        int aLen = a.length;
        int bLen = b.length;
        String[] c = new String[aLen+bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

}
