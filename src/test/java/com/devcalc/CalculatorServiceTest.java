package com.devcalc;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CalculatorServiceTest {

    private final CalculatorService calculatorService = new CalculatorService();

    @Test
    public void testAdd() {
        assertEquals(15, calculatorService.add(10, 5));
    }

    @Test
    public void testSubtract() {
        assertEquals(5, calculatorService.subtract(10, 5));
    }

    @Test
    public void testMultiply() {
        assertEquals(50, calculatorService.multiply(10, 5));
    }

    @Test
    public void testDivide() {
        assertEquals(2, calculatorService.divide(10, 5));
    }

    @Test
    public void testDivideByZero() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            calculatorService.divide(10, 0);
        });
        assertEquals("Division by zero", exception.getMessage());
    }
    @Test
    public void failingTest() {
        assertEquals(1, 2, "Teste falho proposital");
    }
}
