package com.r2m.kotlin.reactivewebapp.models

import java.time.LocalDate

data class Beer(
    val id: String,
    val userId: String,
    val name: String,
    val brewery: String,
    val alcoholByVolume: Double,
    val type: BeerType
)

enum class BeerType {
    LAGER,
    ALE,
    STOUT,
    OTHER
}