package com.bbinxx.texspace

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.*
import androidx.compose.ui.graphics.Color
>>>>>>> dev
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*

// ─── IDE THEME TOKENS ────────────────────────────────────────────────────────
private val EditorBg        = Color(0xFF0D0F1A)
private val GutterBg        = Color(0xFF0B0D16)
private val GutterText      = Color(0xFF3E4260)
private val GutterActiveLine = Color(0xFF6272A4)
private val EditorFg        = Color(0xFFCDD6F4)    // Catppuccin text
private val EditorCursor    = Color(0xFF89B4FA)
private val ActiveLineBg    = Color(0xFF1A1D2E)
private val SelectionBg     = Color(0xFF313569)

// Syntax Colors (Dracula-inspired palette)
private val ColCommand      = Color(0xFF8BE9FD)   // Cyan  — \commands
private val ColKeyword      = Color(0xFFFF79C6)   // Pink  — \documentclass, \usepackage…
private val ColEnv          = Color(0xFF50FA7B)   // Green — \begin{} \end{}
private val ColMath         = Color(0xFFF1FA8C)   // Yellow — math mode
private val ColComment      = Color(0xFF6272A4)   // Gray-blue — %comments
private val ColBrace        = Color(0xFFFFB86C)   // Orange — {}
private val ColBracket      = Color(0xFFBD93F9)   // Purple — []
private val ColString       = Color(0xFFF8F8F2)   // White — normal text
private val ColNumber       = Color(0xFFFF6E6E)   // Red — numbers in args

private val StatusBarBg     = Color(0xFF070910)
private val FindBarBg       = Color(0xFF161829)

// ─── SYNTAX HIGHLIGHTING ENGINE ───────────────────────────────────────────────
class LatexVisualTransformation : VisualTransformation {
    private val commandRegex  = Regex("""\\[a-zA-Z*@]+""")
    private val keywordRegex  = Regex("""\\(documentclass|usepackage|newcommand|renewcommand|input|include|maketitle|tableofcontents|bibliography|bibliographystyle|title|author|date|label|ref|cite|footnote|caption|textbf|textit|emph|texttt|underline|url)(?![a-zA-Z])""")
    private val envRegex      = Regex("""\\(?:begin|end)\{[^}]*\}""")
    private val mathInlineRegex = Regex("""\$[^$\n]*\$""")
    private val mathDisplayRegex = Regex("""\$\$[\s\S]*?\$\$""")
    private val commentRegex  = Regex("""(?<!\\)%[^\n]*""")
    private val braceRegex    = Regex("""[{}]""")
    private val bracketRegex  = Regex("""[\[\]]""")
    private val numberArgRegex = Regex("""(?<=\{)\d+(?=\})""")
    private val optArgRegex   = Regex("""(?<=\[)\d+(?=\])""")

    override fun filter(text: AnnotatedString): TransformedText {
        val src = text.text
        val annotated = buildAnnotatedString {
            append(src)

            // Order matters — later styles override earlier ones for overlapping ranges
            // 1. Base braces & brackets
            braceRegex.findAll(src).forEach { addStyle(SpanStyle(color = ColBrace, fontWeight = FontWeight.Bold), it.range.first, it.range.last + 1) }
            bracketRegex.findAll(src).forEach { addStyle(SpanStyle(color = ColBracket), it.range.first, it.range.last + 1) }
            // 2. Numbers inside args
            numberArgRegex.findAll(src).forEach { addStyle(SpanStyle(color = ColNumber), it.range.first, it.range.last + 1) }
            optArgRegex.findAll(src).forEach { addStyle(SpanStyle(color = ColNumber), it.range.first, it.range.last + 1) }
            // 3. Generic commands
            commandRegex.findAll(src).forEach { addStyle(SpanStyle(color = ColCommand), it.range.first, it.range.last + 1) }
            // 4. Keyword commands (override generic)
            keywordRegex.findAll(src).forEach { addStyle(SpanStyle(color = ColKeyword, fontWeight = FontWeight.Bold), it.range.first, it.range.last + 1) }
            // 5. Environments (override keyword for \begin\end)
            envRegex.findAll(src).forEach { addStyle(SpanStyle(color = ColEnv, fontWeight = FontWeight.SemiBold), it.range.first, it.range.last + 1) }
            // 6. Inline math
            mathInlineRegex.findAll(src).forEach { addStyle(SpanStyle(color = ColMath), it.range.first, it.range.last + 1) }
            // 7. Display math (override inline)
            mathDisplayRegex.findAll(src).forEach { addStyle(SpanStyle(color = ColMath, fontStyle = FontStyle.Italic), it.range.first, it.range.last + 1) }
            // 8. Comments (must be last to override everything)
            commentRegex.findAll(src).forEach { addStyle(SpanStyle(color = ColComment, fontStyle = FontStyle.Italic), it.range.first, it.range.last + 1) }
        }
        return TransformedText(annotated, OffsetMapping.Identity)
    }
}

