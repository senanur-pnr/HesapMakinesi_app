package com.example.hesapmakinesi

import android.os.Bundle
import android.view.HapticFeedbackConstants
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hesapmakinesi.ui.theme.HesapMakinesiTheme
import java.util.Locale
import kotlin.math.*

enum class AppTheme {
    Dark, Light, MidnightBlue, RoseGold, Retro
}

data class ThemeColors(
    val background: Color,
    val displayText: Color,
    val secondaryText: Color,
    val scientificBtn: Color,
    val numberBtn: Color,
    val topRowBtn: Color,
    val operationBtn: Color,
    val menuBackground: Color
)

fun getThemeColors(theme: AppTheme): ThemeColors {
    return when (theme) {
        AppTheme.Dark -> ThemeColors(
            background = Color(0xFF000000),
            displayText = Color.White,
            secondaryText = Color.LightGray,
            scientificBtn = Color(0xFF212121),
            numberBtn = Color(0xFF333333),
            topRowBtn = Color(0xFFA5A5A5),
            operationBtn = Color(0xFFFF9F0A),
            menuBackground = Color(0xFF212121)
        )
        AppTheme.Light -> ThemeColors(
            background = Color(0xFFFFFFFF),
            displayText = Color.Black,
            secondaryText = Color.DarkGray,
            scientificBtn = Color(0xFFF0F0F0),
            numberBtn = Color(0xFFE0E0E0),
            topRowBtn = Color(0xFFD4D4D2),
            operationBtn = Color(0xFFFF9F0A),
            menuBackground = Color.White
        )
        AppTheme.MidnightBlue -> ThemeColors(
            background = Color(0xFF1A1A2E),
            displayText = Color.White,
            secondaryText = Color(0xFF4E5D6C),
            scientificBtn = Color(0xFF0F3460),
            numberBtn = Color(0xFF16213E),
            topRowBtn = Color(0xFFE94560),
            operationBtn = Color(0xFFE94560),
            menuBackground = Color(0xFF16213E)
        )
        AppTheme.RoseGold -> ThemeColors(
            background = Color(0xFFFFF5F5),
            displayText = Color(0xFF5D4037),
            secondaryText = Color(0xFF8D6E63),
            scientificBtn = Color(0xFFFFEBEE),
            numberBtn = Color(0xFFF8BBD0),
            topRowBtn = Color(0xFFF48FB1),
            operationBtn = Color(0xFFEC407A),
            menuBackground = Color(0xFFFFF5F5)
        )
        AppTheme.Retro -> ThemeColors(
            background = Color(0xFFC0C0C0),
            displayText = Color.Black,
            secondaryText = Color(0xFF404040),
            scientificBtn = Color(0xFF808080),
            numberBtn = Color(0xFFE0E0E0),
            topRowBtn = Color(0xFFFFFF00),
            operationBtn = Color(0xFF00FF00),
            menuBackground = Color(0xFFC0C0C0)
        )
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HesapMakinesiTheme {
                CalculatorScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen() {
    var state by remember { mutableStateOf(CalculatorState()) }
    var menuExpanded by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val view = LocalView.current

    val themeColors = getThemeColors(state.currentTheme)
    
    val onAction: (CalculatorAction) -> Unit = { action ->
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        state = calculatorLogic(state, action)
    }

    // Canlı kurları çekme
    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.api.getLatestRates()
            if (response.result == "success") {
                android.util.Log.d("HesapMakinesi", "Kurlar başarıyla güncellendi: ${response.rates["TRY"]}")
                onAction(CalculatorAction.UpdateRates(response.rates))
            }
        } catch (e: Exception) {
            android.util.Log.e("HesapMakinesi", "Kur çekme hatası: ${e.message}")
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = themeColors.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(8.dp)
                .safeDrawingPadding()
        ) {
            // Üst Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showHistory = true }) {
                        Icon(Icons.Default.List, "Geçmiş", tint = themeColors.displayText)
                    }
                    Text(
                        text = if (state.isDegreeMode) "Deg" else "Rad",
                        color = themeColors.secondaryText,
                        fontSize = 14.sp
                    )
                }
                
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, "Menü", tint = themeColors.displayText)
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.background(themeColors.menuBackground)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Temel Hesap Makinesi", color = themeColors.displayText) },
                            onClick = {
                                if (state.isScientificMode || state.isConverterMode || state.isCurrencyMode || state.isProgrammerMode) onAction(CalculatorAction.ToggleCalculatorMode)
                                if (state.isConverterMode) onAction(CalculatorAction.ToggleConverterMode)
                                if (state.isCurrencyMode) onAction(CalculatorAction.ToggleCurrencyMode)
                                if (state.isProgrammerMode) onAction(CalculatorAction.ToggleProgrammerMode)
                                menuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Bilimsel Hesap Makinesi", color = themeColors.displayText) },
                            onClick = {
                                if (!state.isScientificMode) onAction(CalculatorAction.ToggleCalculatorMode)
                                menuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Birim Dönüştürücü", color = themeColors.displayText) },
                            onClick = {
                                if (!state.isConverterMode) onAction(CalculatorAction.ToggleConverterMode)
                                menuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Döviz Çevirici", color = themeColors.displayText) },
                            onClick = {
                                if (!state.isCurrencyMode) onAction(CalculatorAction.ToggleCurrencyMode)
                                menuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Programcı Modu", color = themeColors.displayText) },
                            onClick = {
                                if (!state.isProgrammerMode) onAction(CalculatorAction.ToggleProgrammerMode)
                                menuExpanded = false
                            }
                        )
                        HorizontalDivider(color = themeColors.secondaryText.copy(alpha = 0.2f))
                        Text("Temalar", modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 4.dp), fontSize = 12.sp, color = themeColors.secondaryText)
                        AppTheme.entries.forEach { theme ->
                            DropdownMenuItem(
                                text = { Text(theme.name, color = themeColors.displayText) },
                                onClick = {
                                    onAction(CalculatorAction.SetTheme(theme))
                                    menuExpanded = false
                                },
                                trailingIcon = { if (state.currentTheme == theme) Text("✓", color = themeColors.operationBtn) }
                            )
                        }
                    }
                }
            }

            // Ekran (Display)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(if (state.isScientificMode || state.isProgrammerMode) 0.2f else 0.4f),
                contentAlignment = Alignment.BottomEnd
            ) {
                if (state.isProgrammerMode) {
                    ProgrammerDisplay(state, themeColors.displayText, themeColors.secondaryText)
                } else if (state.isCurrencyMode) {
                    CurrencyDisplay(state, themeColors.displayText, themeColors.secondaryText)
                } else if (state.isConverterMode) {
                    ConverterDisplay(state, themeColors.displayText, themeColors.secondaryText)
                } else {
                    Text(
                        text = formatDisplay(state),
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth(),
                        fontWeight = FontWeight.Light,
                        fontSize = if (formatDisplay(state).length > 12) 35.sp else 55.sp,
                        color = themeColors.displayText,
                        maxLines = 2
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tuş Takımı
            Box(modifier = Modifier.weight(if (state.isScientificMode || state.isProgrammerMode) 0.8f else 0.6f)) {
                if (state.isProgrammerMode) {
                    ProgrammerUI(onAction, themeColors.scientificBtn, themeColors.displayText, state)
                } else if (state.isCurrencyMode) {
                    CurrencyUI(onAction, themeColors.scientificBtn, themeColors.displayText, state)
                } else if (state.isConverterMode) {
                    ConverterUI(onAction, themeColors.scientificBtn, themeColors.displayText, state)
                } else {
                    Column(verticalArrangement = Arrangement.Bottom) {
                        if (state.isScientificMode) {
                            ScientificKeypad(onAction, themeColors.scientificBtn, themeColors.displayText)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        StandardKeypad(onAction, themeColors.numberBtn, themeColors.topRowBtn, themeColors.operationBtn, themeColors.displayText, isCompact = state.isScientificMode)
                    }
                }
            }
        }
    }

    if (showHistory) {
        ModalBottomSheet(
            onDismissRequest = { showHistory = false },
            sheetState = sheetState,
            containerColor = themeColors.menuBackground,
            contentColor = themeColors.displayText
        ) {
            Column(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.6f).padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("İşlem Geçmişi", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { onAction(CalculatorAction.ClearHistory) }) {
                        Icon(Icons.Default.Delete, "Sil")
                    }
                }
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(state.history.reversed()) { historyItem ->
                        Text(
                            text = historyItem,
                            modifier = Modifier.fillMaxWidth().clickable {
                                val result = historyItem.substringAfter("=").trim()
                                onAction(CalculatorAction.SelectHistoryResult(result))
                                showHistory = false
                            }.padding(vertical = 12.dp),
                            fontSize = 18.sp,
                            color = themeColors.operationBtn
                        )
                        HorizontalDivider(color = themeColors.secondaryText.copy(alpha = 0.1f))
                    }
                }
            }
        }
    }
}

