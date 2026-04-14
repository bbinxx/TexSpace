package com.bbinxx.texspace

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange

class LatexVisualTransformation : VisualTransformation {
    override fun filter(text: androidx.compose.ui.text.AnnotatedString): TransformedText {
        val originalText = text.text
        val annotatedString = buildAnnotatedString {
            append(originalText)
            
            val commandRegex = Regex("\\\\[a-zA-Z*]+")
            val multiLineCommentRegex = Regex("%.*")
            val mathModeRegex = Regex("\\$[^$]*\\$|\\$\\$[^$]*\\$\\$|\\\\\\[[\\s\\S]*?\\\\\\]|\\\\\\([\\s\\S]*?\\\\\\)")
            val environmentRegex = Regex("\\\\(begin|end)\\{[^}]*\\}")
            val braceRegex = Regex("[{}]")
            val bracketRegex = Regex("[\\[\\]]")
            val keywordRegex = Regex("\\\\(documentclass|usepackage|document|input|include|title|author|date|maketitle|section|subsection|subsubsection|paragraph|subparagraph|tableofcontents)")

            braceRegex.findAll(originalText).forEach { addStyle(SpanStyle(color = Color(0xFFD4D4D4)), it.range.first, it.range.last + 1) }
            bracketRegex.findAll(originalText).forEach { addStyle(SpanStyle(color = Color(0xFFD4D4D4)), it.range.first, it.range.last + 1) }
            commandRegex.findAll(originalText).forEach { addStyle(SpanStyle(color = Color(0xFF569CD6)), it.range.first, it.range.last + 1) }
            keywordRegex.findAll(originalText).forEach { addStyle(SpanStyle(color = Color(0xFFC586C0), fontWeight = FontWeight.Bold), it.range.first, it.range.last + 1) }
            environmentRegex.findAll(originalText).forEach { addStyle(SpanStyle(color = Color(0xFF4EC9B0), fontWeight = FontWeight.Bold), it.range.first, it.range.last + 1) }
            mathModeRegex.findAll(originalText).forEach { addStyle(SpanStyle(color = Color(0xFFDCDCAA)), it.range.first, it.range.last + 1) }
            multiLineCommentRegex.findAll(originalText).forEach { addStyle(SpanStyle(color = Color(0xFF6A9955), fontStyle = androidx.compose.ui.text.font.FontStyle.Italic), it.range.first, it.range.last + 1) }
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
    var textFieldValue by remember(source) { 
        mutableStateOf(TextFieldValue(text = source, selection = TextRange(source.length))) 
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFF0F111A) // Match App Theme
    ) {
        val lines = textFieldValue.text.lines()
        val lineCount = lines.size.coerceAtLeast(1)
        val scrollState = rememberScrollState()

        Row(modifier = Modifier.fillMaxSize()) {
            // Line numbers gutter
            Box(
                modifier = Modifier
                    .width(44.dp)
                    .fillMaxHeight()
                    .background(Color(0xFF0F111A))
                    .padding(end = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(top = 16.dp).verticalScroll(scrollState),
                    horizontalAlignment = Alignment.End
                ) {
                    repeat(lineCount) { index ->
                        Text(
                            text = (index + 1).toString(),
                            style = TextStyle(
                                color = Color(0xFF3B3D4D),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.End
                            ),
                            modifier = Modifier.fillMaxWidth().height(20.dp)
                        )
                    }
                }
            }

            // Editor Area
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                BasicTextField(
                    value = textFieldValue,
                    onValueChange = { newValue ->
                        // IDE Features: Auto-pairing
                        val processedValue = handleAutoPairing(newValue, textFieldValue)
                        textFieldValue = processedValue
                        if (processedValue.text != source) {
                            onSourceChange(processedValue.text)
                        }
                    },
                    textStyle = TextStyle(
                        color = Color(0xFFE0E0E0),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    ),
                    cursorBrush = SolidColor(Color(0xFF64B5F6)),
                    visualTransformation = LatexVisualTransformation(),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 16.dp, start = 4.dp, end = 16.dp)
                        .verticalScroll(scrollState)
                )
            }
        }
    }
}

private fun handleAutoPairing(newValue: TextFieldValue, oldValue: TextFieldValue): TextFieldValue {
    if (newValue.text.length <= oldValue.text.length) return newValue
    
    val cursor = newValue.selection.start
    if (cursor == 0) return newValue
    
    val lastChar = newValue.text[cursor - 1]
    val pairs = mapOf('{' to '}', '[' to ']', '(' to ')', '$' to '$')
    
    if (pairs.containsKey(lastChar)) {
        val pair = pairs[lastChar]!!
        val newText = StringBuilder(newValue.text).insert(cursor, pair).toString()
        return newValue.copy(
            text = newText,
            selection = TextRange(cursor)
        )
    }
    return newValue
}

@Composable
fun FallbackPdfPreviewPanel(
    pdfBase64: String?,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize().background(Color(0xFF1A1C2E)), contentAlignment = Alignment.Center) {
        if (!pdfBase64.isNullOrEmpty()) {
            Surface(
                modifier = Modifier.padding(32.dp).aspectRatio(0.707f).fillMaxHeight(),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("PDF Preview Available", color = Color.Black)
                }
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Render Preview", fontWeight = FontWeight.Bold, color = Color.Gray)
                Text("Click Compile to start", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}
