package com.example.instantappbundle

import android.util.Log
import android.widget.Toast

fun MainActivity.toastAndLog(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    Log.d(TAG, text)
}


const val TAG = "DynamicFeatures Demo"