@Composable
fun ProgrammerDisplay(state: CalculatorState, textColor: Color, secondaryColor: Color) {
    val doubleValue = state.number1.toDoubleOrNull() ?: 0.0
    val integerPart = doubleValue.toLong()
    val fractionalPart = doubleValue - integerPart

    fun convertFraction(value: Double, radix: Int, precision: Int = 8): String {
        if (value == 0.0) return ""
        var fraction = value
        var result = "."
        repeat(precision) {
            fraction *= radix
            val digit = fraction.toInt()
            result += digit.toString(radix).uppercase()
            fraction -= digit
            if (fraction == 0.0) return result
        }
        return result
    }

    fun formatWithBase(radix: Int): String {
        val intStr = integerPart.toString(radix).uppercase()
        val fracStr = if (fractionalPart > 0) convertFraction(fractionalPart, radix) else ""
        return intStr + fracStr
    }

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
        Text("DEC: ${state.number1}", fontSize = 18.sp, color = textColor)
        Text("HEX: ${formatWithBase(16)}", fontSize = 18.sp, color = secondaryColor)
        Text("BIN: ${formatWithBase(2)}", fontSize = 14.sp, color = secondaryColor)
        Text("OCT: ${formatWithBase(8)}", fontSize = 18.sp, color = secondaryColor)
    }
}

