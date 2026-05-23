package com.harsh.fittrack.data.remote

import kotlin.concurrent.Volatile

class TokenStore {
    @Volatile
    var token: String? = null
}
