package interfaces;


import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public interface CreateHITInterface {


    /**
     * A function that creates the UI for the Human Intelligence Task.
     * @param mainLayout The layout for which the layout will be in
     * @param list list[0] contains a progress spinner,
     *             and list[1] is the surrounding layout which mainLayout is in
     * @param context the context
     */
    void createUI(LinearLayout mainLayout, View[] list, activity.MainActivity context);

}