// ─── AUTO-PAIRING ENGINE ─────────────────────────────────────────────────────
private val OPEN_CLOSE = mapOf('{' to '}', '[' to ']', '(' to ')', '$' to '$')
private val CLOSE_CHARS = setOf('}', ']', ')', '$')

private fun handleSmartInput(new: TextFieldValue, old: TextFieldValue): TextFieldValue {
    val cur = new.selection.start
    if (!new.selection.collapsed) return new   // Don't interfere with selections
    if (new.text.length < old.text.length) {
        // Deletion: if we deleted an open brace followed by matching close, remove both
        if (old.text.length == new.text.length + 1) {
            val deletedChar = old.text.getOrNull(cur)
            val nextChar = new.text.getOrNull(cur)
            if (deletedChar != null && OPEN_CLOSE[deletedChar] == nextChar) {
                val newText = new.text.removeRange(cur, cur + 1)
                return new.copy(text = newText, selection = TextRange(cur))
            }
        }
        return new
    }

    if (new.text.length == old.text.length + 1) {
        val typedChar = new.text.getOrNull(cur - 1) ?: return new
        // Skip-over matching close bracket if it already exists
        if (typedChar in CLOSE_CHARS && old.text.getOrNull(cur - 1) == typedChar) {
            val stripped = new.text.removeRange(cur - 1, cur)
            return new.copy(text = stripped, selection = TextRange(cur))
        }
        // Insert matching close bracket
        val match = OPEN_CLOSE[typedChar]
        if (match != null) {
            val newText = StringBuilder(new.text).insert(cur, match).toString()
            return new.copy(text = newText, selection = TextRange(cur))
        }
    }
    return new
}

// ─── SNIPPET ENGINE ──────────────────────────────────────────────────────────
data class Snippet(val trigger: String, val body: String, val cursorOffset: Int = 0)

private val SNIPPETS = listOf(
    Snippet("doc",  "\\documentclass[12pt]{article}\n\n\\begin{document}\n\n\\end{document}", 0),
    Snippet("pkg",  "\\usepackage{}",        12),
    Snippet("sec",  "\\section{}",            9),
    Snippet("ssec", "\\subsection{}",         13),
    Snippet("sssec","\\subsubsection{}",       16),
    Snippet("eq",   "\\begin{equation}\n\t\n\\end{equation}", 18),
    Snippet("al",   "\\begin{align}\n\t\n\\end{align}", 15),
    Snippet("fig",  "\\begin{figure}[h]\n\t\\centering\n\t\\includegraphics[width=0.8\\linewidth]{}\n\t\\caption{}\n\t\\label{fig:}\n\\end{figure}", 0),
    Snippet("tab",  "\\begin{tabular}{|c|c|}\n\t\\hline\n\t & \\\\\n\t\\hline\n\\end{tabular}", 0),
    Snippet("bf",   "\\textbf{}",  8),
    Snippet("it",   "\\textit{}",  8),
    Snippet("tt",   "\\texttt{}",  8),
    Snippet("item", "\\begin{itemize}\n\t\\item \n\\end{itemize}", 23),
    Snippet("enum", "\\begin{enumerate}\n\t\\item \n\\end{enumerate}", 26),
    Snippet("beg",  "\\begin{}\n\n\\end{}", 7),
)

