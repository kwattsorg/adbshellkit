package net.kwatts.android.droidcommandpro;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import net.kwatts.android.droidcommandpro.model.Command;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kwatts on 4/2/18.
 */

public class CustomAdapterCommands extends ArrayAdapter<Command> {


    private static class ViewHolder {
        TextView mEmail;
        TextView mIsPublic;
        TextView mCommandTags;
        TextView mCommandDescription;
        TextView mCommand;
    }


    public List<String> spinnerKeys = new ArrayList<>();
    public List<String> spinnerCommandTags = new ArrayList<>();
    public List<String> spinnerCommandDescriptions = new ArrayList<>();
    public List<String> spinnerEmails = new ArrayList<>();
    public List<String> spinnerIsPublics = new ArrayList<>();
    public List<String> spinnerCommands = new ArrayList<>();

    public List<Command> spinnerCmds = new ArrayList<>();
    Context mContext;

    public CustomAdapterCommands(@NonNull Context context, List<Command> cmds) {
        super(context, R.layout.custom_spinner_row);
        this.mContext = context;
        this.spinnerCmds = cmds;

        for(Command cmd : cmds) {
         if (cmd.key != null) {
                this.spinnerKeys.add(cmd.key);
            } else {
                this.spinnerKeys.add("");
            }

            if (cmd.getEmail() == null) {
                this.spinnerEmails.add("anonymous");
            } else {
                this.spinnerEmails.add(cmd.getEmail());
            }



            if (cmd.isPublic) {
                this.spinnerIsPublics.add("public");
            } else {
                this.spinnerIsPublics.add("private");
            }

            if (cmd.getTagList() == null) {
                this.spinnerCommandTags.add("no_tags");
            } else { this.spinnerCommandTags.add(TextUtils.join(",",cmd.getTagList())); }

            if (cmd.getDescription() == null) {
                this.spinnerCommandDescriptions.add("No Description");
            } else {
                this.spinnerCommandDescriptions.add(cmd.getDescription());
            }

            if (cmd.getCommand() == null) {
                this.spinnerCommands.add("");
            } else {
                this.spinnerCommands.add(cmd.getCommand());
            }
        }
    }



    @Override
    public int getCount() {
        return spinnerCmds.size();
    }

    public int getPositionByCommand(String c) {
        for (int x=0; x < spinnerCommands.size(); x++) {
            if (c.equals(spinnerCommands.get(x))) return x;
        }
        return 0;
    }

    public int getPositionByKey(String key) {
        for (int x=0; x < spinnerKeys.size(); x++) {
            if (key.equals(spinnerKeys.get(x))) return x;
        }
        return 0;
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
            mViewHolder.mEmail = (TextView) convertView.findViewById(R.id.tvUser);
            mViewHolder.mIsPublic = (TextView) convertView.findViewById(R.id.tvIsPublic);
            mViewHolder.mCommandTags = (TextView) convertView.findViewById(R.id.tvCommandTags);
            mViewHolder.mCommandDescription = (TextView) convertView.findViewById(R.id.tvCommandDescription);
            mViewHolder.mCommand = (TextView) convertView.findViewById(R.id.tvCommand);


            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        mViewHolder.mEmail.setText("by " + spinnerEmails.get(position));
        mViewHolder.mIsPublic.setText(spinnerIsPublics.get(position));
        mViewHolder.mCommandTags.setText(spinnerCommandTags.get(position));
        mViewHolder.mCommandDescription.setText(spinnerCommandDescriptions.get(position));
        mViewHolder.mCommand.setText(spinnerCommands.get(position));

        return convertView;

        }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getView(position, convertView, parent);
    }
}
