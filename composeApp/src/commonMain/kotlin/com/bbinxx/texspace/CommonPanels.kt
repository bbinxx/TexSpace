package com.bbinxx.texspace

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.background
<<<<<<< HEAD
=======
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
>>>>>>> dev
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
<<<<<<< HEAD
=======
import androidx.compose.ui.graphics.Color
>>>>>>> dev
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
<<<<<<< HEAD
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth

import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
=======
import androidx.compose.ui.unit.sp

import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
>>>>>>> dev
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.OffsetMapping

class LatexVisualTransformation : VisualTransformation {
    override fun filter(text: androidx.compose.ui.text.AnnotatedString): TransformedText {
        val originalText = text.text
        val annotatedString = buildAnnotatedString {
            append(originalText)
            
<<<<<<< HEAD
            // basic latex highlighting
            val commandRegex = Regex("\\\\[a-zA-Z]+")
            val braceRegex = Regex("[{}]")
            val commentRegex = Regex("%.*")

            commandRegex.findAll(originalText).forEach { match ->
                addStyle(SpanStyle(color = androidx.compose.ui.graphics.Color(0xFF007ACC)), match.range.first, match.range.last + 1)
            }
            braceRegex.findAll(originalText).forEach { match ->
                addStyle(SpanStyle(color = androidx.compose.ui.graphics.Color(0xFFD32F2F)), match.range.first, match.range.last + 1)
            }
            commentRegex.findAll(originalText).forEach { match ->
                addStyle(SpanStyle(color = androidx.compose.ui.graphics.Color(0xFF388E3C)), match.range.first, match.range.last + 1)
            }
=======
            // Comprehensive Syntax Highlighting
            val commandRegex = Regex("\\\\[a-zA-Z*]+")
            val multiLineCommentRegex = Regex("%.*")
            val mathModeRegex = Regex("\\$[^$]*\\$|\\$\\$[^$]*\\$\\$|\\\\\\[[\\s\\S]*?\\\\\\]|\\\\\\([\\s\\S]*?\\\\\\)")
            val environmentRegex = Regex("\\\\(begin|end)\\{[^}]*\\}")
            val braceRegex = Regex("[{}]")
            val bracketRegex = Regex("[\\[\\]]")
            val keywordRegex = Regex("\\\\(documentclass|usepackage|document|input|include|title|author|date|maketitle|section|subsection|subsubsection|paragraph|subparagraph|tableofcontents)")

            // Order matters: simpler patterns later to avoid overriding complex ones if using addStyle overlapping
            
            // Braces & Brackets - Gray
            braceRegex.findAll(originalText).forEach { addStyle(SpanStyle(color = Color(0xFFD4D4D4)), it.range.first, it.range.last + 1) }
            bracketRegex.findAll(originalText).forEach { addStyle(SpanStyle(color = Color(0xFFD4D4D4)), it.range.first, it.range.last + 1) }

            // Commands - Blue
            commandRegex.findAll(originalText).forEach { addStyle(SpanStyle(color = Color(0xFF569CD6)), it.range.first, it.range.last + 1) }
            
            // Keywords / Structure - Purple
            keywordRegex.findAll(originalText).forEach { addStyle(SpanStyle(color = Color(0xFFC586C0), fontWeight = FontWeight.Bold), it.range.first, it.range.last + 1) }
            
            // Environments - Teal
            environmentRegex.findAll(originalText).forEach { addStyle(SpanStyle(color = Color(0xFF4EC9B0), fontWeight = FontWeight.Bold), it.range.first, it.range.last + 1) }
            
            // Math Mode - Yellow/Gold
            mathModeRegex.findAll(originalText).forEach { addStyle(SpanStyle(color = Color(0xFFDCDCAA)), it.range.first, it.range.last + 1) }
            
            // Comments - Green (Highest priority, apply last)
            multiLineCommentRegex.findAll(originalText).forEach { addStyle(SpanStyle(color = Color(0xFF6A9955), fontStyle = androidx.compose.ui.text.font.FontStyle.Italic), it.range.first, it.range.last + 1) }
>>>>>>> dev
        }
        return TransformedText(annotatedString, OffsetMapping.Identity)
    }
}

@Composable
fun TextFieldEditorPanel(
    source: String,
    onSourceChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
<<<<<<< HEAD
        color = MaterialTheme.colorScheme.background
    ) {
        val lineCount = source.lines().size.coerceAtLeast(1)
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(end = 8.dp)
            ) {
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.padding(start = 8.dp, top = 16.dp, bottom = 16.dp)
                ) {
                    items(lineCount) { index ->
                        Text(
                            text = (index + 1).toString(),
                            style = TextStyle(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontFamily = FontFamily.Monospace,
                                fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                textAlign = androidx.compose.ui.text.style.TextAlign.End
                            ),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
=======
        color = Color(0xFF1E1E1E)
    ) {
        val lines = source.lines()
        val lineCount = lines.size.coerceAtLeast(1)
        val scrollState = rememberScrollState()

        Row(modifier = Modifier.fillMaxSize()) {
            // Line numbers gutter
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .fillMaxHeight()
                    .background(Color(0xFF1E1E1E))
                    .padding(end = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(top = 16.dp).verticalScroll(scrollState),
                    horizontalAlignment = Alignment.End
                ) {
                    repeat(lineCount) { index ->
                        Text(
                            text = (index + 1).toString(),
                            style = TextStyle(
                                color = Color(0xFF858585),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.End
                            ),
                            modifier = Modifier.fillMaxWidth().height(20.dp).padding(horizontal = 4.dp)
>>>>>>> dev
                        )
                    }
                }
            }

<<<<<<< HEAD
            BasicTextField(
                value = source,
                onValueChange = onSourceChange,
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontFamily = FontFamily.Monospace,
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                visualTransformation = LatexVisualTransformation(),
                modifier = Modifier.padding(16.dp).weight(1f).fillMaxHeight()
            )
=======
            // Editor Area
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                BasicTextField(
                    value = source,
                    onValueChange = onSourceChange,
                    textStyle = TextStyle(
                        color = Color(0xFFD4D4D4),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    ),
                    cursorBrush = SolidColor(Color.White),
                    visualTransformation = LatexVisualTransformation(),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 16.dp, start = 8.dp, end = 16.dp)
                        .verticalScroll(scrollState)
                )
            }
>>>>>>> dev
        }
    }
}

@Composable
fun FallbackPdfPreviewPanel(
    pdfBase64: String?,
    modifier: Modifier = Modifier
) {
<<<<<<< HEAD
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (pdfBase64 != null) {
            Text(
                "PDF generated successfully.\nSize: ${pdfBase64.length} bytes\n(Platform-specific viewer required)",
                color = androidx.compose.ui.graphics.Color.Black
            )
        } else {
            Text("No PDF to display.", color = androidx.compose.ui.graphics.Color.Gray)
=======
    Box(modifier = modifier.fillMaxSize().background(Color(0xFF525659)), contentAlignment = Alignment.Center) {
        if (!pdfBase64.isNullOrEmpty()) {
            Surface(
                modifier = Modifier.padding(32.dp).aspectRatio(0.707f).fillMaxHeight(),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("PDF Preview Available\n(Rendering via Native Engine)", color = Color.Black)
                }
            }
        } else {
            Text("Click 'Recompile' to see preview", color = Color.LightGray)
>>>>>>> dev
        }
    }
}
