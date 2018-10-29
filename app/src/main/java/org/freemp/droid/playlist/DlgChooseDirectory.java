package org.freemp.droid.playlist;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.freemp.droid.FileUtils;
import org.freemp.droid.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by recoilme on 05/12/13.
 */
/*
 * Copyright (C) 2011-2012 George Yunaev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */
public class DlgChooseDirectory implements AdapterView.OnItemClickListener, DialogInterface.OnClickListener {
    List<File> m_entries = new ArrayList<File>();
    File m_currentDir;
    Context m_context;
    AlertDialog m_alertDialog;
    ListView m_list;
    EditText editText;
    Result m_result = null;

    public DlgChooseDirectory(Activity ctx, Result res, String startDir) {
        m_context = ctx;
        m_result = res;

        if (startDir != null)
            m_currentDir = new File(startDir);
        else
            m_currentDir = Environment.getExternalStorageDirectory();

        listDirs(ctx);
        final Dialog dialog = new Dialog(ctx, R.style.FullHeightDialog);

        View view = ((Activity) ctx).getLayoutInflater().inflate(R.layout.dlg_dirlist, null);
        TextView title = (TextView) view.findViewById(R.id.dlgtitle);
        Button btnOk = (Button) view.findViewById(R.id.buttonOk);
        editText = (EditText) view.findViewById(R.id.editText);
        editText.setText(m_currentDir.toString());
        title.setText(ctx.getString(R.string.dlg_choosedir_title));
        m_list = (ListView) view.findViewById(R.id.listView);

        DirAdapter adapter = new DirAdapter(android.R.layout.simple_list_item_1);

        m_list.setAdapter(adapter);

        m_list.setOnItemClickListener(this);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (m_result != null)
                    m_result.onChooseDirectory(editText.getText().toString());//m_currentDir.getAbsolutePath() );
                dialog.dismiss();
            }
        });
        dialog.setContentView(view);


        Dexter.withActivity(ctx)
                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        dialog.show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();


        /*
        DirAdapter adapter = new DirAdapter( android.R.layout.simple_list_item_1 );


        AlertDialog.Builder builder = new AlertDialog.Builder( ctx );
        builder.setTitle( R.string.dlg_choosedir_title );
        builder.setAdapter( adapter, this );

        builder.setPositiveButton( "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if ( m_result != null )
                    m_result.onChooseDirectory( m_currentDir.getAbsolutePath() );
                dialog.dismiss();
            }
        });



        AlertDialog m_alertDialog = builder.create();
        m_alertDialog.setContentView(view);
        m_list = m_alertDialog.getListView();
        m_list.setOnItemClickListener( this );
        if (m_context!= null && !((Activity)m_context).isFinishing()) {
            m_alertDialog.show();
        }
        */
    }

    private void listDirs(Context ctx) {
        m_entries.clear();

        // Get files
        File[] files = m_currentDir.listFiles();

        // Add the ".." entry
        if (m_currentDir.getParent() != null)
            m_entries.add(new File(".."));

        if (files != null) {
            for (File file : files) {
                if (!file.isDirectory() || !file.canWrite())
                    continue;

                m_entries.add(file);
            }
        }
        if (m_currentDir.getAbsolutePath().equals("/")) {
            File extSd = FileUtils.getExternalSdCardPath(ctx);
            boolean needAdd = true;
            if (extSd != null && files != null) {
                for (File file : files) {
                    if (file.getAbsolutePath().equals(extSd.getAbsolutePath())) {
                        needAdd = false;
                        break;
                    }
                }
            } else {
                needAdd = false;
            }
            if (extSd != null && needAdd) {
                m_entries.add(extSd);
            }
        }

        Collections.sort(m_entries, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return f1.getName().toLowerCase().compareTo(f2.getName().toLowerCase());
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View list, int pos, long id) {
        if (pos < 0 || pos >= m_entries.size())
            return;

        if (m_entries.get(pos).getName().equals(".."))
            m_currentDir = m_currentDir.getParentFile();
        else
            m_currentDir = m_entries.get(pos);

        editText.setText(m_currentDir.toString());
        listDirs(this.m_context);
        DirAdapter adapter = new DirAdapter(android.R.layout.simple_list_item_1);
        m_list.setAdapter(adapter);
    }

    public void onClick(DialogInterface dialog, int which) {
    }

    public interface Result {
        void onChooseDirectory(String dir);
    }

    public class DirAdapter extends ArrayAdapter<File> {
        public DirAdapter(int resid) {
            super(m_context, resid, m_entries);
        }

        // This function is called to show each view item
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textview = (TextView) super.getView(position, convertView, parent);

            if (m_entries.get(position) == null) {
                textview.setText("..");
                textview.setCompoundDrawablesWithIntrinsicBounds(m_context.getResources().getDrawable(R.drawable.freemp), null, null, null);
            } else {
                textview.setText(m_entries.get(position).getName());
                textview.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            }

            return textview;
        }
    }
}