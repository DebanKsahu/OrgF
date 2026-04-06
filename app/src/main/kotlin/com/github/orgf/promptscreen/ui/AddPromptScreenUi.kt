package com.github.orgf.promptscreen.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.orgf.R
import com.github.orgf.utils.enums.PromptCategory
import com.github.orgf.utils.ui.OrgFTheme
import org.koin.androidx.compose.koinViewModel

private val AddPromptScreenBackground = Color(0xFF07172A)
private val AddPromptScreenTop = Color(0xFF08172B)
private val AddPromptScreenBottom = Color(0xFF071423)
private val AddPromptScreenHeader = Color(0xFF0B1528)
private val AddPromptScreenPanelSoft = Color(0xFF0E1A2B)
private val AddPromptScreenStroke = Color(0xFF2A3F59)
private val AddPromptScreenStrokeSoft = Color(0xFF21344A)
private val AddPromptScreenMuted = Color(0xFF8EA0B8)
private val AddPromptScreenMutedSoft = Color(0xFF5F7289)
private val AddPromptScreenHint = Color(0xFF6A7D95)
private val AddPromptScreenChipInactive = Color(0xFF17263A)
private val AddPromptScreenChipActive = Color(0xFF1FA4F0)
private val AddPromptScreenButton = Color(0xFF23A8EA)
private val AddPromptFontFamily = FontFamily.SansSerif
private const val AddPromptMaxCharCount = 2000

private object AddPromptTypography {
    val title = TextStyle(
        fontFamily = AddPromptFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.1.sp
    )

    val sectionLabel = TextStyle(
        fontFamily = AddPromptFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.8.sp
    )

    val fieldText = TextStyle(
        fontFamily = AddPromptFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )

    val chipText = TextStyle(
        fontFamily = AddPromptFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp
    )

    val saveButtonText = TextStyle(
        fontFamily = AddPromptFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )

    val promptHint = TextStyle(
        fontFamily = AddPromptFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp
    )

    val characterCount = TextStyle(
        fontFamily = AddPromptFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.2.sp
    )
}

@Composable
fun AddPromptScreenUi(
    modifier: Modifier = Modifier,
    onSavePromptClick: () -> Unit
) {
    val addPromptScreenViewModel: AddPromptScreenViewModel = koinViewModel()

    OrgFTheme(darkTheme = true, dynamicColor = false) {
        AddPromptScreenContent(
            modifier = modifier,
            onFileTypeClick = { currPromptCategory ->
                addPromptScreenViewModel.updatePromptCategory(promptCategory = currPromptCategory)
            },
            onSavePromptClick = {
                addPromptScreenViewModel.addPrompt()
                onSavePromptClick()
            },
            onFolderNameChange = { currDestinationFolder ->
                addPromptScreenViewModel.updateDestinationFolder(destinationFolder = currDestinationFolder)
            },
            onPromptTextChange = { currPrompt ->
                addPromptScreenViewModel.updatePrompt(prompt = currPrompt)
            }
        )
    }
}

@Composable
fun AddPromptScreenContent(
    modifier: Modifier = Modifier,
    onFileTypeClick: (PromptCategory) -> Unit = {},
    onSavePromptClick: () -> Unit = {},
    onFolderNameChange: (String) -> Unit = {},
    onPromptTextChange: (String) -> Unit = {},
    initialFolderName: String = "",
    initialPromptText: String = "",
    initialFileTypeExpanded: Boolean = false
) {
    val fileTypes = remember { PromptCategory.entries.toList() }
    var selectedFileType by remember { mutableStateOf(fileTypes.firstOrNull()) }
    var isFileTypeExpanded by remember { mutableStateOf(initialFileTypeExpanded) }



    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AddPromptScreenBackground)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(AddPromptScreenTop, AddPromptScreenBottom)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            AddPromptTopBar()
            Spacer(modifier = Modifier.height(30.dp))

            AddPromptSectionLabel(text = "FOLDER NAME")
            Spacer(modifier = Modifier.height(12.dp))
            AddPromptFolderNameField(
                onFolderNameChange = onFolderNameChange,
                initialFolderName = initialFolderName
            )

            Spacer(modifier = Modifier.height(26.dp))
            AddPromptSectionLabel(text = "FILE TYPE")
            Spacer(modifier = Modifier.height(12.dp))
            AddPromptFileTypeChips(
                fileTypes = fileTypes,
                selectedFileType = selectedFileType,
                isExpanded = isFileTypeExpanded,
                onFileTypeClick = { fileType ->
                    selectedFileType = fileType
                    onFileTypeClick(fileType)
                },
                onMoreClick = { isFileTypeExpanded = !isFileTypeExpanded }
            )

            Spacer(modifier = Modifier.height(28.dp))
            AddPromptSectionLabel(text = "YOUR PROMPT")
            Spacer(modifier = Modifier.height(12.dp))
            AddPromptPromptField(
                onPromptTextChange = onPromptTextChange,
                initialPromptText = initialPromptText
            )

            Spacer(modifier = Modifier.height(14.dp))
            AddPromptSaveButton(onClick = onSavePromptClick)
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AddPromptTopBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(AddPromptScreenHeader),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "New Prompt",
            color = Color.White,
            style = AddPromptTypography.title,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AddPromptSectionLabel(text: String) {
    Text(
        text = text,
        color = AddPromptScreenMuted,
        style = AddPromptTypography.sectionLabel
    )
}

