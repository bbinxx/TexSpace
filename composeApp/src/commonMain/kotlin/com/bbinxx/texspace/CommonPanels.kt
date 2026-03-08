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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.OffsetMapping

class LatexVisualTransformation : VisualTransformation {
    override fun filter(text: androidx.compose.ui.text.AnnotatedString): TransformedText {
        val originalText = text.text
        val annotatedString = buildAnnotatedString {
            append(originalText)
            
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
                        )
                    }
                }
            }

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
        }
    }
}

@Composable
fun FallbackPdfPreviewPanel(
    pdfBase64: String?,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (pdfBase64 != null) {
            Text(
                "PDF generated successfully.\nSize: ${pdfBase64.length} bytes\n(Platform-specific viewer required)",
                color = androidx.compose.ui.graphics.Color.Black
            )
        } else {
            Text("No PDF to display.", color = androidx.compose.ui.graphics.Color.Gray)
        }
    }
}
