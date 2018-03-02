package com.r2m.kotlin.reactivewebapp.routes

import com.r2m.kotlin.reactivewebapp.handlers.BeerHandler
import com.r2m.kotlin.reactivewebapp.handlers.UserHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType

@Configuration
class ApiRoutes(
    private val userHandler: UserHandler,
    private val beerHandler: BeerHandler) {

    @Bean
    fun router() = org.springframework.web.reactive.function.server.router {

        "/rest".nest {
            "/users".nest {
                accept(MediaType.APPLICATION_JSON).nest {
                    GET("", userHandler::findAll)
                }
            }

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