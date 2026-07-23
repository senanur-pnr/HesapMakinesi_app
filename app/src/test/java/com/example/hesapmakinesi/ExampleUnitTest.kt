package com.example.hesapmakinesi

import org.junit.Test
import org.junit.Assert.*

class ExampleUnitTest {
    
    @Test
    fun test_addition() {
        val state = CalculatorState(number1 = "10", number2 = "5", operation = "+")
        val result = performCalculation(state)
        assertEquals("15", result)
    }

    @Test
    fun test_subtraction() {
        val state = CalculatorState(number1 = "10", number2 = "5", operation = "-")
        val result = performCalculation(state)
        assertEquals("5", result)
    }

    @Test
    fun test_multiplication() {
        val state = CalculatorState(number1 = "10", number2 = "5", operation = "*")
        val result = performCalculation(state)
        assertEquals("50", result)
    }

    @Test
    fun test_division() {
        val state = CalculatorState(number1 = "10", number2 = "2", operation = "/")
        val result = performCalculation(state)
        assertEquals("5", result)
    }

    @Test
    fun test_division_by_zero() {
        val state = CalculatorState(number1 = "10", number2 = "0", operation = "/")
        val result = performCalculation(state)
        assertEquals("Hata", result)
    }

    @Test
    fun test_unit_conversion_length() {
        val state = CalculatorState(
            number1 = "1000",
            converterCategory = "Uzunluk",
            fromUnit = "Metre",
            toUnit = "Kilometre"
        )
        val result = performUnitConversion(state)
        assertEquals("1", result)
    }

    @Test
    fun test_unit_conversion_weight() {
        val state = CalculatorState(
            number1 = "1",
            converterCategory = "Ağırlık",
            fromUnit = "Kilogram",
            toUnit = "Gram"
        )
        val result = performUnitConversion(state)
        assertEquals("1000", result)
    }

    @Test
    fun test_currency_conversion() {
        // Based on rates in performCurrencyConversion: USD = 34.25, TRY = 1.0
        val state = CalculatorState(
            number1 = "1",
            fromCurrency = "USD",
            toCurrency = "TRY"
        )
        val result = performCurrencyConversion(state)
        assertEquals("34.25", result)
    }

    @Test
    fun test_calculator_logic_number_input() {
        var state = CalculatorState(number1 = "0")
        state = calculatorLogic(state, CalculatorAction.Number(5))
        assertEquals("5", state.number1)
        
        state = calculatorLogic(state, CalculatorAction.Number(2))
        assertEquals("52", state.number1)
    }

    @Test
    fun test_calculator_logic_clear() {
        val state = CalculatorState(number1 = "123", operation = "+", number2 = "456")
        val newState = calculatorLogic(state, CalculatorAction.Clear)
        assertEquals("0", newState.number1)
        assertEquals("", newState.number2)
        assertNull(newState.operation)
    }
}