private fun tryExpandSnippet(tfv: TextFieldValue): TextFieldValue? {
    val cur = tfv.selection.start
    val text = tfv.text
    val wordStart = text.lastIndexOf('\n', cur - 1).let { if (it < 0) 0 else it + 1 }
    val word = text.substring(wordStart, cur).trim()
    val snippet = SNIPPETS.find { it.trigger == word } ?: return null
    val before = text.substring(0, wordStart)
    val after = text.substring(cur)
    val newText = before + snippet.body + after
    val newCursor = before.length + if (snippet.cursorOffset > 0) snippet.cursorOffset else snippet.body.length
    return tfv.copy(text = newText, selection = TextRange(newCursor))
}

// ─── FIND / REPLACE BAR ──────────────────────────────────────────────────────
@Composable
private fun FindBar(
    query: String,
    replaceQuery: String,
    showReplace: Boolean,
    matchCount: Int,
    currentMatch: Int,
    onQueryChange: (String) -> Unit,
    onReplaceQueryChange: (String) -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onReplace: () -> Unit,
    onReplaceAll: () -> Unit,
    onClose: () -> Unit
) {
    Surface(color = FindBarBg, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Search, null, tint = ColComment, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(color = EditorFg, fontFamily = FontFamily.Monospace, fontSize = 13.sp),
                    cursorBrush = SolidColor(EditorCursor),
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White.copy(0.05f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    decorationBox = { inner ->
                        if (query.isEmpty()) Text("Find…", color = ColComment, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                        inner()
                    }
                )
                Spacer(Modifier.width(8.dp))
                if (query.isNotEmpty()) {
                    Text("$currentMatch/$matchCount", fontSize = 11.sp, color = ColComment)
                    Spacer(Modifier.width(4.dp))
                }
                IconButton(onClick = onPrev,  modifier = Modifier.size(28.dp)) { Icon(Icons.Default.KeyboardArrowUp,   null, tint = ColComment, modifier = Modifier.size(16.dp)) }
                IconButton(onClick = onNext,  modifier = Modifier.size(28.dp)) { Icon(Icons.Default.KeyboardArrowDown, null, tint = ColComment, modifier = Modifier.size(16.dp)) }
                IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Close,             null, tint = ColComment, modifier = Modifier.size(16.dp)) }
            }
            if (showReplace) {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FindReplace, null, tint = ColComment, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    BasicTextField(
                        value = replaceQuery,
                        onValueChange = onReplaceQueryChange,
                        singleLine = true,
                        textStyle = TextStyle(color = EditorFg, fontFamily = FontFamily.Monospace, fontSize = 13.sp),
                        cursorBrush = SolidColor(EditorCursor),
                        modifier = Modifier
                            .weight(1f)
                            .background(Color.White.copy(0.05f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        decorationBox = { inner ->
                            if (replaceQuery.isEmpty()) Text("Replace with…", color = ColComment, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                            inner()
                        }
                    )
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = onReplace,    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)) { Text("Replace", fontSize = 11.sp) }
                    TextButton(onClick = onReplaceAll, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)) { Text("All",     fontSize = 11.sp) }
                }
            }
        }
    }
}

// ─── MINIMAP ─────────────────────────────────────────────────────────────────
@Composable
private fun MiniMap(source: String, scrollFraction: Float, modifier: Modifier = Modifier) {
    val lines = source.lines()
    Canvas(modifier = modifier.fillMaxHeight().width(48.dp).background(GutterBg)) {
        val lineH = (size.height / lines.size.coerceAtLeast(1)).coerceAtMost(3f)
        lines.forEachIndexed { i, line ->
            val y = i * lineH
            val density = (line.length / 80f).coerceIn(0f, 1f)
            drawRect(
                color = Color.White.copy(alpha = density * 0.25f),
                topLeft = androidx.compose.ui.geometry.Offset(4f, y),
                size = androidx.compose.ui.geometry.Size(size.width - 8f, lineH.coerceAtLeast(1f))
            )
        }
        // Viewport indicator
        val vpH = (size.height * 0.15f).coerceIn(8f, size.height)
        val vpY = scrollFraction * (size.height - vpH)
        drawRect(
            color = Color.White.copy(0.12f),
            topLeft = androidx.compose.ui.geometry.Offset(0f, vpY),
            size = androidx.compose.ui.geometry.Size(size.width, vpH)
        )
    }
}

