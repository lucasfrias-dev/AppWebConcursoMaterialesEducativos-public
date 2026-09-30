package ar.edu.unnoba.appweb_concurso_materiales_educativos.utils;

import java.util.Map;

/**
 * Clase utilitaria para convertir números enteros a romanos y viceversa.
 */
public class RomanNumerals {
    private static final Map<Character, Integer> values = Map.of(
        'I', 1,
        'V', 5,
        'X', 10,
        'L', 50,
        'C', 100,
        'D', 500,
        'M', 1000
    );

    // Arreglos que contienen los números y letras romanas en orden descendente.
    private static final int[] numbers = { 1000,  900,  500,  400,  100,   90,   50,   40,   10,    9,    5,    4,    1 };
    private static final String[] letters = { "M",  "CM",  "D",  "CD", "C",  "XC", "L",  "XL", "X",  "IX", "V",  "IV", "I" };

    /**
     * Convierte un número romano en su equivalente entero.
     *
     * @param roman El número romano a convertir.
     * @return El equivalente entero del número romano.
     */
    public static int toInteger(String roman) {
        int result = 0;
        for (int i = 0; i < roman.length(); i++) {
            if (i > 0 && values.get(roman.charAt(i)) > values.get(roman.charAt(i - 1))) {
                // Si el valor numérico de la letra actual es mayor que el de la anterior,
                // se resta dos veces el valor de la letra anterior del valor total.
                result += values.get(roman.charAt(i)) - 2 * values.get(roman.charAt(i - 1));
            } else {
                // De lo contrario, se suma el valor numérico de la letra al resultado.
                result += values.get(roman.charAt(i));
            }
        }
        return result;
    }

    /**
     * Convierte un número entero en su equivalente romano.
     *
     * @param number El número entero a convertir.
     * @return El equivalente romano del número entero.
     */
    public static String convert(int number) {
        StringBuilder roman = new StringBuilder();
        for (int i = 0; i < numbers.length; i++) {
            while (number >= numbers[i]) {
                // Agrega la letra romana al resultado y resta su valor numérico del número dado.
                roman.append(letters[i]);
                number -= numbers[i];
            }
        }
        return roman.toString();
    }
}

