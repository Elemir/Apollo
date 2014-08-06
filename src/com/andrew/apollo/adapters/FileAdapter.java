/*
 * Copyright (C) 2012 Andrew Neal Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.andrew.apollo.adapters;

import android.content.Context;
import android.os.FileObserver;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.andrew.apollo.R;
import com.andrew.apollo.ui.MusicHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This {@link android.widget.ArrayAdapter} is used to display all of the files on a user's
 * device for {@link com.andrew.apollo.ui.fragments.FileFragment}.
 *
 * @author Evgeny Omelchenko (elemir90@gmail.com)
 */
public class FileAdapter extends BaseAdapter {

    /**
     * Number of views (TextView)
     */
    private static final int VIEW_TYPE_COUNT = 1;
    /**
     * The resource Id of the layout to inflate
     */
    private final int mLayoutId = R.layout.list_item_file;
    /**
     * Link to saved context
     */
    private final Context mContext;
    /**
     * Current directory and his parent
     */
    private File mDirectory, mParentDirectory;
    /**
     *
     */
    private int mHasParentDirectory;
    /**
     * Containers for directories and files
     */
    private List<File> mDirectories, mFiles;
    /**
     * The Observer instance for the current directory.
     */
    private Observer mFileObserver;
    /**
     * Refresh handler
     */
    private Handler mRefreshHandler;

    /**
     * Constructor of <code>FileAdapter</code>
     *
     * @param context The {@link android.content.Context} to use.
     * @param layoutId The resource Id of the view to inflate.
     */
    public FileAdapter(final Context context) {
        super();
        // Save context
        mContext = context;
        // Initialize lists
        mDirectories = new ArrayList<File>();
        mFiles = new ArrayList<File>();
        mRefreshHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                refresh();
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCount() {
        return mDirectories.size() + mFiles.size() + mHasParentDirectory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getItem(int i) {
        if (i == 0 && mHasParentDirectory == 1)
            return mParentDirectory;
        else if (i < mDirectories.size() + mHasParentDirectory)
            return mDirectories.get(i - mHasParentDirectory);
        else
            return mFiles.get(i - mDirectories.size() - mHasParentDirectory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getItemId(int i) {
        return i;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        MusicHolder holder;
        final boolean isDirectory = position < mDirectories.size() + mHasParentDirectory;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(mLayoutId, parent, false);
            holder = new MusicHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (MusicHolder)convertView.getTag();
        }

        if (isDirectory) {
            holder.mImage.get().setVisibility(View.VISIBLE);
            holder.mImage.get().setImageResource(R.drawable.ic_folder);
        } else
            holder.mImage.get().setVisibility(View.GONE);

        if (mHasParentDirectory == 1 && position == 0)
            holder.mLineOne.get().setText("..");
        else
            holder.mLineOne.get().setText(getItem(position).getName());

        return convertView;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasStableIds() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }


    /**
     * {@inheritDoc}
     */
    public final void changeDirectory(File directory) {
        mDirectory = directory;
        mParentDirectory = mDirectory.getParentFile();
        mHasParentDirectory = (mParentDirectory != null ? 1 : 0);
        mFileObserver = new Observer(mDirectory.getPath());
        refresh();
    }

    /**
     * {@inheritDoc}
     */
    public final void refresh() {
        File files[] = mDirectory.listFiles();
        mDirectories.clear();
        mFiles.clear();

        Arrays.sort(files);

        for (File file: files) {
            if (file.isDirectory())
                mDirectories.add(file);
            else mFiles.add(file);
        }
        notifyDataSetChanged();
    }

    /**
     * FileObserver that reloads the files in this adapter.
     */
    private class Observer extends FileObserver {
        public Observer(String path)
        {
            super(path, FileObserver.CREATE | FileObserver.DELETE | FileObserver.MOVED_TO | FileObserver.MOVED_FROM);
            startWatching();
        }

        @Override
        public void onEvent(int event, String path)
        {
            mRefreshHandler.sendEmptyMessage(0);
        }
    }
}
