package com.r2m.kotlin.reactivewebapp.handlers

import com.r2m.kotlin.reactivewebapp.models.Beer
import com.r2m.kotlin.reactivewebapp.models.BeerType.*
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.bodyToServerSentEvents
import reactor.core.publisher.Flux
import reactor.core.publisher.Flux.just
import reactor.core.publisher.Mono
import java.time.Duration


private val beers = just(
    Beer("1", "1", "Brooklyn Lager", "Brooklyn Brewery", 5.2, LAGER),
    Beer("2", "2", "Lagunitas IPA", "Lagunitas Brewing Company", 6.2, ALE),
    Beer("3", "1", "Guinness", "Guinness", 4.2, OTHER)
)

@Component
class BeerHandler {

  private val beerStream = Flux
      .interval(Duration.ofMillis(100))
      .zipWith(beers)
      .map { it.t2 }


  fun findAll(req: ServerRequest) = ok().body(beers)

  fun findAllReactive(req: ServerRequest)= ok().bodyToServerSentEvents(beerStream)

  fun findOne(serverRequest: ServerRequest): Mono<ServerResponse> {

    val id = serverRequest.pathVariable("id")
    return ok().body(Mono.justOrEmpty(beers.toIterable().firstOrNull { it.id == id }))
  }
}