@Composable
fun ProgrammerUI(onAction: (CalculatorAction) -> Unit, btnColor: Color, textColor: Color, state: CalculatorState) {
    val btnTextColor = if (state.currentTheme == AppTheme.RoseGold) Color.Black else textColor
    
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val keys = listOf(
            listOf("7", "8", "9", "AC"),
            listOf("4", "5", "6", "Del"),
            listOf("1", "2", "3", "0"),
            listOf(".", "", "", "")
        )
        keys.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { key ->
                    if (key.isNotEmpty()) {
                        Button(
                            onClick = {
                                when(key) {
                                    "AC" -> onAction(CalculatorAction.Clear)
                                    "Del" -> onAction(CalculatorAction.Delete)
                                    "." -> onAction(CalculatorAction.Decimal)
                                    else -> onAction(CalculatorAction.Number(key.toInt()))
                                }
                            },
                            modifier = Modifier.weight(1f).height(60.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = if(key=="AC"||key=="Del") Color.Gray else btnColor),
                            shape = CircleShape
                        ) { Text(key, fontSize = 20.sp, color = btnTextColor) }
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
        Text("Ondalıklı dönüşümlerde 2, 8 ve 16 tabanlarında tam karşılık gösterilir.", fontSize = 11.sp, color = textColor.copy(alpha = 0.7f), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun CurrencyDisplay(state: CalculatorState, textColor: Color, secondaryColor: Color) {
    val result = performCurrencyConversion(state)
    val symbols = mapOf("USD" to "$", "EUR" to "€", "TRY" to "₺", "GBP" to "£", "JPY" to "¥")
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
        Text(state.number1 + " " + (symbols[state.fromCurrency] ?: ""), fontSize = 24.sp, color = secondaryColor)
        Text("= " + result + " " + (symbols[state.toCurrency] ?: ""), fontSize = 40.sp, color = textColor, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun CurrencyUI(onAction: (CalculatorAction) -> Unit, btnColor: Color, textColor: Color, state: CalculatorState) {
    val currencies = listOf("USD", "EUR", "TRY", "GBP", "JPY")
    val btnTextColor = if (state.currentTheme == AppTheme.RoseGold) Color.Black else textColor
    
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Şundan:", color = textColor, fontSize = 11.sp)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            currencies.forEach { curr ->
                Button(
                    onClick = { onAction(CalculatorAction.SetCurrencyUnits(curr, state.toCurrency)) },
                    modifier = Modifier.weight(1f).height(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (state.fromCurrency == curr) Color(0xFFFF9F0A) else btnColor.copy(alpha = 0.3f)),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(8.dp)
                ) { Text(curr, fontSize = 10.sp, color = textColor) }
            }
        }
        Text("Şuna:", color = textColor, fontSize = 11.sp)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            currencies.forEach { curr ->
                Button(
                    onClick = { onAction(CalculatorAction.SetCurrencyUnits(state.fromCurrency, curr)) },
                    modifier = Modifier.weight(1f).height(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (state.toCurrency == curr) Color(0xFFFF9F0A) else btnColor.copy(alpha = 0.3f)),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(8.dp)
                ) { Text(curr, fontSize = 10.sp, color = textColor) }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        val keys = listOf(listOf("7", "8", "9"), listOf("4", "5", "6"), listOf("1", "2", "3"), listOf("AC", "0", "Del"))
        keys.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { key ->
                    Button(
                        onClick = {
                            when(key) {
                                "AC" -> onAction(CalculatorAction.Clear)
                                "Del" -> onAction(CalculatorAction.Delete)
                                else -> onAction(CalculatorAction.Number(key.toInt()))
                            }
                        },
                        modifier = Modifier.weight(1f).height(45.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if(key=="AC"||key=="Del") Color.Gray else btnColor),
                        shape = CircleShape
                    ) { Text(key, fontSize = 18.sp, color = btnTextColor) }
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { onAction(CalculatorAction.Decimal) }, modifier = Modifier.weight(1f).height(45.dp), colors = ButtonDefaults.buttonColors(containerColor = btnColor), shape = CircleShape) { Text(".", fontSize = 20.sp, color = btnTextColor) }
        }
    }
}

@Composable
fun ConverterDisplay(state: CalculatorState, textColor: Color, secondaryColor: Color) {
    val result = performUnitConversion(state)
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
        Text(state.number1 + " " + state.fromUnit, fontSize = 24.sp, color = secondaryColor)
        Text("= " + result + " " + state.toUnit, fontSize = 40.sp, color = textColor, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ConverterUI(onAction: (CalculatorAction) -> Unit, btnColor: Color, textColor: Color, state: CalculatorState) {
    val unitsMap = mapOf(
        "Uzunluk" to listOf("Metre", "Kilometre", "Mil", "Ayak"),
        "Ağırlık" to listOf("Kilogram", "Gram", "Libre", "Ons"),
        "Sıcaklık" to listOf("Celsius", "Fahrenheit", "Kelvin"),
        "Veri" to listOf("Byte", "Kilobyte", "Megabyte", "Gigabyte")
    )
    val btnTextColor = if (state.currentTheme == AppTheme.RoseGold) Color.Black else textColor

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf("Uzunluk", "Ağırlık", "Sıcaklık", "Veri").forEach { cat ->
                Button(
                    onClick = { onAction(CalculatorAction.SetConverterCategory(cat)) },
                    modifier = Modifier.weight(1f).height(38.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (state.converterCategory == cat) Color(0xFFFF9F0A) else btnColor),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(cat, fontSize = 10.sp, color = if (state.converterCategory == cat) Color.Black else textColor)
                }
            }
        }

        Text("Şundan:", color = textColor, fontSize = 11.sp)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            unitsMap[state.converterCategory]?.forEach { unit ->
                Button(
                    onClick = { onAction(CalculatorAction.SetConverterUnits(unit, state.toUnit)) },
                    modifier = Modifier.weight(1f).height(32.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (state.fromUnit == unit) Color(0xFFFF9F0A) else btnColor.copy(alpha = 0.3f)),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(8.dp)
                ) { Text(unit, fontSize = 9.sp, color = textColor) }
            }
        }

        Text("Şuna:", color = textColor, fontSize = 11.sp)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            unitsMap[state.converterCategory]?.forEach { unit ->
                Button(
                    onClick = { onAction(CalculatorAction.SetConverterUnits(state.fromUnit, unit)) },
                    modifier = Modifier.weight(1f).height(32.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (state.toUnit == unit) Color(0xFFFF9F0A) else btnColor.copy(alpha = 0.3f)),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(8.dp)
                ) { Text(unit, fontSize = 9.sp, color = textColor) }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        
        val keys = listOf(listOf("7", "8", "9"), listOf("4", "5", "6"), listOf("1", "2", "3"), listOf("AC", "0", "Del"))
        keys.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { key ->
                    Button(
                        onClick = {
                            when(key) {
                                "AC" -> onAction(CalculatorAction.Clear)
                                "Del" -> onAction(CalculatorAction.Delete)
                                else -> onAction(CalculatorAction.Number(key.toInt()))
                            }
                        },
                        modifier = Modifier.weight(1f).height(45.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if(key=="AC"||key=="Del") Color.Gray else btnColor),
                        shape = CircleShape
                    ) { Text(key, fontSize = 18.sp, color = btnTextColor) }
                }
            }
        }
        
        Row(modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { onAction(CalculatorAction.Decimal) }, modifier = Modifier.weight(1f).height(45.dp), colors = ButtonDefaults.buttonColors(containerColor = btnColor), shape = CircleShape) { Text(".", fontSize = 20.sp, color = btnTextColor) }
        }
    }
}

