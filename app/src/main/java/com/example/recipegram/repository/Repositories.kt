package com.example.recipegram.repository

import android.content.Context
import android.net.Uri
import com.example.recipegram.data.RecipeGramDao
import com.example.recipegram.data.User
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.net.toUri

@Singleton
class FileRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun saveImageToInternalStorage(uriString: String?): String? {
        if (uriString == null) return null

        return withContext(Dispatchers.IO) {
            try {
                val uri = uriString.toUri()
                val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null

                val fileName = "img_${UUID.randomUUID()}.jpg"
                val file = File(context.filesDir, fileName)

                file.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }

                file.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}

@Singleton
class AuthRepository @Inject constructor(
    private val dao: RecipeGramDao,
    private val fileRepository: FileRepository
) {

    suspend fun login(username: String, password: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val user = dao.getUserByUsername(username)
                if (user != null && user.password == password) {
                    Result.success(user)
                } else {
                    Result.failure(Exception("Invalid credentials"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun register(username: String, password: String, imageUri: String?): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val existing = dao.getUserByUsername(username)
                if (existing != null) {
                    return@withContext Result.failure(Exception("User already exists"))
                }

                val savedImagePath = fileRepository.saveImageToInternalStorage(imageUri)

                val newUser = User(
                    username = username,
                    password = password,
                    profileImage = savedImagePath
                )

                val newId = dao.insertUser(newUser)

                Result.success(newUser.copy(id = newId))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}