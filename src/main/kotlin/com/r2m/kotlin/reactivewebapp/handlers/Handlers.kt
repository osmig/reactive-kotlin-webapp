package com.r2m.kotlin.reactivewebapp.handlers

import com.r2m.kotlin.reactivewebapp.models.Beer
import com.r2m.kotlin.reactivewebapp.models.BeerType
import com.r2m.kotlin.reactivewebapp.models.User
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.bodyToServerSentEvents
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.LocalDate


private val beers = Flux.just(
    Beer("1", "1", "Brooklyn Lager", "Brooklyn Brewery", 5.6, BeerType.LAGER),
    Beer("2", "2", "Elefantöl", "Elefantbryggerier AB", 15.3, BeerType.OTHER),
    Beer("3", "1", "Brooklyn IPA", "Brooklyn Brewery", 7.6, BeerType.ALE)
)

private val users = Flux.just(
    User("1", "Foo", "Foo", LocalDate.now().minusDays(1)),
    User("2", "Bar", "Bar", LocalDate.now().minusDays(10)),
    User("3", "Baz", "Baz", LocalDate.now().minusDays(100)))

@Component
class UserHandler {

  private val userStream = Flux
      .interval(Duration.ofMillis(500))
      .zipWith(beers.repeat())
      .map { it.t2 }

  fun findAll(req: ServerRequest) =
      ok().body(users)
}

class UserDto(val firstName: String, val lastName: String, val birthDate: String)

fun User.toDto() = UserDto(firstName, lastName, birthDate.toString())

@Component
class BeerHandler {

  private val beerStream = Flux
      .interval(Duration.ofMillis(15))
      .zipWith(beers.repeat())
      .map { it.t2 }


  fun findAll(req: ServerRequest) = ok().body(beers)

  fun findAllReactive(req: ServerRequest)= ok().bodyToServerSentEvents(beerStream)

  fun findOne(serverRequest: ServerRequest): Mono<ServerResponse> {

    val id = serverRequest.pathVariable("id")
    return ok().body(Mono.justOrEmpty(beers.toIterable().firstOrNull { it.id == id }))
  }
}