@Composable
fun ScientificKeypad(onAction: (CalculatorAction) -> Unit, btnColor: Color, textColor: Color) {
    val keys = listOf("(", ")", "mc", "m+", "m-", "mr", "x²", "x³", "xʸ", "eˣ", "10ˣ", "¹/x", "²√x", "³√x", "ʸ√x", "ln", "log₁₀", "x!", "sin", "cos", "tan", "e", "EE", "Rad", "sinh", "cosh", "tanh", "π", "Rand", "Deg")
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        keys.chunked(6).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                row.forEach { key ->
                    Button(onClick = { when(key) { "mc" -> onAction(CalculatorAction.MemoryClear); "m+" -> onAction(CalculatorAction.MemoryAdd); "m-" -> onAction(CalculatorAction.MemorySubtract); "mr" -> onAction(CalculatorAction.MemoryRecall); "Rad", "Deg" -> onAction(CalculatorAction.ToggleAngleMode); "π" -> onAction(CalculatorAction.Constant(PI)); "e" -> onAction(CalculatorAction.Constant(E)); "Rand" -> onAction(CalculatorAction.Constant(Math.random())); else -> onAction(CalculatorAction.Scientific(key)) } }, modifier = Modifier.weight(1f).height(34.dp), colors = ButtonDefaults.buttonColors(containerColor = btnColor), contentPadding = PaddingValues(0.dp), shape = CircleShape) { Text(key, fontSize = 10.sp, color = textColor) }
                }
            }
        }
    }
}

