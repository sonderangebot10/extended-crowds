package util;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.johan_dp8ahsz.cs.R;

import activity.MainActivity;

public class NavigationDrawerAdapter extends BaseAdapter {

    private ArrayList<NavigationItems> arrayList;
    private Context context;

    public NavigationDrawerAdapter(Context context, ArrayList<NavigationItems> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @Override
    public int getCount() {

        return arrayList.size();
    }

    @Override
    public NavigationItems getItem(int pos) {

        return arrayList.get(pos);
    }

    @Override
    public long getItemId(int pos) {

        return pos;
    }

    @Override
    public View getView(final int pos, View view, ViewGroup parent) {
        ViewHolder holder;

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.custom_view, parent, false);

            holder = new ViewHolder();
            holder.title = (TextView) view.findViewById(R.id.title);
            holder.subtitle = (TextView) view.findViewById(R.id.subtitle);

            holder.icon = (ImageView) view.findViewById(R.id.icon);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();

        }

        holder.title.setText(arrayList.get(pos).getTitle());
        holder.subtitle.setText(arrayList.get(pos).getSubTitle());

        holder.icon.setBackgroundResource(arrayList.get(pos).getIcon());

        view.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {

                //Calling main activity selectPosition method to replpace the fragment
                ((MainActivity)context).selectItem(pos);
            }
        });

        return view;
    }

    private class ViewHolder {
        TextView title, subtitle;
        ImageView icon;
    }

}