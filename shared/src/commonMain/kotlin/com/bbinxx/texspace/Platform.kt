package com.bbinxx.texspace

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform