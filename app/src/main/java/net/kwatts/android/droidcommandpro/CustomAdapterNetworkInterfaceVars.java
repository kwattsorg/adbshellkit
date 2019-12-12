package net.kwatts.android.droidcommandpro;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.net.NetworkInterface;
import java.util.List;

/**
 * Created by kwatts on 5/14/18.
 */

public class CustomAdapterNetworkInterfaceVars extends ArrayAdapter<String> {


    public List<NetworkInterface> spinnerVars;
    Context mContext;

    public CustomAdapterNetworkInterfaceVars(@NonNull Context context, List<NetworkInterface> vars) {
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
            mViewHolder.mVar = convertView.findViewById(R.id.tvVar);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        mViewHolder.mVar.setText(spinnerVars.get(position).getDisplayName());

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
