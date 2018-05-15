package com.r2m.kotlin.reactivewebapp.models

import com.r2m.kotlin.reactivewebapp.models.BeerType.*

data class Beer(
    val id: String,
    val userId: String,
    val name: String,
    val brewery: String,
    val alcoholByVolume: Double,
    val type: BeerType
)

data class AddBeerDto(
    val userId: String,
    val name: String,
    val brewery: String,
    val alcoholByVolume: Double,
    val type: BeerType = OTHER
)

enum class BeerType {
    LAGER,
    ALE,
    STOUT,
    OTHER
}