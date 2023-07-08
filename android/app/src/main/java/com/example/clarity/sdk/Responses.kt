package com.example.clarity.sdk

data class CreateUserResponse(val response: StatusResponse, val message: String, val userId: Int?, val username: String?)
data class GetUserResponse(val response: StatusResponse, val user: UserWithId?, val message: String)
data class CreateClassroomResponse(val response: StatusResponse, val id: String)
data class DeleteCardFromSetResponse(val response: StatusResponse, val msg: String)
data class CreateCardSetResponse(val response: StatusResponse, val msg: String)
data class GetCardsInSetResponse(val response: StatusResponse, val cards: List<String>)
data class GetSetsResponse(val response: StatusResponse, val sets: List<String>)
data class CreateCardResponse(val response: StatusResponse, val msg: String)
data class EvaluateResponse(val response: StatusResponse, val score: Int)
data class AddCardToSetResponse(val response: StatusResponse, val msg: String)
data class GetSetsByUsernameResponse(val response: StatusResponse, val data: List<SetMetadata>)
data class LoginResponse(val response: StatusResponse, val message: String, val user: UserWithId?)
data class JoinClassroomResponse(val response: StatusResponse, val id: String)
data class GetClassroomResponse(val response: StatusResponse, val id: List<String>)
data class GetDataForSetResponse(val response: StatusResponse, val data: List<String>)
data class GetProgressForSetResponse(val response: StatusResponse, val progress: Int)
data class UpdateProgressForSetResponse(val response: StatusResponse, val msg: String)
data class CreateClassroomAttemptResponse(val response: StatusResponse, val metadata: ClassroomAttemptMetadata?, val message: String)
data class GetUserAverageAttemptsResponse(val response: StatusResponse, val user_id: Int, val pronunciationScore: Double? = null, val accuracyScore: Double? = null,
                                          val fluencyScore: Double? = null, val completenessScore: Double? = null, val message: String)
data class CreateAttemptResponse(val response: StatusResponse, val metadata: AttemptMetadata?, val message: String)

data class PhraseSearchResponse(val response: StatusResponse, val cards: List<Card>)

