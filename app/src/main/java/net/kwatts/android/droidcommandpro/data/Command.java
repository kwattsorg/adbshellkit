package net.kwatts.android.droidcommandpro.data;

import android.os.Build;

import com.google.gson.annotations.SerializedName;

import net.kwatts.android.droidcommandpro.BuildConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by kwatts on 11/9/17.
 */

//public class Command implements net.kwatts.android.droidcommandpro.commands.Command {
public class Command {
    /* NOTE! The names and character cases matter & won't be processed by firebase if it doesn't match!
             This includes the getters and setters! */
    public String key;
    @SerializedName("public")
    public boolean isPublic;
    @SerializedName("command")
    public String mCommand;
    @SerializedName("localbinary")
    public String mLocalBinary;
    @SerializedName("remotebinary")
    public String mRemoteBinary;
    @SerializedName("appversion")
    public Long mAppversion;
    @SerializedName("runcounts")
    public Long mRuncounts;
    @SerializedName("lastused")
    public Long mLastused;
    @SerializedName("uid")
    private String mUid;
    @SerializedName("email")
    private String mEmail;
    @SerializedName("taglist")
    private List<String> mTagList;
    @SerializedName("permissionlist")
    private List<String> mPermissionlist;
    @SerializedName("description")
    private String mDescription;

    //@SerializedName("sortRank")
    //public Long mSortRank;

    public Command() {
    }

    public String getUid() {
        return mUid;
    }

    public void setUid(String uid) {
        mUid = uid;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        mEmail = email;
    }

    public List<String> getTagList() {
        return mTagList;
    }

    public void setTagList(List<String> tagList) {
        mTagList = tagList;
    }

    public List<String> getPermissionList() {
        if (mPermissionlist == null) {
            mPermissionlist = new ArrayList<>();
        }
        return mPermissionlist;
    }

    public void setPermissionList(List<String> permissionList) {
        mPermissionlist = permissionList;
    }

    public void addPermission(String permission) {
        if (mPermissionlist != null) {
            mPermissionlist.add(permission);
        } else {
            mPermissionlist = Collections.singletonList(permission);
        }

    }

    public void removePermission(String permission) {
        if (mPermissionlist != null) {
            mPermissionlist.remove(permission);
        }
    }

    public String getDescription() {
        if (mDescription == null) {
            mDescription = "";
        }
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public String getCommand() {
        if (mCommand == null) {
            mCommand = "";
        }
        return mCommand;
    }

    public void setCommand(String command) {
        mCommand = command;
    }

    public Long getRuncounts() {
        return mRuncounts;
    }

    public void setRuncounts(Long runcounts) {
        mRuncounts = runcounts;
    }

    public Long getLastused() {
        if (mLastused == null) {
            return 0L;
        } else {
            return mLastused;
        }
    }

    public void setLastused(Long lastused) {
        if (!isPublic) {
            mLastused = lastused;
        }
    }

    /*
    public Long getSortRank() {
        if (mSortRank == null) {
            return 0L;
        } else {
            return mSortRank;
        }
    }

    public void setSortRank(Long sortRank) {
          mSortRank = sortRank;
    } */

    public int getAppversion() {
        if (mAppversion == null) {
            return 0;
        } else {
            return mAppversion.intValue();
        }
    }

    public void setAppversion(int appversion) {
        mAppversion = new Long(appversion);
    }


    public String getLocalBinary() {
        if (mLocalBinary == null) {
            mLocalBinary = "";
        }
        return mLocalBinary;
    }

    public void setLocalBinary(String localBinary) {
        mLocalBinary = localBinary;
    }

    public String getRemoteBinary() {
        if (mRemoteBinary == null) {
            mRemoteBinary = "";
        }
        return mRemoteBinary;
    }

    public void setRemoteBinary(String remoteBinary) {
        mRemoteBinary = remoteBinary;
    }

/*
    public String getCommandName() {
        return key;
    }

    public String[] getPermissions() {
        return mPermissionlist.toArray(new String[0]);
    }

    public JSONObject execute(android.content.Context ctx, List<String> args) {
        JSONObject res = new JSONObject();
        res.put("key", key);
        return res;
    }
*/

    public void addToRuncounts() {
        if (!isPublic) {
            if (mRuncounts == null) {
                mRuncounts = 1L;

            } else {
                mRuncounts += 1L;
            }
        }
    }

    public boolean versionCodeSupport(int currentAppVersionCode) {
        int minVersionCodeSupported = getAppversion();

        if (minVersionCodeSupported == 0) { return true; } //XXX for cmds w/out appversion

        if (minVersionCodeSupported > currentAppVersionCode) {
            return false;
        } else {
            return true;
        }
    }


    public boolean isPinned() {
        if (mTagList != null) {
            return mTagList.contains("pinned");
        } else {
            return false;
        }
    }

    public boolean isSuperUser() {
        if (mTagList != null) {
            return mTagList.contains("superuser");
        } else {
            return false;
        }
    }

    public boolean isOnboarding() {
        if (mTagList != null) {
            return mTagList.contains("onboarding");
        } else {
            return false;
        }
    }


    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", mUid);
        result.put("email", mEmail);
        result.put("isPublic", isPublic);
        result.put("description", mDescription);
        result.put("tagList", mTagList);
        result.put("permissionList", mPermissionlist);
        result.put("command", mCommand);
        result.put("appversion", mAppversion);
        result.put("runcounts", mRuncounts);
        result.put("lastused", mLastused);
        return result;
    }

    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof Command)) return false;
        Command o = (Command) obj;
        return o.key.equals(this.key);
    }

    public String toString() {
        return "key: " + this.key +
                "\nuid: " + this.mUid +
                "\nuser: " + this.mEmail +
                "\nisPublic: " + this.isPublic +
                "\nisPinned: " + isPinned() +
                "\nappversion: " + this.mAppversion +
                "\nruncounts: " + this.mRuncounts +
                "\nlastused: " + this.mLastused +
                "\ndescription: " + this.mDescription +
                "\ncommand: " + this.mCommand;

    }

}
