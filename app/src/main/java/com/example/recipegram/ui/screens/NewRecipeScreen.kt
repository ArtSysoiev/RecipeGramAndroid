package com.example.recipegram.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
//import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.rememberAsyncImagePainter
import com.example.recipegram.repository.StepDataUI
import com.example.recipegram.ui.components.AuthInput
import com.example.recipegram.ui.viewmodel.AuthViewModel
import com.example.recipegram.ui.viewmodel.NewRecipeViewModel

@Composable
fun NewRecipeScreen(
    onPublishSuccess: () -> Unit,
    viewModel: NewRecipeViewModel = hiltViewModel<NewRecipeViewModel>(),
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    val currentUser by authViewModel.currentUser.collectAsState()
    val isPublishing by viewModel.isPublishing.collectAsState()
    val publishSuccess by viewModel.publishSuccess.collectAsState()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var coverUri by remember { mutableStateOf<Uri?>(null) }

    var isDescriptionExpanded by remember { mutableStateOf(false) }

    val coverPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { coverUri = it }

    LaunchedEffect(publishSuccess) {
        if (publishSuccess) onPublishSuccess()
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("New Recipe", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant).clickable {
                    coverPicker.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            if (coverUri != null) {
                Image(
                    rememberAsyncImagePainter(coverUri),
                    null,
                    Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CameraAlt, null, Modifier.size(48.dp))
                    Text("Add Cover Photo")
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        AuthInput("Recipe Name", name, { name = it }, Icons.Default.RestaurantMenu)
        AuthInput("Cooking Time (e.g. 45 min)", time, { time = it }, Icons.Default.Timer)

        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            onClick = { isDescriptionExpanded = !isDescriptionExpanded },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(12.dp).animateContentSize()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Description, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Description", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    Icon(
                        if (isDescriptionExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null
                    )
                }
                AnimatedVisibility(
                    visible = isDescriptionExpanded,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        placeholder = { Text("Tell us about this dish...") },
                        minLines = 3
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Text("Ingredients", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        viewModel.ingredients.forEachIndexed { index, ing ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                OutlinedTextField(ing.name, { viewModel.updateIngredient(index, it, ing.amount) },
                    Modifier.weight(2f), placeholder = { Text("Item") }, singleLine = true)
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(ing.amount, { viewModel.updateIngredient(index, ing.name, it) },
                    Modifier.weight(1f), placeholder = { Text("Amount") }, singleLine = true)
                IconButton(onClick = { viewModel.removeIngredient(index) }) {
                    Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                }
            }
        }
        OutlinedButton(onClick = { viewModel.addIngredient() }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Add, null)
            Text("Add Ingredient")
        }

        Spacer(Modifier.height(24.dp))

        Text("Instructions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        viewModel.steps.forEachIndexed { index, step ->
            StepPreviewItem(index, step, onClick = { viewModel.openStepModal(index) })
        }
        OutlinedButton(onClick = { viewModel.addStep() }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Add, null)
            Text("Add Step")
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = {
                if (name.isBlank() || time.isBlank()) {
                    Toast.makeText(context, "Name and Time are required", Toast.LENGTH_SHORT).show()
                } else {
                    currentUser?.let { viewModel.publishRecipe(name, description, time, it.id, coverUri?.toString()) }
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = !isPublishing
        ) {
            if (isPublishing) CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
            else Text("Publish Recipe", fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(50.dp))
    }

    if (viewModel.isStepModalVisible) {
        StepEditDialog(viewModel)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepPreviewItem(index: Int, step: StepDataUI, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape,
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "${index + 1}",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                if (!step.name.isNullOrBlank()) {
                    Text(
                        text = step.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = step.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (step.imageUri != null) {
                Icon(
                    Icons.Default.Image,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
            }

            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepEditDialog(viewModel: NewRecipeViewModel) {
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            viewModel.tempStep = viewModel.tempStep.copy(imageUri = uri.toString())
        }
    }

    Dialog(
        onDismissRequest = { viewModel.isStepModalVisible = false },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (viewModel.editingStepIndex == null) "New Step" else "Edit Step",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { viewModel.isStepModalVisible = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable {
                            picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (viewModel.tempStep.imageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(viewModel.tempStep.imageUri),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Surface(
                            color = Color.Black.copy(alpha = 0.5f),
                            shape = CircleShape,
                            modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
                        ) {
                            Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.padding(8.dp).size(20.dp))
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddAPhoto, null, Modifier.size(48.dp))
                            Text("Add Step Photo")
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = viewModel.tempStep.name ?: "",
                    onValueChange = { viewModel.tempStep = viewModel.tempStep.copy(name = it) },
                    label = { Text("Headline (e.g. Preheat Oven)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = viewModel.tempStep.description,
                    onValueChange = { viewModel.tempStep = viewModel.tempStep.copy(description = it) },
                    label = { Text("Instructions") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 5
                )

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.saveStep() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Step", fontWeight = FontWeight.Bold)
                }

                if (viewModel.editingStepIndex != null) {
                    TextButton(
                        onClick = {
                            viewModel.removeStep(viewModel.editingStepIndex!!)
                            viewModel.isStepModalVisible = false
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Remove Step")
                    }
                }
            }
        }
    }
}