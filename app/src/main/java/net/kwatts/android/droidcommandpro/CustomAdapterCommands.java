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

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Collection;

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
                    TextUtils.join(",",c.getTagList())
            );
        } else {
            mViewHolder.mCommandTags.setText("no_tags");
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

    // https://stackoverflow.com/questions/9906464/sort-listview-with-array-adapter

    @Override
    public void add(Command c) {
        //this.sort(ListThing.sComparator);
        //TODO: replace with last time used order
        super.add(c);
        this.spinnerCmds.add(c);
        //notifyDataSetChanged();
        /*
        Collections.sort(this.spinnerCmds, new java.util.Comparator<Command>() {
            public int compare(Command c1, Command c2) {
                //return c1==null?0L:c2==null?0L:c2.compareTo(c1);
                java.util.Date c1Date = new Date(c1.getLastused());
                java.util.Date c2Date = new Date(c2.getLastused());
                return c1Date.compareTo(c2Date);
            }
        }); */



    }

    public void addAllCommands(List<Command> cmds) {
        super.addAll(cmds);
        // For now, just clear
        spinnerCmds.clear();
        for(Command c: cmds){
            //super.add(item);
            spinnerCmds.add(c);
        }

        //if (!mCommands.contains(cmd)) {
        //    mCommands.add(cmd);
        //}
        notifyDataSetChanged();

    }

    @Override
    public void addAll(Command... items) {
        for(Command item: items){
            super.add(item);
            spinnerCmds.add(item);
        }
        //notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();

        this.setNotifyOnChange(false);

        //TODO: replace with last time used order
        Collections.sort(spinnerCmds, new java.util.Comparator<Command>() {
            public int compare(Command c1, Command c2) {
                //return c1==null?0L:c2==null?0L:c2.compareTo(c1);
                java.util.Date c1Date = new Date(c1.getLastused());
                java.util.Date c2Date = new Date(c2.getLastused());
                return c1Date.compareTo(c2Date);
            }
        });
        // reverse to display at top
        Collections.reverse(spinnerCmds);

        this.setNotifyOnChange(true);
    }



}
