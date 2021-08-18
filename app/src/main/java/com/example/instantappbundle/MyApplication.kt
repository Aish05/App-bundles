package com.example.instantappbundle

import android.app.Application
import android.content.Context
import com.google.android.play.core.splitcompat.SplitCompat


class MyApplication : Application() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        SplitCompat.install(this)
    }
}