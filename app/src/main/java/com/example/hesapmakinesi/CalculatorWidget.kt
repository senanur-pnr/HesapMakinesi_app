package com.example.hesapmakinesi

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.GlanceTheme
import androidx.glance.text.FontWeight
import androidx.glance.text.TextAlign
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.currentState

class CalculatorWidget : GlanceAppWidget() {
    override var stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val display = prefs[DisplayKey] ?: "0"

            GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(Color(0xFF000000))
                        .padding(8.dp),
                    horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                    verticalAlignment = Alignment.Vertical.CenterVertically
                ) {
                    // Ekran Alanı
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .padding(bottom = 8.dp)
                            .background(Color(0xFF1C1C1C)),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Text(
                            text = display,
                            style = TextStyle(
                                color = ColorProvider(Color.White),
                                fontSize = if (display.length > 10) 16.sp else 24.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End
                            ),
                            modifier = GlanceModifier.padding(horizontal = 12.dp),
                            maxLines = 1
                        )
                    }
                    
                    // Tuş Takımı
                    Column(modifier = GlanceModifier.fillMaxSize()) {
                        val rows = listOf(
                            listOf("7", "8", "9", "/"),
                            listOf("4", "5", "6", "*"),
                            listOf("1", "2", "3", "-"),
                            listOf("C", "0", "=", "+")
                        )

                        rows.forEach { rowKeys ->
                            Row(modifier = GlanceModifier.fillMaxWidth().defaultWeight()) {
                                rowKeys.forEach { key ->
                                    WidgetButton(key, key)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun RowScope.WidgetButton(text: String, value: String) {
        val bgColor = when (text) {
            "/", "*", "-", "+", "=" -> Color(0xFFFF9F0A)
            "C" -> Color(0xFFA5A5A5)
            else -> Color(0xFF333333)
        }
        val contentColor = if (text == "C") Color.Black else Color.White

        Box(
            modifier = GlanceModifier
                .defaultWeight()
                .fillMaxHeight()
                .padding(2.dp)
                .background(bgColor)
                .clickable(actionRunCallback<CalculatorActionCallback>(
                    actionParametersOf(ValueParam to value)
                )),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = TextStyle(
                    color = ColorProvider(contentColor),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            )
        }
    }

    companion object {
        val DisplayKey = stringPreferencesKey("widget_display")
        val ValueParam = ActionParameters.Key<String>("value")
    }
}

class CalculatorActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val value = parameters[CalculatorWidget.ValueParam] ?: return
        
        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
            val current = prefs[CalculatorWidget.DisplayKey] ?: "0"
            val next = when (value) {
                "C" -> "0"
                "=" -> {
                    try {
                        evaluateSimple(current)
                    } catch (e: Exception) { "Hata" }
                }
                else -> {
                    if (current == "0" && value !in "+-*/") value 
                    else current + value
                }
            }
            val mutablePrefs = prefs.toMutablePreferences()
            mutablePrefs[CalculatorWidget.DisplayKey] = next
            mutablePrefs
        }
        CalculatorWidget().update(context, glanceId)
    }

    private fun evaluateSimple(expr: String): String {
        val ops = charArrayOf('+', '-', '*', '/')
        val opIndex = expr.indexOfAny(ops)
        if (opIndex == -1) return expr
        
        val op = expr[opIndex]
        val n1 = expr.substring(0, opIndex).toDoubleOrNull() ?: return "Hata"
        val n2 = expr.substring(opIndex + 1).toDoubleOrNull() ?: return "Hata"
        
        val res = when(op) {
            '+' -> n1 + n2
            '-' -> n1 - n2
            '*' -> n1 * n2
            '/' -> if (n2 != 0.0) n1 / n2 else Double.NaN
            else -> n1
        }
        
        return if (res.isNaN()) "Hata" 
        else {
            val s = if (res % 1.0 == 0.0) res.toLong().toString() 
                    else String.format(java.util.Locale.US, "%.4f", res).trimEnd('0').trimEnd('.')
            if (s.length > 12) s.take(12) else s
        }
    }
}

class CalculatorWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CalculatorWidget()
}
