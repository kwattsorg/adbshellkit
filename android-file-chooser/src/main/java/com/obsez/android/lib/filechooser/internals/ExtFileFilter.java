package com.obsez.android.lib.filechooser.internals;

import java.io.File;
import java.io.FileFilter;
import com.topjohnwu.superuser.io.*;

/**
 * Created by coco on 6/7/15.
 */
public class ExtFileFilter implements FileFilter {
    boolean m_allowHidden;
    boolean m_onlyDirectory;
    String[] m_ext;

    public ExtFileFilter() {
        this(false, false);
    }

    public ExtFileFilter(String... ext_list) {
        this(false, false, ext_list);
    }

    public ExtFileFilter(boolean dirOnly, boolean hidden, String... ext_list) {
        m_allowHidden = hidden;
        m_onlyDirectory = dirOnly;
        m_ext = ext_list;
    }

    @Override
    public boolean accept(File pathname) {
        SuFile suPathName = new SuFile(pathname);
        if (!m_allowHidden) {
            if (suPathName.isHidden()) {
                return false;
            }
        }

        if (m_onlyDirectory) {
            if (!suPathName.isDirectory()) {
                return false;
            }
        }

        if (m_ext == null) {
            return true;
        }

        if (suPathName.isDirectory()) {
            return true;
        }

        String ext = FileUtil.getExtensionWithoutDot(suPathName);
        for (String e : m_ext) {
            if (ext.equalsIgnoreCase(e)) {
                return true;
            }
        }
        return false;
    }

}
