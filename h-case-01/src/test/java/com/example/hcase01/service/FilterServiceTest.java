package com.example.hcase01.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class FilterServiceTest {

    private final FilterService filterService = new FilterService();

    @Test
    void shouldFilterSuccessfully() {
        String[][] input = {
                {"0", "s1", null, "35", "90", "60"},
                {"ttt", null, null, "15"},
                {"75", "95", "0", "0", null, "ssss", "0", "-15"},
                {"25", "fgdfg", "", "105", "dsfdsf", "-5"}
        };

        List<Integer> result = filterService.filter(input);
        List<Integer> expected = Arrays.asList(0, 35, 90, 60, 15, 75, 95, 0, 0);
        Assertions.assertEquals(expected, result);
    }

    @Test
    void shouldLeftoverAllElements() {
        String[][] input = {
                {"10", "20", "30", "40", "50"}
        };

        List<Integer> result = filterService.filter(input);
        Assertions.assertEquals(Collections.emptyList(), result);
    }

    @Test
    void shouldLeftoverLastTwoElements() {
        String[][] input = {
                {"30", "30", "30", "99","130"}
        };

        List<Integer> result = filterService.filter(input);
        List<Integer> expected = Arrays.asList(30, 30, 30);
        Assertions.assertEquals(expected, result);
    }

}
