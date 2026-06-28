package org.aals.family.chore

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform