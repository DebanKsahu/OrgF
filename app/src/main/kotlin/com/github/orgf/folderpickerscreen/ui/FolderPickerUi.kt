package com.github.orgf.folderpickerscreen.ui

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.orgf.R
import com.github.orgf.utils.ui.FolderPickerUiTokens
import com.github.orgf.utils.ui.OrgFTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun FolderPickerUi(
    onSuccessfulWorkspaceSelection: () -> Unit
) {
    val folderPickerViewModel: FolderPickerViewModel = koinViewModel()
    val appContext: Context = LocalContext.current

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            folderPickerViewModel.saveWorkspaceUri(appContext = appContext, workspaceUri = it)
            folderPickerViewModel.startRequiredBackgroundService(
                appContext = appContext,
                workspaceUri = it
            )
        }
    }

    OrgFTheme(darkTheme = true, dynamicColor = false) {
        FolderPickerUiContent(
            onSelectWorkspaceClick = {
                folderPickerLauncher.launch(null)
                onSuccessfulWorkspaceSelection()
            }
        )
    }
}

@Composable
fun FolderPickerUiContent(
    modifier: Modifier = Modifier,
    onSelectWorkspaceClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        FolderPickerUiTokens.screenTop,
                        FolderPickerUiTokens.screenBottom
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = FolderPickerUiTokens.screenHorizontalPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(FolderPickerUiTokens.topSectionSpacing))
            OrgFBrandHeader()
            Spacer(modifier = Modifier.height(FolderPickerUiTokens.headerToButtonSpacing))
            SelectWorkspaceButton(onClick = onSelectWorkspaceClick)
            Spacer(modifier = Modifier.height(FolderPickerUiTokens.buttonToPrivacySpacing))
            PrivacyInfoSection()
            Spacer(modifier = Modifier.height(FolderPickerUiTokens.bottomSectionSpacing))
        }
    }
}

@Composable
private fun OrgFBrandHeader() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        OrgFLogoCard()
        Spacer(modifier = Modifier.height(FolderPickerUiTokens.logoToTitleSpacing))

        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color.White)) { append("Org") }
                withStyle(style = SpanStyle(color = FolderPickerUiTokens.privacyAccent)) { append("F") }
            },
            style = FolderPickerUiTokens.brandTitleStyle
        )

        Spacer(modifier = Modifier.height(FolderPickerUiTokens.titleToSubtitleSpacing))

        Text(
            text = "Intelligent Local Storage",
            color = FolderPickerUiTokens.textMuted,
            style = FolderPickerUiTokens.brandSubtitleStyle,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun OrgFLogoCard() {
    Box(modifier = Modifier.size(190.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(174.dp)
                .shadow(
                    elevation = FolderPickerUiTokens.boldDepthStyle.logoGlowElevation,
                    shape = RoundedCornerShape(38.dp),
                    ambientColor = FolderPickerUiTokens.privacyAccent.copy(alpha = FolderPickerUiTokens.boldDepthStyle.logoGlowAmbientAlpha),
                    spotColor = FolderPickerUiTokens.privacyAccent.copy(alpha = FolderPickerUiTokens.boldDepthStyle.logoGlowSpotAlpha)
                )
        )

        Box(
            modifier = Modifier
                .size(150.dp)
                .shadow(
                    elevation = FolderPickerUiTokens.boldDepthStyle.logoCardElevation,
                    shape = RoundedCornerShape(30.dp),
                    ambientColor = FolderPickerUiTokens.deepShadow.copy(alpha = FolderPickerUiTokens.boldDepthStyle.logoCardShadowAlpha),
                    spotColor = FolderPickerUiTokens.deepShadow.copy(alpha = FolderPickerUiTokens.boldDepthStyle.logoCardShadowAlpha)
                )
                .clip(RoundedCornerShape(30.dp))
                .background(FolderPickerUiTokens.logoCardColor)
                .border(1.dp, FolderPickerUiTokens.surfaceHighlight, RoundedCornerShape(30.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_lock),
                contentDescription = "OrgF logo",
                tint = FolderPickerUiTokens.privacyAccent,
                modifier = Modifier.size(72.dp)
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 8.dp, y = 8.dp)
                .size(48.dp)
                .shadow(
                    elevation = FolderPickerUiTokens.boldDepthStyle.badgeElevation,
                    shape = CircleShape,
                    ambientColor = FolderPickerUiTokens.deepShadow.copy(alpha = FolderPickerUiTokens.boldDepthStyle.badgeAmbientAlpha),
                    spotColor = FolderPickerUiTokens.privacyAccent.copy(alpha = FolderPickerUiTokens.boldDepthStyle.badgeSpotAlpha)
                )
                .clip(CircleShape)
                .background(FolderPickerUiTokens.privacyAccent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_folder),
                contentDescription = "Folder picker badge",
                tint = FolderPickerUiTokens.screenBottom,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun SelectWorkspaceButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(74.dp)
            .shadow(
                elevation = FolderPickerUiTokens.boldDepthStyle.buttonElevation,
                shape = RoundedCornerShape(22.dp),
                ambientColor = FolderPickerUiTokens.deepShadow.copy(alpha = FolderPickerUiTokens.boldDepthStyle.buttonAmbientAlpha),
                spotColor = FolderPickerUiTokens.privacyAccent.copy(alpha = FolderPickerUiTokens.boldDepthStyle.buttonSpotAlpha)
            ),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(
            1.dp,
            Color.White.copy(alpha = FolderPickerUiTokens.boldDepthStyle.buttonBorderAlpha)
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = FolderPickerUiTokens.privacyAccent,
            contentColor = FolderPickerUiTokens.primaryButtonText
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_folder),
                contentDescription = "Select workspace folder",
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "Select Workspace Folder",
                style = FolderPickerUiTokens.ctaTextStyle
            )
        }
    }
}

