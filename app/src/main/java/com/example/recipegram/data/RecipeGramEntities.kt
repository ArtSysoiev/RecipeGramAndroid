package com.example.recipegram.data

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(
    tableName = "users",
    indices = [Index(value = ["username"], unique = true)]
)
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val password: String,
    @ColumnInfo(name = "profile_image")
    val profileImage: String? = null
)

@Entity(
    tableName = "recipes",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["author_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["author_id"])]
)
data class Recipe(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String?,
    val time: String, // time to cook
    @ColumnInfo(name = "author_id")
    val authorId: Long,
    @ColumnInfo(name = "image_path")
    val imagePath: String?,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "ingredients",
    foreignKeys = [
        ForeignKey(
            entity = Recipe::class,
            parentColumns = ["id"],
            childColumns = ["recipe_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["recipe_id"])]
)
data class Ingredient(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "recipe_id")
    val recipeId: Long,
    val name: String,
    val amount: String
)

@Entity(
    tableName = "steps",
    foreignKeys = [
        ForeignKey(
            entity = Recipe::class,
            parentColumns = ["id"],
            childColumns = ["recipe_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["recipe_id"])]
)
data class Step(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "recipe_id")
    val recipeId: Long,
    val name: String?,
    val description: String,
    @ColumnInfo(name = "step_order")
    val stepOrder: Int
)

@Entity(
    tableName = "step_photos",
    foreignKeys = [
        ForeignKey(
            entity = Step::class,
            parentColumns = ["id"],
            childColumns = ["step_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["step_id"])]
)
data class StepPhoto(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "step_id")
    val stepId: Long,
    @ColumnInfo(name = "image_path")
    val imagePath: String,
    val caption: String?
)


data class StepWithPhotos(
    @Embedded val step: Step,
    @Relation(
        parentColumn = "id",
        entityColumn = "step_id"
    )
    val photos: List<StepPhoto>
)

data class RecipeFull(
    @Embedded val recipe: Recipe,

    @Relation(
        parentColumn = "author_id",
        entityColumn = "id"
    )
    val author: User,

    @Relation(
        parentColumn = "id",
        entityColumn = "recipe_id"
    )
    val ingredients: List<Ingredient>,

    @Relation(
        parentColumn = "id",
        entityColumn = "recipe_id",
        entity = Step::class
    )
    val steps: List<StepWithPhotos>
)