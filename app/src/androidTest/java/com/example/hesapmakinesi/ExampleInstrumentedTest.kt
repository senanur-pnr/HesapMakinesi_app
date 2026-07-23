package com.example.hesapmakinesi

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun test_initial_display_is_zero() {
        // Başlangıçta ekranda "0" yazdığını doğrula
        composeTestRule.onNodeWithText("0").assertExists()
    }

    @Test
    fun test_button_click_updates_display() {
        // "7" butonunu bul ve tıkla (Birden fazla 7 varsa buton olanı seç)
        composeTestRule.onNode(hasText("7") and hasClickAction()).performClick()
        
        // Ekranda "7" yazdığını doğrula
        composeTestRule.onNodeWithText("7").assertExists()
    }

    @Test
    fun test_addition_interaction() {
        // 5 + 3 = 8 işlemini simüle et
        composeTestRule.onNode(hasText("5") and hasClickAction()).performClick()
        composeTestRule.onNode(hasText("+") and hasClickAction()).performClick()
        composeTestRule.onNode(hasText("3") and hasClickAction()).performClick()
        composeTestRule.onNode(hasText("=") and hasClickAction()).performClick()
        
        // Sonucun "8" olduğunu doğrula
        composeTestRule.onNodeWithText("8").assertExists()
    }

    @Test
    fun test_clear_button() {
        // Sayı yaz ve AC butonuna bas
        composeTestRule.onNode(hasText("9") and hasClickAction()).performClick()
        composeTestRule.onNode(hasText("AC") and hasClickAction()).performClick()
        
        // Ekranın tekrar "0" olduğunu doğrula
        composeTestRule.onNodeWithText("0").assertExists()
    }
}