@Composable
private fun PrivacyInfoSection() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier
                .shadow(
                    elevation = FolderPickerUiTokens.boldDepthStyle.privacyChipElevation,
                    shape = RoundedCornerShape(20.dp),
                    ambientColor = FolderPickerUiTokens.deepShadow.copy(alpha = FolderPickerUiTokens.boldDepthStyle.privacyChipAmbientAlpha),
                    spotColor = FolderPickerUiTokens.privacyAccent.copy(alpha = FolderPickerUiTokens.boldDepthStyle.privacyChipSpotAlpha)
                )
                .clip(RoundedCornerShape(20.dp))
                .background(FolderPickerUiTokens.privacyAccent.copy(alpha = FolderPickerUiTokens.boldDepthStyle.privacyChipBackgroundAlpha))
                .border(
                    1.dp,
                    FolderPickerUiTokens.privacyAccent.copy(alpha = FolderPickerUiTokens.boldDepthStyle.privacyChipBorderAlpha),
                    RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_lock),
                contentDescription = "Privacy guaranteed",
                tint = FolderPickerUiTokens.privacyAccent,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "PRIVACY GUARANTEED",
                color = FolderPickerUiTokens.privacyAccent,
                style = FolderPickerUiTokens.privacyLabelStyle
            )
        }

        Spacer(modifier = Modifier.height(FolderPickerUiTokens.privacyLabelToBodySpacing))

        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = FolderPickerUiTokens.textMuted)) {
                    append("All file processing and metadata ")
                    append("generation happens ")
                }
                withStyle(
                    style = SpanStyle(
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                ) {
                    append("locally on your ")
                    append("device")
                }
                withStyle(style = SpanStyle(color = FolderPickerUiTokens.textMuted)) {
                    append(". Your data never leaves your ")
                    append("control.")
                }
            },
            modifier = Modifier
                .widthIn(max = FolderPickerUiTokens.privacyBodyMaxWidth)
                .padding(horizontal = 12.dp),
            textAlign = TextAlign.Center,
            style = FolderPickerUiTokens.privacyBodyStyle
        )
    }
}

@Preview(
    name = "Folder Picker - Bold",
    showBackground = true,
    backgroundColor = 0xFF030B14,
    showSystemUi = true
)
@Composable
private fun FolderPickerUiPreviewBold() {
    OrgFTheme(darkTheme = true, dynamicColor = false) {
        FolderPickerUiContent()
    }
}