// ─── STATUS BAR ──────────────────────────────────────────────────────────────
@Composable
private fun EditorStatusBar(
    line: Int,
    col: Int,
    charCount: Int,
    lineCount: Int,
    encoding: String = "UTF-8",
    language: String = "LaTeX"
) {
    Surface(color = StatusBarBg, modifier = Modifier.fillMaxWidth().height(24.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp).fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatusItem(Icons.Default.Code, language)
                StatusItem(Icons.Default.SwapVert, "Ln $line, Col $col")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("$charCount chars", fontSize = 9.sp, color = ColComment)
                Text("$lineCount lines", fontSize = 9.sp, color = ColComment)
                Text(encoding, fontSize = 9.sp, color = ColComment)
            }
        }
    }
}

@Composable
private fun StatusItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, null, tint = ColComment, modifier = Modifier.size(10.dp))
        Text(label, fontSize = 9.sp, color = ColComment, fontWeight = FontWeight.SemiBold, letterSpacing = 0.3.sp)
    }
}

// ─── BREADCRUMB BAR ──────────────────────────────────────────────────────────
@Composable
private fun BreadcrumbBar(source: String, cursorLine: Int) {
    val nearestSection = remember(source, cursorLine) {
        val lines = source.lines()
        val sectionRegex = Regex("""\\(section|subsection|subsubsection)\{([^}]*)\}""")
        var found = "Document"
        for (i in cursorLine downTo 0) {
            val match = sectionRegex.find(lines.getOrElse(i) { "" })
            if (match != null) {
                found = match.groupValues[2].take(30)
                break
            }
        }
        found
    }

    Surface(color = Color(0xFF0F1118), modifier = Modifier.fillMaxWidth().height(28.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp).fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Description, null, tint = ColCommand.copy(0.6f), modifier = Modifier.size(12.dp))
            Text(" › ", fontSize = 11.sp, color = ColComment)
            Text(nearestSection, fontSize = 11.sp, color = EditorFg.copy(0.8f), fontFamily = FontFamily.SansSerif)
        }
    }
}

// ─── COMMAND PALETTE ─────────────────────────────────────────────────────────
@Composable
private fun CommandPalette(onDismiss: () -> Unit, onInsert: (String) -> Unit) {
    val items = listOf(
        "\\textbf{}" to "Bold text",
        "\\textit{}" to "Italic text",
        "\\underline{}" to "Underlined text",
        "\\begin{equation}\n\t\n\\end{equation}" to "Display equation",
        "\\begin{figure}[h]\n\t\\centering\n\t\\includegraphics[width=0.8\\linewidth]{}\n\t\\caption{}\n\\end{figure}" to "Figure",
        "\\begin{table}[h]\n\t\\centering\n\t\\begin{tabular}{|c|c|}\n\t\t\\hline\n\t\t & \\\\\n\t\t\\hline\n\t\\end{tabular}\n\t\\caption{}\n\\end{table}" to "Table",
        "\\cite{}" to "Citation",
        "\\label{}" to "Label",
        "\\ref{}" to "Reference",
        "\\footnote{}" to "Footnote",
    )
    var query by remember { mutableStateOf("") }
    val filtered = items.filter { it.second.contains(query, ignoreCase = true) || it.first.contains(query, ignoreCase = true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Command Palette", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search commands…") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                    items(filtered.size) { i ->
                        val (cmd, desc) = filtered[i]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onInsert(cmd); onDismiss() }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(desc, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = EditorFg)
                            Text(cmd.take(24).replace('\n', ' '), fontSize = 10.sp, color = ColComment, fontFamily = FontFamily.Monospace)
                        }
                        HorizontalDivider(color = Color.White.copy(0.04f))
                    }
                }
            }
        },
        confirmButton = {}
    )
}

