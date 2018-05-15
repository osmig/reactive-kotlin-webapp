package com.r2m.kotlin.reactivewebapp.handlers

import com.r2m.kotlin.reactivewebapp.models.Beer
import com.r2m.kotlin.reactivewebapp.models.BeerType.*
import com.r2m.kotlin.reactivewebapp.repositories.BeerRepository
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.bodyToServerSentEvents
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono


private var beers = Flux.just(
    Beer("1", "1", "Brooklyn Lager", "Brooklyn Brewery", 5.2, LAGER),
    Beer("2", "2", "Lagunitas IPA", "Lagunitas Brewing Company", 6.2, ALE),
    Beer("3", "1", "Guinness", "Guinness", 4.2, OTHER)
)

@Component
class BeerHandler(private val beerRepository: BeerRepository) {
//
//  fun create(req: ServerRequest): Mono<ServerResponse> {
//    val addBeer: Mono<AddBeerDto> = req.bodyToMono(AddBeerDto::class.java)
//
//    return ok().body(
//        addBeer.flatMap {
//          val beer = Beer(
//              id = "3",
//              userId = it.userId,
//              name = it.name,
//              brewery = it.brewery,
//              alcoholByVolume = it.alcoholByVolume,
//              type = it.type
//          )
//
//          beers.add(beer)
//          Mono.just(beer)
//        }
//    )
//  }

  fun findAll(req: ServerRequest) = ok()
      .body(beerRepository.findAll())

  fun findAllReactive(req: ServerRequest) = ok()
      .bodyToServerSentEvents(beerRepository.findAll())

  fun findOne(serverRequest: ServerRequest): Mono<ServerResponse> {

    val id = serverRequest.pathVariable("id")
    return ok().body(Mono.justOrEmpty(beers.toIterable().firstOrNull { it.id == id }))
  }
}