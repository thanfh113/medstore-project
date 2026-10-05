package com.example.nhathuoc.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    // Hilt t? d?ng kh?i t?o CartRepository v� CheckoutRepository 
    // th�ng qua @Inject constructor c� trong c�c class n�y.
    // N?u sau n�y c� Interface Repository, b?n m?i c?n th�m @Binds v�o d�y.
}