@Composable
fun StandardKeypad(onAction: (CalculatorAction) -> Unit, numColor: Color, topColor: Color, opColor: Color, textColor: Color, isCompact: Boolean) {
    val rows = listOf(listOf("Del", "AC", "%", "÷"), listOf("7", "8", "9", "×"), listOf("4", "5", "6", "−"), listOf("1", "2", "3", "+"), listOf("±", "0", ".", "="))
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { key ->
                    val color = when { key in listOf("÷", "×", "−", "+", "=") -> opColor; key in listOf("Del", "AC", "%", "±") -> topColor; else -> numColor }
                    val btnTextColor = if (color == topColor) Color.Black else textColor
                    Button(onClick = { when (key) { "AC" -> onAction(CalculatorAction.Clear); "Del" -> onAction(CalculatorAction.Delete); "=" -> onAction(CalculatorAction.Calculate); "." -> onAction(CalculatorAction.Decimal); "±" -> onAction(CalculatorAction.ToggleSign); "÷" -> onAction(CalculatorAction.Operation("/")); "×" -> onAction(CalculatorAction.Operation("*")); "−" -> onAction(CalculatorAction.Operation("-")); "+" -> onAction(CalculatorAction.Operation("+")); "%" -> onAction(CalculatorAction.Scientific("%")); else -> onAction(CalculatorAction.Number(key.toInt())) } }, modifier = Modifier.weight(1f).height(if(isCompact) 48.dp else 70.dp), colors = ButtonDefaults.buttonColors(containerColor = color), shape = CircleShape, contentPadding = PaddingValues(0.dp)) { Text(key, fontSize = 20.sp, color = btnTextColor) }
                }
            }
        }
    }
}

