package com.harsh.fittrack

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform