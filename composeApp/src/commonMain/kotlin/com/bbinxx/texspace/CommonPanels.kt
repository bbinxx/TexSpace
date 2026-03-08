package com.bbinxx.texspace

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.OffsetMapping

class LatexVisualTransformation : VisualTransformation {
    override fun filter(text: androidx.compose.ui.text.AnnotatedString): TransformedText {
        val originalText = text.text
        val annotatedString = buildAnnotatedString {
            append(originalText)
            
            // Overleaf-like highlighting
            val commandRegex = Regex("\\\\[a-zA-Z]+")
            val braceRegex = Regex("[{}]")
            val commentRegex = Regex("%.*")
            val keywordRegex = Regex("\\\\(begin|end|documentclass|usepackage|document)")

            commandRegex.findAll(originalText).forEach { match ->
                addStyle(SpanStyle(color = Color(0xFF569CD6)), match.range.first, match.range.last + 1)
            }
            keywordRegex.findAll(originalText).forEach { match ->
                addStyle(SpanStyle(color = Color(0xFFC586C0), fontWeight = FontWeight.Bold), match.range.first, match.range.last + 1)
            }
            braceRegex.findAll(originalText).forEach { match ->
                addStyle(SpanStyle(color = Color(0xFFD4D4D4)), match.range.first, match.range.last + 1)
            }
            commentRegex.findAll(originalText).forEach { match ->
                addStyle(SpanStyle(color = Color(0xFF6A9955)), match.range.first, match.range.last + 1)
            }
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
        color = Color(0xFF1E1E1E) // VS Code / Overleaf Dark Editor Background
    ) {
        val lineCount = source.lines().size.coerceAtLeast(1)
        Row(modifier = Modifier.fillMaxSize()) {
            // Line numbers
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .fillMaxHeight()
                    .background(Color(0xFF1E1E1E))
                    .padding(end = 8.dp)
            ) {
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.padding(top = 16.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    items(lineCount) { index ->
                        Text(
                            text = (index + 1).toString(),
                            style = TextStyle(
                                color = Color(0xFF858585),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.End
                            ),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }

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
                modifier = Modifier.padding(top = 16.dp, start = 8.dp, end = 16.dp).weight(1f).fillMaxHeight()
            )
        }
    }
}

@Composable
fun FallbackPdfPreviewPanel(
    pdfBase64: String?,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize().background(Color(0xFF525659)), contentAlignment = Alignment.Center) {
        if (pdfBase64 != null) {
            Surface(
                modifier = Modifier.padding(32.dp).aspectRatio(0.707f).fillMaxHeight(),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("PDF Preview Available\n(Rendering via PDFBox)", color = Color.Black)
                }
            }
        } else {
            Text("Click 'Recompile' to see preview", color = Color.LightGray)
        }
    }
}