data class CalculatorState(
    val number1: String = "0",
    val number2: String = "",
    val operation: String? = null,
    val memory: Double = 0.0,
    val isDegreeMode: Boolean = true,
    val isScientificMode: Boolean = false,
    val isConverterMode: Boolean = false,
    val isCurrencyMode: Boolean = false,
    val isProgrammerMode: Boolean = false,
    val currentTheme: AppTheme = AppTheme.Dark,
    val history: List<String> = emptyList(),
    val converterCategory: String = "Uzunluk",
    val fromUnit: String = "Metre",
    val toUnit: String = "Kilometre",
    val fromCurrency: String = "USD",
    val toCurrency: String = "TRY",
    val rates: Map<String, Double> = mapOf("USD" to 1.0, "EUR" to 0.92, "TRY" to 34.25, "GBP" to 0.77, "JPY" to 150.0)
)

sealed class CalculatorAction {
    data class Number(val number: Int): CalculatorAction()
    data object Clear: CalculatorAction()
    data object Delete: CalculatorAction()
    data object Decimal: CalculatorAction()
    data object Calculate: CalculatorAction()
    data object ToggleSign: CalculatorAction()
    data object ToggleAngleMode: CalculatorAction()
    data object ToggleCalculatorMode: CalculatorAction()
    data object ToggleConverterMode: CalculatorAction()
    data object ToggleCurrencyMode: CalculatorAction()
    data object ToggleProgrammerMode: CalculatorAction()
    data class SetTheme(val theme: AppTheme): CalculatorAction()
    data object MemoryClear: CalculatorAction()
    data object MemoryAdd: CalculatorAction()
    data object MemorySubtract: CalculatorAction()
    data object MemoryRecall: CalculatorAction()
    data object ClearHistory: CalculatorAction()
    data class SelectHistoryResult(val result: String): CalculatorAction()
    data class Operation(val operation: String): CalculatorAction()
    data class Scientific(val type: String): CalculatorAction()
    data class Constant(val value: Double): CalculatorAction()
    data class SetConverterCategory(val category: String): CalculatorAction()
    data class SetConverterUnits(val from: String, val to: String): CalculatorAction()
    data class SetCurrencyUnits(val from: String, val to: String): CalculatorAction()
    data class UpdateRates(val rates: Map<String, Double>): CalculatorAction()
}

