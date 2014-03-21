/*
 * vim: set sta sw=4 et:
 *
 * Copyright (C) 2012 Liu DongMiao <thom@piebridge.me>
 *
 * This program is free software. It comes without any warranty, to
 * the extent permitted by applicable law. You can redistribute it
 * and/or modify it under the terms of the Do What The Fuck You Want
 * To Public License, Version 2, as published by Sam Hocevar. See
 * http://sam.zoy.org/wtfpl/COPYING for more details.
 *
 */

package me.piebridge.curl;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

public class Demo extends Activity
{

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        ImageView textView = new ImageView(this);

        try {
            Data data = new Data();
            data.getURL("http://www.idmebeles.lv/files/201304101004041588957484_2.png");
            byte[] b =data.getOutputStream().toByteArray();
            textView.setImageBitmap(BitmapFactory.decodeByteArray(b,0,b.length));
        } catch (UnsatisfiedLinkError e) {
            //textView.setText("UnsatisfiedLinkError");
        } catch (ExceptionInInitializerError e) {
            //textView.setText("ExceptionInInitializerError");
        }
        setContentView(textView);
    }


}
