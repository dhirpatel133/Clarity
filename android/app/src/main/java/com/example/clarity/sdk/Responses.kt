package com.example.clarity.sdk

data class CreateUserResponse(val response: StatusResponse, val message: String, val userId: Int?, val username: String?)
data class GetUserResponse(val response: StatusResponse, val user: UserWithId?, val message: String)
data class GetAllUsersResponse(val response: StatusResponse, val users: List<UserWithId>)
data class CreateClassroomResponse(val response: StatusResponse, val id: String)
data class DeleteCardFromSetResponse(val response: StatusResponse, val msg: String)
data class CreateCardSetResponse(val response: StatusResponse, val msg: String, val set: SetMetadata? = null)
data class GetCardsInSetResponse(val response: StatusResponse, val cards: List<Card>)
data class GetSetIDsResponse(val response: StatusResponse, val sets: List<String>)
data class CreateCardResponse(val response: StatusResponse, val msg: String, val card: Card?)
data class AddCardToSetResponse(val response: StatusResponse, val msg: String)
data class GetSetsByUsernameResponse(val response: StatusResponse, val data: List<SetMetadata>)
data class getPublicCardSetsOrderedByLikesResponse(val response: StatusResponse, val sets: List<SetMetadata>)
data class LoginResponse(val response: StatusResponse, val message: String, val user: UserWithId?)
data class JoinClassroomResponse(val response: StatusResponse, val id: String)
data class GetClassroomResponse(val response: StatusResponse, val id: List<ClassroomReturnObject>)
data class CreateClassroomAttemptResponse(val response: StatusResponse, val metadata: ClassroomAttemptMetadata?, val message: String)
data class ClassroomReturnObject(val code: String, val name: String, val teacherID: String)
data class GetUserAverageAttemptsResponse(val response: StatusResponse, val user_id: Int, val pronunciationScore: Double? = null, val accuracyScore: Double? = null,
                                          val fluencyScore: Double? = null, val completenessScore: Double? = null, val message: String)
data class CreateAttemptResponse(val response: StatusResponse, val metadata: AttemptMetadata?, val message: String)
data class PhraseSearchResponse(val response: StatusResponse, val cards: List<Card>)
data class GetUserAttemptsResponse(val user_id: Int, val attempts: List<CardAttempt>, val response: StatusResponse)
data class GetAttemptsForSetResponse(val user_id: Int, val set_id: Int, val attempts: List<CardAttempt>, val response: StatusResponse)
data class GetTaskAttemptsResponse(val task_id: Int, val attempts: List<TaskAttemptWithName>, val response: StatusResponse)
data class GetClassAttemptsResponse(val classroom: String, val attempts: List<TaskAttemptWithNameAndClass>, val response: StatusResponse)
data class CreateTaskResponse(val response: StatusResponse, val id: String)
data class GetTasksResponse(val response: StatusResponse, val id: List<Task>)
data class Announcement(val announcementId: Int, val classId: String, val text: String, val description: String, val dateCreated: String)
data class AnnouncementResponse(val response: StatusResponse, val message: String)
data class GetAnnouncementsResponse(val response: StatusResponse, val result: List<Announcement>)
data class FollowingResponse(val response: StatusResponse, val message: String)
data class FollowerListResponse(val response: StatusResponse, val followers: List<Int>)
data class GetClassroomTaskProgressResponse(val response: StatusResponse, val task_id: Int, val card_count: Int?, val studentProgress: List<StudentProgress>?, val message: String)
data class LikeCardSetResponse(val response: StatusResponse, val message: String)
data class UnlikeCardSetResponse(val response: StatusResponse, val message: String)
data class ToggleCardSetResponse(val response: StatusResponse, val is_public: Int)
data class GetWordsResponse(val response: StatusResponse, val result: List<String>)
data class UpdateDifficultyResponse(val response: StatusResponse, val newDifficulty: Difficulty?, val message: String)
data class UpdateTaskDifficultyResponse(val response: StatusResponse, val newDifficulty: Difficulty? = null, val message: String)
data class EditUserResponse(val response: StatusResponse, val message: String)
data class ChangePasswordResponse(val response: StatusResponse, val message: String)
data class GetPublicCardSetsResponse(val response: StatusResponse, val sets: List<SetMetadata>)
data class GetSetDataResponse(val response: StatusResponse, val set: CardSet? = null)
data class ClonePublicSetResponse(val response: StatusResponse, val new_set_id: Int, val msg: String)
data class GetUnreadResponse(val response: StatusResponse, val messages: List<Notification>)
data class NotificationResponse(val response: StatusResponse, val message: String)
data class Notification(val notificationId: Int, val userId: Int, val message: String, val notificationDate: String, val messageRead: Int)
data class PracticeAttemptResponse(val response: StatusResponse, val metadata: AttemptMetadata?, val message: String)
data class PracticeClassroomAttemptResponse(val response: StatusResponse, val metadata: ClassroomAttemptMetadata?, val message: String)