fun calculatorLogic(state: CalculatorState, action: CalculatorAction): CalculatorState {
    return when(action) {
        is CalculatorAction.Number -> {
            if (state.operation == null) {
                val newNum = if (state.number1 == "0") action.number.toString() else state.number1 + action.number
                state.copy(number1 = newNum)
            } else {
                state.copy(number2 = state.number2 + action.number)
            }
        }
        is CalculatorAction.Operation -> if (state.number1.isNotEmpty()) state.copy(operation = action.operation) else state
        is CalculatorAction.Calculate -> {
            val res = performCalculation(state)
            val expression = formatDisplay(state) + " = " + res
            state.copy(
                number1 = res, 
                number2 = "", 
                operation = null,
                history = state.history + expression
            )
        }
        is CalculatorAction.Clear -> CalculatorState(memory=state.memory, isDegreeMode=state.isDegreeMode, isScientificMode=state.isScientificMode, isConverterMode=state.isConverterMode, isCurrencyMode=state.isCurrencyMode, isProgrammerMode=state.isProgrammerMode, currentTheme=state.currentTheme, history=state.history, rates=state.rates)
        is CalculatorAction.Delete -> {
            if (state.number2.isNotEmpty()) state.copy(number2 = state.number2.dropLast(1))
            else if (state.operation != null) state.copy(operation = null)
            else if (state.number1.length > 1) state.copy(number1 = state.number1.dropLast(1))
            else state.copy(number1 = "0")
        }
        is CalculatorAction.Decimal -> {
            if (state.operation == null && !state.number1.contains(".")) state.copy(number1 = state.number1 + ".")
            else if (state.operation != null && !state.number2.contains(".")) state.copy(number2 = state.number2 + ".")
            else state
        }
        is CalculatorAction.ToggleSign -> {
            if (state.operation == null) state.copy(number1 = if(state.number1.startsWith("-")) state.number1.drop(1) else "-"+state.number1)
            else state.copy(number2 = if(state.number2.startsWith("-")) state.number2.drop(1) else "-"+state.number2)
        }
        is CalculatorAction.Scientific -> applyScientific(state, action.type)
        is CalculatorAction.Constant -> if (state.operation == null) state.copy(number1 = formatResult(action.value)) else state.copy(number2 = formatResult(action.value))
        is CalculatorAction.ToggleAngleMode -> state.copy(isDegreeMode = !state.isDegreeMode)
        is CalculatorAction.ToggleCalculatorMode -> state.copy(isScientificMode = !state.isScientificMode, isConverterMode = false, isCurrencyMode = false, isProgrammerMode = false)
        is CalculatorAction.ToggleConverterMode -> state.copy(isConverterMode = !state.isConverterMode, isScientificMode = false, isCurrencyMode = false, isProgrammerMode = false)
        is CalculatorAction.ToggleCurrencyMode -> state.copy(isCurrencyMode = !state.isCurrencyMode, isScientificMode = false, isConverterMode = false, isProgrammerMode = false)
        is CalculatorAction.ToggleProgrammerMode -> state.copy(isProgrammerMode = !state.isProgrammerMode, isScientificMode = false, isConverterMode = false, isCurrencyMode = false)
        is CalculatorAction.SetTheme -> state.copy(currentTheme = action.theme)
        is CalculatorAction.MemoryClear -> state.copy(memory = 0.0)
        is CalculatorAction.MemoryAdd -> state.copy(memory = state.memory + (state.number1.toDoubleOrNull() ?: 0.0))
        is CalculatorAction.MemorySubtract -> state.copy(memory = state.memory - (state.number1.toDoubleOrNull() ?: 0.0))
        is CalculatorAction.MemoryRecall -> state.copy(number1 = formatResult(state.memory))
        is CalculatorAction.ClearHistory -> state.copy(history = emptyList())
        is CalculatorAction.SelectHistoryResult -> if (state.operation == null) state.copy(number1 = action.result, number2 = "", operation = null) else state.copy(number2 = action.result)
        is CalculatorAction.SetConverterCategory -> state.copy(converterCategory = action.category, number1 = "0")
        is CalculatorAction.SetConverterUnits -> state.copy(fromUnit = action.from, toUnit = action.to)
        is CalculatorAction.SetCurrencyUnits -> state.copy(fromCurrency = action.from, toCurrency = action.to)
        is CalculatorAction.UpdateRates -> state.copy(rates = action.rates)
    }
}

