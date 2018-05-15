package com.r2m.kotlin.reactivewebapp.repositories

import com.r2m.kotlin.reactivewebapp.models.Beer
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository

@Repository("beer")
interface BeerRepository : ReactiveMongoRepository<Beer, String>