@Composable
private fun AddPromptFolderNameField(
    onFolderNameChange: (String) -> Unit,
    initialFolderName: String = ""
) {
    val folderNameHint = "Enter folder name..."
    var folderName by remember { mutableStateOf(initialFolderName) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = AddPromptScreenPanelSoft),
        border = BorderStroke(1.dp, AddPromptScreenStrokeSoft)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_folder),
                    contentDescription = "Folder name",
                    tint = AddPromptScreenChipActive,
                    modifier = Modifier.size(20.dp)
                )
            }
            BasicTextField(
                value = folderName,
                onValueChange = {
                    folderName = it
                    onFolderNameChange(it)
                },
                singleLine = true,
                textStyle = AddPromptTypography.fieldText.copy(color = Color.White),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (folderName.isEmpty()) {
                            Text(
                                text = folderNameHint,
                                color = AddPromptScreenHint,
                                style = AddPromptTypography.fieldText
                            )
                        }
                        innerTextField()
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun AddPromptFileTypeChips(
    fileTypes: List<PromptCategory>,
    selectedFileType: PromptCategory?,
    isExpanded: Boolean,
    onFileTypeClick: (PromptCategory) -> Unit,
    onMoreClick: () -> Unit,
) {
    val collapsedVisibleCount = 3
    val visibleFileTypes = if (isExpanded) fileTypes else fileTypes.take(collapsedVisibleCount)
    val shouldShowMoreChip = !isExpanded && visibleFileTypes.size < fileTypes.size

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        visibleFileTypes.forEach { fileType ->
            AddPromptChip(
                text = fileType.toUiLabel(),
                selected = fileType == selectedFileType,
                onClick = { onFileTypeClick(fileType) }
            )
        }

        if (shouldShowMoreChip) {
            AddPromptChip(
                text = "More",
                selected = false,
                trailingSymbol = "⌄",
                onClick = onMoreClick
            )
        }
    }
}

private fun PromptCategory.toUiLabel(): String {
    return name
        .removeSuffix("Type")
        .replace(Regex("([a-z])([A-Z])"), "$1 $2")
        .trim()
}

@Composable
private fun AddPromptChip(
    text: String,
    selected: Boolean,
    trailingSymbol: String? = null,
    onClick: () -> Unit
) {
    val chipContainer = if (selected) AddPromptScreenChipActive else AddPromptScreenChipInactive
    val chipTextColor = if (selected) Color.Black else AddPromptScreenMuted
    val chipBorder = if (selected) Color.Transparent else AddPromptScreenStroke

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        color = chipContainer,
        border = BorderStroke(1.dp, chipBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = text,
                color = chipTextColor,
                style = AddPromptTypography.chipText
            )
            if (trailingSymbol != null) {
                Text(
                    text = trailingSymbol,
                    color = chipTextColor,
                    style = AddPromptTypography.chipText,
                    modifier = Modifier.padding(bottom = 1.dp)
                )
            }
        }
    }
}