fun applyScientific(state: CalculatorState, type: String): CalculatorState {
    val num = (if (state.operation == null) state.number1 else state.number2).toDoubleOrNull() ?: return state
    val result = when(type) {
        "x²" -> num.pow(2); "x³" -> num.pow(3); "eˣ" -> exp(num); "10ˣ" -> 10.0.pow(num); "¹/x" -> 1.0 / num; "²√x" -> sqrt(num); "³√x" -> Math.cbrt(num); "ln" -> ln(num); "log₁₀" -> log10(num); "x!" -> { var f = 1.0; for (i in 1..num.toInt().coerceAtMost(20)) f *= i; f }; "sin" -> sin(if(state.isDegreeMode) Math.toRadians(num) else num); "cos" -> cos(if(state.isDegreeMode) Math.toRadians(num) else num); "tan" -> tan(if(state.isDegreeMode) Math.toRadians(num) else num); "sinh" -> sinh(num); "cosh" -> cosh(num); "tanh" -> tanh(num); "%" -> num / 100.0; else -> null
    }
    if (result != null) {
        val resStr = formatResult(result)
        return state.copy(number1 = resStr, number2 = "", operation = null, history = state.history + "$type($num) = $resStr")
    }
    return when(type) { "xʸ" -> state.copy(operation = "^"); "ʸ√x" -> state.copy(operation = "yroot"); else -> state }
}

fun performCalculation(state: CalculatorState): String {
    val n1 = state.number1.toDoubleOrNull() ?: 0.0
    val n2 = state.number2.toDoubleOrNull() ?: return state.number1
    val res = when(state.operation) { "+" -> n1 + n2; "-" -> n1 - n2; "*" -> n1 * n2; "/" -> if(n2!=0.0) n1/n2 else Double.NaN; "^" -> n1.pow(n2); "yroot" -> n1.pow(1.0/n2); else -> n1 }
    return formatResult(res)
}

fun formatResult(d: Double): String = if(d.isNaN()) "Hata" else String.format(Locale.US, "%.6f", d).trimEnd('0').trimEnd('.')

fun formatDisplay(state: CalculatorState): String = state.number1 + (state.operation ?: "") + state.number2

fun performUnitConversion(state: CalculatorState): String {
    val v = state.number1.toDoubleOrNull() ?: 0.0
    val result = when(state.converterCategory) {
        "Uzunluk" -> { val m = when(state.fromUnit) { "Metre" -> v; "Kilometre" -> v * 1000.0; "Mil" -> v * 1609.34; "Ayak" -> v * 0.3048; else -> v }; when(state.toUnit) { "Metre" -> m; "Kilometre" -> m / 1000.0; "Mil" -> m / 1609.34; "Ayak" -> m / 0.3048; else -> m } }
        "Ağırlık" -> { val g = when(state.fromUnit) { "Gram" -> v; "Kilogram" -> v * 1000.0; "Libre" -> v * 453.592; "Ons" -> v * 28.3495; else -> v }; when(state.toUnit) { "Gram" -> g; "Kilogram" -> g / 1000.0; "Libre" -> g / 453.592; "Ons" -> g / 28.3495; else -> g } }
        "Sıcaklık" -> { val c = when(state.fromUnit) { "Celsius" -> v; "Fahrenheit" -> (v - 32) * 5/9; "Kelvin" -> v - 273.15; else -> v }; when(state.toUnit) { "Celsius" -> c; "Fahrenheit" -> (c * 9/5) + 32; "Kelvin" -> c + 273.15; else -> c } }
        "Veri" -> { val b = when(state.fromUnit) { "Byte" -> v; "Kilobyte" -> v * 1024.0; "Megabyte" -> v * 1024.0.pow(2); "Gigabyte" -> v * 1024.0.pow(3); else -> v }; when(state.toUnit) { "Byte" -> b; "Kilobyte" -> b / 1024.0; "Megabyte" -> b / 1024.0.pow(2); "Gigabyte" -> b / 1024.0.pow(3); else -> b } }
        else -> v
    }
    return formatResult(result)
}

fun performCurrencyConversion(state: CalculatorState): String {
    val v = state.number1.toDoubleOrNull() ?: 0.0
    // Canlı kurlar üzerinden hesaplama (Base: USD)
    val fromRate = state.rates[state.fromCurrency] ?: 1.0
    val toRate = state.rates[state.toCurrency] ?: 1.0
    val result = (v / fromRate) * toRate
    return formatResult(result)
}

@Preview(showBackground = true)
@Composable
fun CalculatorPreview() { HesapMakinesiTheme { CalculatorScreen() } }
