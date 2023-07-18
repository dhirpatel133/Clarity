package com.example.clarity.sdk

data class CreateUserResponse(val response: StatusResponse, val message: String, val userId: Int?, val username: String?)
data class GetUserResponse(val response: StatusResponse, val user: UserWithId?, val message: String)
data class CreateClassroomResponse(val response: StatusResponse, val id: String)
data class DeleteCardFromSetResponse(val response: StatusResponse, val msg: String)
data class CreateCardSetResponse(val response: StatusResponse, val msg: String, val set: SetMetadata? = null)
data class GetCardsInSetResponse(val response: StatusResponse, val cards: List<Card>)
data class GetSetsResponse(val response: StatusResponse, val sets: List<String>)
data class CreateCardResponse(val response: StatusResponse, val msg: String, val card: Card?)
data class AddCardToSetResponse(val response: StatusResponse, val msg: String)
data class GetSetsByUsernameResponse(val response: StatusResponse, val data: List<SetMetadata>)
data class LoginResponse(val response: StatusResponse, val message: String, val user: UserWithId?)
data class JoinClassroomResponse(val response: StatusResponse, val id: String)
data class GetClassroomResponse(val response: StatusResponse, val id: List<String>)
data class GetDataForSetResponse(val response: StatusResponse, val data: List<String>)data class CreateClassroomAttemptResponse(val response: StatusResponse, val metadata: ClassroomAttemptMetadata?, val message: String)
data class GetUserAverageAttemptsResponse(val response: StatusResponse, val user_id: Int, val pronunciationScore: Double? = null, val accuracyScore: Double? = null,
                                          val fluencyScore: Double? = null, val completenessScore: Double? = null, val message: String)
data class CreateAttemptResponse(val response: StatusResponse, val metadata: AttemptMetadata?, val message: String)
data class PhraseSearchResponse(val response: StatusResponse, val cards: List<Card>)
data class GetUserAttemptsResponse(val user_id: Int, val attempts: List<CardAttempt>, val response: StatusResponse)
data class GetAttemptsForSetResponse(val user_id: Int, val set_id: Int, val attempts: List<CardAttempt>, val response: StatusResponse)

data class GetTaskAttemptsResponse(val task_id: Int, val attempts: List<TaskAttemptWithName>, val response: StatusResponse)
data class GetClassAttemptsResponse(val classroom: String, val attempts: List<TaskAttemptWithNameAndClass>, val response: StatusResponse)

data class GetClassroomTaskProgressResponse(val response: StatusResponse, val task_id: Int, val card_count: Int?, val studentProgress: List<StudentProgress>?, val message: String)