@Composable
private fun AddPromptPromptField(
    onPromptTextChange: (String) -> Unit,
    initialPromptText: String = ""
) {
    val promptHint = remember {
        "e.g., Move photos with sunsets to the /Vacation folder, and organize technical documents into subfolders based on project names mentioned in the first"
    }
    var promptText by remember { mutableStateOf(initialPromptText) }
    val promptFieldScrollState = rememberScrollState()
    val promptFieldInteractionSource = remember { MutableInteractionSource() }
    val isPromptFieldFocused by promptFieldInteractionSource.collectIsFocusedAsState()
    val hasOverflow by remember {
        derivedStateOf { promptFieldScrollState.maxValue > 0 }
    }
    val isScrollbarActive by remember {
        derivedStateOf {
            hasOverflow && (isPromptFieldFocused || promptFieldScrollState.isScrollInProgress)
        }
    }
    val scrollbarTrackAlpha by animateFloatAsState(
        targetValue = if (isScrollbarActive) 0.72f else 0.34f,
        label = "addPromptScrollbarTrackAlpha"
    )
    val scrollbarThumbAlpha by animateFloatAsState(
        targetValue = if (isScrollbarActive) 0.92f else 0.56f,
        label = "addPromptScrollbarThumbAlpha"
    )
    val trackHeightDp = 160.dp
    val thumbHeightDp = if (hasOverflow) 54.dp else 60.dp
    val scrollProgress by remember {
        derivedStateOf {
            if (promptFieldScrollState.maxValue > 0) {
                promptFieldScrollState.value.toFloat() / promptFieldScrollState.maxValue.toFloat()
            } else {
                0f
            }
        }
    }
    val thumbOffsetDp = ((160f - if (hasOverflow) 54f else 60f) * scrollProgress).dp

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = AddPromptScreenPanelSoft),
        border = BorderStroke(1.dp, AddPromptScreenStrokeSoft)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 17.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                BasicTextField(
                    value = promptText,
                    onValueChange = {
                        promptText = it
                        onPromptTextChange(it)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(end = 14.dp)
                        .verticalScroll(promptFieldScrollState),
                    interactionSource = promptFieldInteractionSource,
                    textStyle = AddPromptTypography.promptHint.copy(color = Color.White),
                    decorationBox = { innerTextField ->
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (promptText.isEmpty()) {
                                Text(
                                    text = promptHint,
                                    color = AddPromptScreenHint,
                                    style = AddPromptTypography.promptHint,
                                    lineHeight = 22.sp
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .padding(vertical = 8.dp)
                        .size(width = 3.dp, height = trackHeightDp)
                        .clip(RoundedCornerShape(50))
                        .background(AddPromptScreenStrokeSoft.copy(alpha = scrollbarTrackAlpha))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(thumbHeightDp)
                            .padding(top = thumbOffsetDp)
                            .clip(RoundedCornerShape(50))
                            .background(AddPromptScreenMuted.copy(alpha = scrollbarThumbAlpha))
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_history),
                        contentDescription = "Attach prompt",
                        tint = AddPromptScreenMuted,
                        modifier = Modifier.size(17.dp)
                    )
                }
                Text(
                    text = "${promptText.length} / $AddPromptMaxCharCount chars",
                    textAlign = TextAlign.End,
                    color = AddPromptScreenMutedSoft,
                    style = AddPromptTypography.characterCount
                )
            }
        }
    }
}

@Composable
private fun AddPromptSaveButton(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = AddPromptScreenButton,
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 11.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Save Prompt",
                color = Color.Black,
                style = AddPromptTypography.saveButtonText
            )
        }
    }
}

@Preview(
    name = "Add Prompt",
    showBackground = true,
    backgroundColor = 0xFF07172A,
    showSystemUi = true
)
@Composable
private fun AddPromptScreenUiPreview() {
    OrgFTheme(darkTheme = true, dynamicColor = false) {
        AddPromptScreenContent(
            onFileTypeClick = {},
            onSavePromptClick = {},
            onFolderNameChange = {},
            onPromptTextChange = {},
            initialFolderName = "Vacation 2026",
            initialPromptText = "Move travel photos into the Vacation folder, keep receipts in Finance, and sort project documents by client name.",
            initialFileTypeExpanded = true
        )
    }
}
