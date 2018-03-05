package com.r2m.kotlin.reactivewebapp.routes

import com.r2m.kotlin.reactivewebapp.handlers.BeerHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.router

@Configuration
class ApiRoutes(private val beerHandler: BeerHandler) {

    @Bean
    fun router() = router {

        "/rest".nest {

            "/beers".nest {

                accept(MediaType.APPLICATION_JSON).nest {
                    GET("", beerHandler::findAll)
                    GET("/{id}", beerHandler::findOne)
                }

                accept(MediaType.TEXT_EVENT_STREAM).nest {
                    GET("", beerHandler::findAllReactive)
                }
            }
        }
    }
}