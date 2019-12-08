package com.artemglotov.taskmanager.models

import android.graphics.drawable.Drawable
import java.io.Serializable

data class App (
    var name: String,
    var packageName: String,
    var icon: Drawable
) : Serializable
