package apotee.sky.poc1;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.topjohnwu.superuser.Shell;

import apotee.sky.poc1.data.Command;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by kwatts on 4/2/18.
 */

public class CustomAdapterCommands extends ArrayAdapter<Command> {


    public List<Command> spinnerCmds;
    Context mContext;

    public CustomAdapterCommands(@NonNull Context context, List<Command> cmds) {
        super(context, R.layout.custom_spinner_row);

        this.mContext = context;
        this.spinnerCmds = cmds;
    }

    @Override
    public int getCount() {
        return spinnerCmds.size();
    }

    @Override
    public Command getItem(int position) {
        return spinnerCmds.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder mViewHolder = new ViewHolder();
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) mContext.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.custom_spinner_row, parent, false);
            mViewHolder.mEmail = convertView.findViewById(R.id.tvUser);
            mViewHolder.mIsPublic = convertView.findViewById(R.id.tvIsPublic);
            mViewHolder.mCommandTags = convertView.findViewById(R.id.tvCommandTags);
            mViewHolder.mCommandDescription = convertView.findViewById(R.id.tvCommandDescription);
            mViewHolder.mCommand = convertView.findViewById(R.id.tvCommand);


            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }


        Command c = spinnerCmds.get(position);

        if (c.getEmail() != null) {
            mViewHolder.mEmail.setText("by " + c.getEmail());
        }

        if (c.isPublic) {
            mViewHolder.mIsPublic.setText("public");
        } else {
            mViewHolder.mIsPublic.setText("private");
        }


        if (c.getTagList() != null) {
            mViewHolder.mCommandTags.setText(
                    TextUtils.join(",", c.getTagList())
            );
        } else {
            mViewHolder.mCommandTags.setText("");
        }

        if (c.getDescription() != null) {
            mViewHolder.mCommandDescription.setText(c.getDescription());
        }


        if (c.getCommand() != null) {
            mViewHolder.mCommand.setText(c.getCommand());
        }

        return convertView;

    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getView(position, convertView, parent);
    }

    @Override
    public void add(Command c) {
        super.add(c);
        this.spinnerCmds.add(c);
    }

    public void addAllCommands(List<Command> cmds, boolean hideSuperUser, boolean hideVersionMismatch) {
        super.addAll(cmds);
        // For now, just clear
        spinnerCmds.clear();

        boolean haveRoot = Shell.rootAccess();
        for (Command c : cmds) {
            if (!c.versionCodeSupport(BuildConfig.VERSION_CODE) && hideVersionMismatch) {
                continue;
            }

            if (c.isSuperUser() && hideSuperUser) {
                if (!haveRoot) {
                    continue;
                    // spinnerCmds.add(c);
                }
            }

            spinnerCmds.add(c);



        }
        notifyDataSetChanged();

    }

    public void removeUserCommands() {
        //spinnerCmds.removeIf(c -> !c.isPublic);
        ListIterator<Command> iter = spinnerCmds.listIterator();
        while (iter.hasNext()) {
            if (!iter.next().isPublic) {
                iter.remove();
            }
        }

        notifyDataSetChanged();
    }

    @Override
    public void addAll(Command... items) {
        for (Command item : items) {
            super.add(item);
            spinnerCmds.add(item);
        }
        //notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();

        this.setNotifyOnChange(false);

        //TODO: right now getLastused is null for public so private rolls to top - it's ugly.
        // for public this means it's in the order we get off the server
        // use command.getSortRank() for public commands to set order from top (returns -1, 1, or 0)
        Collections.sort(spinnerCmds, (c1, c2) -> {
            Date c1Date = new Date(c1.getLastused());
            Date c2Date = new Date(c2.getLastused());
            return c1Date.compareTo(c2Date);
        });
        // reverse to display at top
        Collections.reverse(spinnerCmds);
        //TODO: only do this if the command is public
/*
        for (Command c : spinnerCmds) {
            if (c.isOnboardingUser()) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
                if (!sp.getBoolean(App.COMPLETED_ONBOARDING_PREF_NAME, false)) {
                    spinnerCmds.remove(c);
                    spinnerCmds.add(c);
                }
            }
        }
*/
        this.setNotifyOnChange(true);
    }

    private static class ViewHolder {
        TextView mEmail;
        TextView mIsPublic;
        TextView mCommandTags;
        TextView mCommandDescription;
        TextView mCommand;
    }


}
