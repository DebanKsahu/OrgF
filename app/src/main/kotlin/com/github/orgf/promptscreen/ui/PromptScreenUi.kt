package com.github.orgf.promptscreen.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.orgf.R
import com.github.orgf.promptscreen.ui.state.PromptCardUiState
import com.github.orgf.utils.enums.PromptCategory
import com.github.orgf.utils.ui.LightBlue
import com.github.orgf.utils.ui.OrgFTheme
import org.koin.androidx.compose.koinViewModel

private val PromptScreenBackground = Color(0xFF041A2C)
private val PromptSurface = Color(0xFF102234)
private val PromptSurfaceInactive = Color(0xFF0B1826)
private val PromptSurfaceSoft = Color(0xFF162A3A)
private val PromptStroke = Color(0xFF274258)
private val PromptStrokeInactive = Color(0xFF1B3044)
private val PromptMutedText = Color(0xFF8094AA)
private val PromptMutedTextInactive = Color(0xFF637A90)
private val PromptActive = Color(0xFF20C997)
private val PromptFabContainer = Color(0xFF2F79C7)
private val PromptFabIconTint = Color.White
private val PromptFontFamily = FontFamily.SansSerif
private val PromptCardHeight = 214.dp

private object PromptTypography {
    val topBarTitle = TextStyle(
        fontFamily = PromptFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.1.sp
    )
    val searchPlaceholder = TextStyle(
        fontFamily = PromptFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.2.sp
    )
    val chipLabel = TextStyle(
        fontFamily = PromptFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.2.sp
    )
    val cardTitle = TextStyle(
        fontFamily = PromptFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 19.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp
    )
    val cardDescription = TextStyle(
        fontFamily = PromptFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.2.sp
    )
}

@Composable
fun PromptScreenUi(
    onAddPromptClick: () -> Unit
) {
    val promptScreenLifecycleOwner = LocalLifecycleOwner.current

    val promptScreenViewModel: PromptScreenViewModel = koinViewModel()

    val defaultPromptFilters = listOf("All") + PromptCategory.entries.map { it.name }

    val promptScreenUiState =
        promptScreenViewModel.promptScreenUiState.collectAsStateWithLifecycle()
    val selectedPromptFilter = remember { mutableStateOf("All") }

    DisposableEffect(promptScreenLifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                promptScreenViewModel.loadAllPrompt()
            }
        }

        promptScreenLifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            promptScreenLifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


    OrgFTheme(darkTheme = true, dynamicColor = false) {
        PromptScreenUiContent(
            promptFilters = defaultPromptFilters,
            selectedPromptFilter = selectedPromptFilter.value,
            promptCards = promptScreenUiState.value.promptList ?: emptyList(),
            isLoading = promptScreenUiState.value.isLoading,
            error = promptScreenUiState.value.error,
            onSelectFilter = { selectedFilter ->
                selectedPromptFilter.value = selectedFilter
                if (selectedFilter == "All") promptScreenViewModel.loadAllPrompt()
                else promptScreenViewModel.loadPromptByCategory(filter = selectedFilter)
            },
            onSwitchStateChange = { promptId, isEnabled ->
                promptScreenViewModel.updatePromptActiveStatus(
                    promptId = promptId,
                    isEnabled = isEnabled
                )
            },
            onAddPromptClick = {
                onAddPromptClick()
            }
        )
    }
}

@Composable
fun PromptScreenUiContent(
    promptFilters: List<String>,
    selectedPromptFilter: String,
    promptCards: List<PromptCardUiState>,
    isLoading: Boolean,
    error: String?,
    onSelectFilter: (String) -> Unit,
    onSwitchStateChange: (Long, Boolean) -> Unit,
    onAddPromptClick: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = PromptScreenBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddPromptClick,
                containerColor = PromptFabContainer,
                contentColor = PromptFabIconTint,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 12.dp,
                    pressedElevation = 18.dp,
                    focusedElevation = 12.dp,
                    hoveredElevation = 12.dp
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.outline_add_2_24),
                    contentDescription = "Add prompt"
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(PromptScreenBackground),
            contentPadding = PaddingValues(
                start = 0.dp,
                top = innerPadding.calculateTopPadding(),
                end = 0.dp,
                bottom = innerPadding.calculateBottomPadding() + 8.dp
            )
        ) {
            item { PromptTopBar() }
            item { PromptSearchField() }
            item { Spacer(modifier = Modifier.height(12.dp)) }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(promptFilters) { filter ->
                        PromptFilterChip(
                            text = filter,
                            selected = filter == selectedPromptFilter,
                            onSelectFilter = onSelectFilter
                        )
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(18.dp)) }
            when {
                isLoading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = LightBlue)
                        }
                    }
                }

                !error.isNullOrBlank() -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                promptCards.isEmpty() -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No prompts found",
                                color = PromptMutedText
                            )
                        }
                    }
                }

                else -> {
                    items(promptCards) { card ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            PromptCard(promptCard = card, onSwitchStateChange = onSwitchStateChange)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PromptTopBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Prompts",
            color = Color.White,
            style = PromptTypography.topBarTitle
        )
    }
}

@Composable
private fun PromptSearchField() {
    TextField(
        value = "",
        onValueChange = {},
        readOnly = true,
        placeholder = {
            Text(
                text = "Search rules...",
                color = PromptMutedText,
                style = PromptTypography.searchPlaceholder
            )
        },
        leadingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_search),
                contentDescription = "Search",
                tint = PromptMutedText
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(18.dp)),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = PromptSurfaceSoft,
            unfocusedContainerColor = PromptSurfaceSoft,
            disabledContainerColor = PromptSurfaceSoft,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            cursorColor = Color.Transparent
        )
    )
}

