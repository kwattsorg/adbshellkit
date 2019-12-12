package net.kwatts.android.droidcommandpro;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kwatts on 5/14/18.
 */

public class CustomAdapterVars extends ArrayAdapter<String> {


    public List<String> spinnerVars = new ArrayList<>();
    Context mContext;

    public CustomAdapterVars(@NonNull Context context, List<String> vars) {
        super(context, R.layout.custom_spinner_row_vars);
        this.mContext = context;
        this.spinnerVars = vars;
    }

    @Override
    public int getCount() {
        return spinnerVars.size();
    }

    /*
    @Override
    public String getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
*/
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder mViewHolder = new ViewHolder();
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) mContext.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.custom_spinner_row_vars, parent, false);
            mViewHolder.mVar = (TextView) convertView.findViewById(R.id.tvVar);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        mViewHolder.mVar.setText(spinnerVars.get(position));

        return convertView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getView(position, convertView, parent);
    }

    private static class ViewHolder {
        TextView mVar;

    }
}
