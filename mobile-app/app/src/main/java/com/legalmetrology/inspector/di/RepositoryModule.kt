package com.legalmetrology.inspector.di

import com.legalmetrology.inspector.data.repository.FixtureRecommendationRepository
import com.legalmetrology.inspector.domain.repository.RecommendationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds the recommendation repository.
 *
 * SWAP POINT: to go live, write a Retrofit-backed implementation of
 * [RecommendationRepository] and change the binding below. Every screen
 * and ViewModel is written against the interface, so nothing else moves.
 *
 * The server itself will sit behind its own provider-agnostic generation
 * interface, so choosing a cloud LLM or a local model stays a server-side
 * decision and never leaks into the client.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindRecommendationRepository(
        impl: FixtureRecommendationRepository
    ): RecommendationRepository
}
