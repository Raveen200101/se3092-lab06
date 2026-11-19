package com.example.lab06

import android.content.Context

internal const val LAST_RECIPIENT_ACCOUNT = "last_recipient_account"
internal const val LAST_RECIPIENT_NAME = "last_recipient_name"

internal fun Context.lastRecipientPreferences() =
    getSharedPreferences("banking_app_prefs", Context.MODE_PRIVATE)
