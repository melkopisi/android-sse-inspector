package me.melkopisi.sseinspector.ui.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import me.melkopisi.sseinspector.ui.theme.TerminalColors
import org.json.JSONArray
import org.json.JSONObject

object JsonSyntaxHighlighter {

    private const val JSON_INDENT_SPACES = 2
    private const val GROUP_STRING = 1
    private const val GROUP_COLON = 2
    private const val GROUP_KEYWORD = 3

    private val jsonTokenPattern = Regex(
        """("(?:\\.|[^"\\])*")(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d+)?(?:[eE][+-]?\d+)?|[{}]|\[|\]|[,:]"""
    )

    fun isJson(text: String): Boolean {
        val trimmed = text.trim()
        val firstBrace = trimmed.indexOfFirst { it == '{' || it == '[' }
        val lastBrace = trimmed.indexOfLast { it == '}' || it == ']' }
        return firstBrace != -1 && lastBrace > firstBrace
    }

    fun prettify(raw: String): String {
        val trimmed = raw.trim()
        val firstBrace = trimmed.indexOfFirst { it == '{' || it == '[' }
        val lastBrace = trimmed.indexOfLast { it == '}' || it == ']' }
        if (firstBrace == -1 || lastBrace <= firstBrace) {
            return raw
        }
        val prefix = trimmed.substring(0, firstBrace)
        val jsonPart = trimmed.substring(firstBrace, lastBrace + 1)
        val suffix = trimmed.substring(lastBrace + 1)

        val prettyJson = runCatching {
            when {
                jsonPart.startsWith("{") -> JSONObject(jsonPart).toString(JSON_INDENT_SPACES)
                jsonPart.startsWith("[") -> JSONArray(jsonPart).toString(JSON_INDENT_SPACES)
                else -> jsonPart
            }
        }.getOrDefault(jsonPart)

        return if (prefix.isEmpty() && suffix.isEmpty()) {
            prettyJson
        } else {
            "$prefix$prettyJson$suffix"
        }
    }

    fun colorize(text: String): AnnotatedString {
        val prettyText = prettify(text)
        if (!isJson(prettyText)) {
            return AnnotatedString(prettyText)
        }

        return runCatching {
            buildAnnotatedString {
                append(prettyText)
                for (match in jsonTokenPattern.findAll(prettyText)) {
                    val range = match.range
                    val stringGroup = match.groups[GROUP_STRING]
                    val colonGroup = match.groups[GROUP_COLON]
                    val keywordGroup = match.groups[GROUP_KEYWORD]

                    when {
                        colonGroup != null -> {
                            val keyEnd = colonGroup.range.first
                            addStyle(
                                SpanStyle(color = TerminalColors.JsonKey),
                                range.first,
                                keyEnd
                            )
                            addStyle(
                                SpanStyle(color = TerminalColors.JsonPunctuation),
                                keyEnd,
                                range.last + 1
                            )
                        }
                        stringGroup != null -> {
                            addStyle(
                                SpanStyle(color = TerminalColors.JsonString),
                                range.first,
                                range.last + 1
                            )
                        }
                        keywordGroup != null -> {
                            val word = keywordGroup.value
                            val color = if (word == "null") {
                                TerminalColors.JsonNull
                            } else {
                                TerminalColors.JsonBoolean
                            }
                            addStyle(
                                SpanStyle(color = color),
                                range.first,
                                range.last + 1
                            )
                        }
                        match.value.first().isDigit() || match.value.startsWith("-") && match.value.length > 1 -> {
                            addStyle(
                                SpanStyle(color = TerminalColors.JsonNumber),
                                range.first,
                                range.last + 1
                            )
                        }
                        else -> {
                            addStyle(
                                SpanStyle(color = TerminalColors.JsonPunctuation),
                                range.first,
                                range.last + 1
                            )
                        }
                    }
                }
            }
        }.getOrDefault(AnnotatedString(prettyText))
    }
}