@Composable
private fun PromptFilterChip(text: String, selected: Boolean, onSelectFilter: (String) -> Unit) {
    Card(
        shape = RoundedCornerShape(50),
        colors = CardDefaults.cardColors(containerColor = if (selected) LightBlue else PromptSurfaceSoft),
        border = if (selected) null else BorderStroke(
            1.dp,
            PromptStroke
        ),
        onClick = {
            onSelectFilter(text)
        }
    ) {
        Text(
            text = text,
            color = if (selected) Color.Black else PromptMutedText,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
            style = PromptTypography.chipLabel
        )
    }
}

@Composable
private fun PromptCard(
    promptCard: PromptCardUiState,
    onSwitchStateChange: (Long, Boolean) -> Unit
) {
    val cardContainerColor = if (promptCard.isEnabled) PromptSurface else PromptSurfaceInactive
    val cardStrokeColor = if (promptCard.isEnabled) PromptStroke else PromptStrokeInactive
    val titleColor = if (promptCard.isEnabled) Color.White else PromptMutedTextInactive
    val metadataColor = if (promptCard.isEnabled) PromptMutedText else PromptMutedTextInactive
    val iconTint = if (promptCard.isEnabled) LightBlue else PromptMutedTextInactive
    val iconContainerColor = if (promptCard.isEnabled) Color(0xFF1A3A52) else PromptSurfaceSoft

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(PromptCardHeight)
            .border(1.dp, cardStrokeColor, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (promptCard.isEnabled) 2.dp else 0.dp),
        colors = CardDefaults.cardColors(containerColor = cardContainerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconContainerColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = promptCard.iconRes),
                        contentDescription = promptCard.promptCategory,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.size(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = promptCard.promptCategory,
                            color = titleColor,
                            style = PromptTypography.cardTitle
                        )
                        Spacer(modifier = Modifier.size(10.dp))
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (promptCard.isEnabled) PromptActive else PromptMutedText)
                        )
                    }
                }
                Switch(
                    checked = promptCard.isEnabled,
                    onCheckedChange = {
                        onSwitchStateChange(
                            promptCard.promptId,
                            it
                        )
                    },
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = LightBlue,
                        checkedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFF324758),
                        uncheckedThumbColor = Color.White
                    )
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = promptCard.promptText,
                color = metadataColor,
                style = PromptTypography.cardDescription,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.alpha(if (promptCard.isEnabled) 1f else 0.75f)
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}


private fun buildPromptDescription(
    baseText: String,
    emphasizedText: String,
    tailText: String,
    highlightedTail: String? = null
): AnnotatedString {
    return buildAnnotatedString {
        withStyle(SpanStyle(color = PromptMutedText)) { append(baseText) }
        withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.Bold)) {
            append(
                emphasizedText
            )
        }
        if (highlightedTail != null && tailText.contains(highlightedTail)) {
            val prefix = tailText.substringBefore(highlightedTail)
            val suffix = tailText.substringAfter(highlightedTail)
            withStyle(SpanStyle(color = PromptMutedText)) { append(prefix) }
            withStyle(SpanStyle(color = LightBlue, fontWeight = FontWeight.SemiBold)) {
                append(
                    highlightedTail
                )
            }
            withStyle(SpanStyle(color = PromptMutedText)) { append(suffix) }
        } else {
            withStyle(SpanStyle(color = PromptMutedText)) { append(tailText) }
        }
    }
}

@Preview
@Composable
fun PromptScreenUiPreview() {
    val defaultPromptFilters = listOf("All", "Images", "Documents", "Videos")
    val defaultPromptCards = listOf(
        PromptCardUiState(
            promptId = 1L,
            promptText = buildPromptDescription(
                baseText = "Move all ",
                emphasizedText = "PDF invoices",
                tailText = " to Taxes/2023 folder.",
                highlightedTail = "Taxes/2023"
            ),
            promptCategory = "Downloads",
            isEnabled = true,
            iconRes = R.drawable.ic_folder,
        ),
        PromptCardUiState(
            promptId = 2L,
            promptText = buildPromptDescription(
                baseText = "Identify ",
                emphasizedText = "blurry photos",
                tailText = " and move to trash."
            ),
            promptCategory = "Camera Roll",
            isEnabled = true,
            iconRes = R.drawable.ic_image,
        ),
        PromptCardUiState(
            promptId = 3L,
            promptText = AnnotatedString("Automated sorting for project files and contracts."),
            promptCategory = "Documents",
            isEnabled = false,
            iconRes = R.drawable.ic_document,
        ),
        PromptCardUiState(
            promptId = 4L,
            promptText = AnnotatedString("Sort project assets by client and quarter automatically."),
            promptCategory = "Projects",
            isEnabled = true,
            iconRes = R.drawable.ic_folder,
        ),
        PromptCardUiState(
            promptId = 5L,
            promptText = AnnotatedString("Move transcribed meeting notes into Work/Meetings."),
            promptCategory = "Voice Notes",
            isEnabled = false,
            iconRes = R.drawable.ic_lock,
        )
    )

    OrgFTheme(darkTheme = true, dynamicColor = false) {
        PromptScreenUiContent(
            onSelectFilter = {},
            promptFilters = defaultPromptFilters,
            selectedPromptFilter = defaultPromptFilters[0],
            promptCards = defaultPromptCards,
            isLoading = false,
            error = null,
            onSwitchStateChange = { _, _ ->

            },
            onAddPromptClick = {},
        )
    }
}