// ─── MAIN EDITOR PANEL ───────────────────────────────────────────────────────
@Composable
fun TextFieldEditorPanel(
    source: String,
    onSourceChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val transformation = remember { LatexVisualTransformation() }
    var tfv by remember(source) {
        mutableStateOf(TextFieldValue(text = source, selection = TextRange(source.length.coerceAtLeast(0))))
    }

    // ── Find/Replace state
    var showFind    by remember { mutableStateOf(false) }
    var showReplace by remember { mutableStateOf(false) }
    var findQuery   by remember { mutableStateOf("") }
    var replQuery   by remember { mutableStateOf("") }
    var matchIndex  by remember { mutableStateOf(0) }

    val matches = remember(tfv.text, findQuery) {
        if (findQuery.isBlank()) emptyList()
        else Regex(Regex.escape(findQuery), RegexOption.IGNORE_CASE).findAll(tfv.text).map { it.range }.toList()
    }

    fun nextMatch()  { if (matches.isNotEmpty()) matchIndex = (matchIndex + 1) % matches.size }
    fun prevMatch()  { if (matches.isNotEmpty()) matchIndex = (matchIndex - 1 + matches.size) % matches.size }

    // ── Command palette
    var showCommandPalette by remember { mutableStateOf(false) }

    // ── Document stats from cursor
    val lines = tfv.text.lines()
    val cursorPos = tfv.selection.start.coerceIn(0, tfv.text.length)
    val textBeforeCursor = tfv.text.substring(0, cursorPos)
    val currentLine = textBeforeCursor.count { it == '\n' }
    val currentCol  = textBeforeCursor.length - (textBeforeCursor.lastIndexOf('\n') + 1)

    // ── Scroll
    val scrollState  = rememberScrollState()
    val scrollFrac   = if (scrollState.maxValue == 0) 0f else scrollState.value.toFloat() / scrollState.maxValue

    // ── Command palette insertion
    fun insertAtCursor(text: String) {
        val before = tfv.text.substring(0, tfv.selection.start)
        val after  = tfv.text.substring(tfv.selection.end)
        val newText = before + text + after
        val newCursor = before.length + text.length
        val newTfv = tfv.copy(text = newText, selection = TextRange(newCursor))
        tfv = newTfv
        onSourceChange(newText)
    }

    if (showCommandPalette) {
        CommandPalette(
            onDismiss = { showCommandPalette = false },
            onInsert  = ::insertAtCursor
        )
    }

    Column(modifier = modifier.fillMaxSize()) {

        // ── Breadcrumb
        BreadcrumbBar(source = tfv.text, cursorLine = currentLine)

        // ── Find bar (animated)
        AnimatedVisibility(visible = showFind, enter = expandVertically(), exit = shrinkVertically()) {
            FindBar(
                query          = findQuery,
                replaceQuery   = replQuery,
                showReplace    = showReplace,
                matchCount     = matches.size,
                currentMatch   = if (matches.isEmpty()) 0 else matchIndex + 1,
                onQueryChange  = { findQuery = it; matchIndex = 0 },
                onReplaceQueryChange = { replQuery = it },
                onNext         = ::nextMatch,
                onPrev         = ::prevMatch,
                onReplace      = {
                    if (matches.isNotEmpty()) {
                        val r = matches[matchIndex]
                        val new = tfv.text.replaceRange(r, replQuery)
                        val newTfv = tfv.copy(text = new); tfv = newTfv; onSourceChange(new)
                    }
                },
                onReplaceAll   = {
                    val new = tfv.text.replace(findQuery, replQuery, ignoreCase = true)
                    val newTfv = tfv.copy(text = new); tfv = newTfv; onSourceChange(new)
                },
                onClose = { showFind = false; showReplace = false }
            )
        }

        // ── Editor toolbar
        Surface(color = GutterBg, modifier = Modifier.fillMaxWidth().height(36.dp)) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp).fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                EditorToolbarButton("⌘P",  "Command Palette") { showCommandPalette = true }
                EditorToolbarButton("⌘F",  "Find")            { showFind = true; showReplace = false }
                EditorToolbarButton("⌘H",  "Replace")         { showFind = true; showReplace = true }
                Spacer(Modifier.width(4.dp))
                VerticalDivider(color = Color.White.copy(0.08f), modifier = Modifier.height(16.dp))
                Spacer(Modifier.width(4.dp))
                EditorToolbarButton("B",  "Bold",     Color(0xFFFFB86C), italic = false, bold = true) { insertAtCursor("\\textbf{}") }
                EditorToolbarButton("I",  "Italic",   Color(0xFF8BE9FD), italic = true)               { insertAtCursor("\\textit{}") }
                EditorToolbarButton("\\$","Math",     ColMath)                                         { insertAtCursor("\$\$") }
                Spacer(Modifier.weight(1f))
                // Snippet hint
                Text("Tab → expand snippet", fontSize = 9.sp, color = ColComment.copy(0.6f))
            }
        }

        // ── Main editing area
        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            // Gutter
            Surface(color = GutterBg, modifier = Modifier.width(50.dp).fillMaxHeight()) {
                Column(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .verticalScroll(scrollState)
                        .widthIn(min = 50.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    val lineCount = lines.size.coerceAtLeast(1)
                    repeat(lineCount) { i ->
                        val isActive = i == currentLine
                        Text(
                            text = (i + 1).toString(),
                            color = if (isActive) GutterActiveLine else GutterText,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            textAlign = TextAlign.End,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(end = 8.dp, bottom = 2.dp).height(20.dp)
                        )
                    }
                    Spacer(Modifier.height(100.dp))
                }
            }

            // Editor text field
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(EditorBg)
            ) {
                // ① Active line highlight — drawn FIRST so BasicTextField renders on top
                val activeLineY = (currentLine * 20 + 4).dp
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .offset(y = activeLineY)
                        .background(ActiveLineBg)
                )

                // ② Editor — always on top of the highlight
                BasicTextField(
                    value = tfv,
                    onValueChange = { new ->
                        val paired = handleSmartInput(new, tfv)
                        tfv = paired
                        if (paired.text != source) onSourceChange(paired.text)
                    },
                    textStyle = TextStyle(
                        color         = EditorFg,
                        fontFamily    = FontFamily.Monospace,
                        fontSize      = 14.sp,
                        lineHeight    = 20.sp,
                        letterSpacing = 0.3.sp
                    ),
                    cursorBrush          = SolidColor(EditorCursor),
                    visualTransformation = transformation,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
                        .verticalScroll(scrollState)
                        .onKeyEvent { event ->
                            if (event.type == KeyEventType.KeyDown) {
                                when {
                                    event.key == Key.Tab -> {
                                        val expanded = tryExpandSnippet(tfv)
                                        if (expanded != null) {
                                            tfv = expanded; onSourceChange(expanded.text)
                                        } else {
                                            val cur = tfv.selection.start
                                            val newText = tfv.text.substring(0, cur) + "    " + tfv.text.substring(tfv.selection.end)
                                            val newTfv = tfv.copy(text = newText, selection = TextRange(cur + 4))
                                            tfv = newTfv; onSourceChange(newText)
                                        }
                                        true
                                    }
                                    (event.isMetaPressed || event.isCtrlPressed) && event.key == Key.F -> { showFind = true; showReplace = false; true }
                                    (event.isMetaPressed || event.isCtrlPressed) && event.key == Key.H -> { showFind = true; showReplace = true; true }
                                    event.key == Key.Escape -> { if (showFind) { showFind = false; showReplace = false; true } else false }
                                    else -> false
                                }
                            } else false
                        }
                )
            }

            // Minimap
            MiniMap(source = tfv.text, scrollFraction = scrollFrac)
        }

        // ── Status bar
        EditorStatusBar(
            line      = currentLine + 1,
            col       = currentCol + 1,
            charCount = tfv.text.length,
            lineCount = lines.size
        )
    }
}

