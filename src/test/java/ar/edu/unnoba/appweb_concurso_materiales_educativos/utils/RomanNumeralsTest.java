package ar.edu.unnoba.appweb_concurso_materiales_educativos.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RomanNumeralsTest {

    @Test
    void testBasicNumbers() {
        assertEquals("I", RomanNumerals.convert(1));
        assertEquals("IV", RomanNumerals.convert(4));
        assertEquals("V", RomanNumerals.convert(5));
        assertEquals("IX", RomanNumerals.convert(9));
        assertEquals("X", RomanNumerals.convert(10));
    }

    @Test
    void testLargerNumbers() {
        assertEquals("XL", RomanNumerals.convert(40));
        assertEquals("L", RomanNumerals.convert(50));
        assertEquals("XC", RomanNumerals.convert(90));
        assertEquals("C", RomanNumerals.convert(100));
        assertEquals("CD", RomanNumerals.convert(400));
        assertEquals("D", RomanNumerals.convert(500));
        assertEquals("CM", RomanNumerals.convert(900));
        assertEquals("M", RomanNumerals.convert(1000));
        assertEquals("MMXXIV", RomanNumerals.convert(2024));
    }

    @Test
    void testToInteger() {
        assertEquals(1, RomanNumerals.toInteger("I"));
        assertEquals(4, RomanNumerals.toInteger("IV"));
        assertEquals(10, RomanNumerals.toInteger("X"));
        assertEquals(2024, RomanNumerals.toInteger("MMXXIV"));
    }
}
