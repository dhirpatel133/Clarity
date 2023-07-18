package com.example.clarity.sdk

import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.Part


data class LoginRequest(val username: String, val password: String)
data class CreateUserEntity(val user: User)
data class User(val username: String, val email: String, val password: String, val firstname: String, val lastname: String, val phone_number: String)
data class JoinClassroomEntity(val privateCode: String, val userID: String)
data class CreateClassroomEntity(val name: String, val teacher: Integer)
data class CreateCardSetEntity(val creator_id: Int, val title: String, val type: String)
data class GetDataForSetRequest(val set_id: Int)
data class CreateCardEntity(val phrase: String, val title: String, val setId: Int? = null)
data class AddCardToSetRequest(val card_id: Int, val set_id: Int)
data class DeleteCardFromSetRequest(val card_id: Int, val set_id: Int)
data class GetCardsInSetRequest(val set_id: Int)
data class GetUserSetProgressRequest(val set_id: Int, val user_id: Int)

data class GetUserSetProgressResponse(val response: StatusResponse, val set_id: Int, val user_id: Int, val numCards: Int, val numCompletedCards: Int, val cards: List<Card>, val completedCard: List<CardInSet>)

data class GetUserAverageAttemptsRequest(val user_id: Int)
data class PhraseSearchEntity(val phrase: String)
data class GetAttemptsForSetEntity(val user: Int, val set: Int)



