package fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.johan_dp8ahsz.cs.R;

import java.io.ByteArrayOutputStream;

import util.SensorValueInterpreter;

public class TaskHistoryInformationFragment extends android.app.Fragment {


    //TODO: Change this class to be modular like Create Task and Assigned Task.
    //Haven't got time right now!


    private final String TAG = "T_HIST";

    // UI references

    String question = "", type = "";
    String[] data;
    String[][] formatedData;

    String sensor, duration;

    public TaskHistoryInformationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_task_history_information, container, false);
        LinearLayout mainLayout = (LinearLayout) view.findViewById(R.id.history_info_main);

        // Fetch arguments passed to this fragment
        Bundle args = this.getArguments();
        if(args != null){
            type = args.getString("type");
            question = args.getString("question");
            data = args.getString("data").split(",");
            sensor = args.getString("sensor");
            duration = args.getString("duration");
        }

        // mutual views
        TextView mTaskLabel = new TextView(getActivity());
        TextView mTaskView = new TextView(getActivity());
        TextView mCreatedLabel =  new TextView(getActivity());
        TextView mCreatedView = new TextView(getActivity());
        TextView mCompletedLabel =  new TextView(getActivity());
        TextView mCompletedView = new TextView(getActivity());

        mTaskLabel.setText(getString(R.string.task_history_task));
        setTextColorAndSize(mTaskLabel, 20);
        mTaskView.setText(question);
        setTextColorAndSize(mTaskView, 25);
        mainLayout.addView(mTaskLabel);
        mainLayout.addView(mTaskView);


        // Sensing task
        if(TextUtils.equals(type, "Sensor task")){


            String value = "";
            // data2 = 5;7;3;3
            if(TextUtils.equals(data[0], "no answer")){
                value = data[0];
            } else{
                switch (sensor){
                    case "light":
                        value = SensorValueInterpreter.makeSenseOfLightReadings(data[0]);
                        break;
                    case "pressure":
                        value = SensorValueInterpreter.makeSenseOfPressureReadings(data[0], duration);
                        break;
                    case "ambient_temperature":
                        value = SensorValueInterpreter.makeSenseOfTemperatureReadings(data[0]);
                        break;
                    default:
                        break;
                }
            }

            TextView resultLabel = new TextView(getActivity());
            resultLabel.setText("Result:");
            setTextColorAndSize(resultLabel, 20);

            TextView result = new TextView(getActivity());
            result.setText(value);
            setTextColorAndSize(result, 25);

            mCreatedLabel.setText(getString(R.string.task_history_created));
            mCreatedView.setText(data[1]);
            setTextColorAndSize(mCreatedLabel, 20);
            setTextColorAndSize(mCreatedView, 25);

            mCompletedLabel.setText(getString(R.string.task_history_completed));
            mCompletedView.setText(data[2]);
            setTextColorAndSize(mCompletedLabel, 20);
            setTextColorAndSize(mCompletedView, 25);

            mainLayout.addView(result);
            mainLayout.addView(mCreatedLabel);
            mainLayout.addView(mCreatedView);
            mainLayout.addView(mCompletedLabel);
            mainLayout.addView(mCompletedView);

        }
        // Human intelligence task
        else{

            // yes;no;maybe,10-07-2017

            TextView mAnswerLabel = new TextView(getActivity());
            TextView mAnswerView  = new TextView(getActivity());
            ImageView mImage = new ImageView(getActivity());

            mAnswerLabel.setText("Answer");

            Boolean image = false;
            LinearLayout layout = new LinearLayout(getActivity());
            if(data[0].startsWith("img="))
            {
                image = true;

                String encodedImage = data[0].replace("img=", "");
                byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(
                        bitmap, bitmap.getWidth()*10, bitmap.getHeight()*10, false);

                layout.setGravity(Gravity.CENTER);
                layout.addView(mImage);

                mImage.setImageBitmap(resizedBitmap);
            }

            mAnswerView.setText(data[0].replace(";", ", "));
            mCreatedLabel.setText(getString(R.string.task_history_created));
            mCreatedView.setText(data[1]);

            mCompletedLabel.setText(getString(R.string.task_history_completed));
            mCompletedView.setText(data[2]);
            if(TextUtils.equals(data[0].replace(";", ", "), "no answer")){
                mCompletedLabel.setText("Expired:");
            }

            setTextColorAndSize(mAnswerLabel, 20);
            setTextColorAndSize(mAnswerView, 25);
            setTextColorAndSize(mCreatedLabel, 20);
            setTextColorAndSize(mCreatedView, 25);
            setTextColorAndSize(mCompletedLabel, 20);
            setTextColorAndSize(mCompletedView, 25);

            mainLayout.addView(mAnswerLabel);
            if(image)
            {
                mainLayout.addView(layout);
            }
            else
            {
                mainLayout.addView(mAnswerView);
            }

            mainLayout.addView(mCreatedLabel);
            mainLayout.addView(mCreatedView);
            mainLayout.addView(mCompletedLabel);
            mainLayout.addView(mCompletedView);

        }


        return view;
    }

    private void setTextColorAndSize(TextView text, int size){
        text.setTextSize(size);
        text.setTextColor(Color.WHITE);
    }
}