// ─── TOOLBAR BUTTON ──────────────────────────────────────────────────────────
@Composable
private fun EditorToolbarButton(
    label: String,
    tooltip: String,
    tint: Color = ColComment,
    italic: Boolean = false,
    bold: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = label,
            fontSize   = 11.sp,
            color      = tint,
            fontFamily = FontFamily.Monospace,
            fontStyle  = if (italic) FontStyle.Italic else FontStyle.Normal,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// ─── FALLBACK PDF PANEL ──────────────────────────────────────────────────────
@Composable
fun FallbackPdfPreviewPanel(pdfBase64: String?, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().background(Color(0xFF1A1C2E)), contentAlignment = Alignment.Center) {
        if (!pdfBase64.isNullOrEmpty()) {
            Surface(
                modifier = Modifier.padding(32.dp).aspectRatio(0.707f).fillMaxHeight(),
                color = Color.White, shadowElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) { Text("PDF Preview Available", color = Color.Black) }
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.PictureAsPdf, null, tint = ColComment, modifier = Modifier.size(64.dp))
                Text("Compile to see PDF", fontWeight = FontWeight.Bold, color = ColComment, fontSize = 16.sp)
                Text("Press Compile in the toolbar", fontSize = 12.sp, color = GutterText)
            }
        }
    }
}
