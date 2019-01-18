package util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.johan_dp8ahsz.cs.R;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class CustomListViewAdapter extends ArrayAdapter<Item> {

    private List<Item> data;
    private int resource;
    private int selectedPosition = 0;

    private ArrayList<RadioButton> buttons;

    public CustomListViewAdapter(Context context, List<Item> data) {
        super(context, R.layout.fragment_list_row, data);
        this.resource = R.layout.fragment_list_row;
        this.data = data;
        buttons = new ArrayList<>();
    }

    public CustomListViewAdapter(Context context, int resource, List<Item> data) {
        super(context, resource, data);
        this.resource = resource;
        this.data = data;
        buttons = new ArrayList<>();
    }

    public void setChecked(int position){
        selectedPosition = position;
        notifyDataSetChanged();
    }

    public int getChecked(){
        return selectedPosition;
    }

    public List<Item> getData() {
        return data;
    }

    @NonNull
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        View v = convertView;

        if (v == null) {
            LayoutInflater vi = LayoutInflater.from(getContext());
            v = vi.inflate(resource, null);
        }
        Item p = getItem(position);

        if(p != null){
            TextView questionText = (TextView) v.findViewById(R.id.row_question);
            TextView informationText = (TextView) v.findViewById(R.id.row_information);

            questionText.setText(p.getTitle());

            String type = p.getType();
            if (type != null) {
                informationText.setText(type);
            }
            if(resource == R.layout.fragment_list_row_checkable){
                RadioButton r = (RadioButton)v.findViewById(R.id.row_radioButton);
                r.setChecked(position == selectedPosition);
                r.setTag(position);
                buttons.add(r);
                r.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        selectedPosition = (Integer)view.getTag();
                        notifyDataSetChanged();
                    }
                });
            }

        }

        return v;
    }
}
