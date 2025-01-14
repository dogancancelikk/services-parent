package com.example.hcase01.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FilterService {
    public List<Integer> filter(String[][] input) {
        if (input == null) {
            return Collections.emptyList();
        }

        List<Integer> allIntegers = Arrays.stream(input)
                .filter(Objects::nonNull)
                .flatMap(row -> Arrays.stream(row)
                        .filter(Objects::nonNull)
                        .map(this::parseToInteger)
                        .filter(Objects::nonNull)
                )
                .collect(Collectors.toList());


        return IntStream.range(0, allIntegers.size() / 3)
                .mapToObj(i -> allIntegers.subList(i * 3, i * 3 + 3))
                .filter(triplet -> triplet.stream().mapToInt(Integer::intValue).sum() >= 90)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    private Integer parseToInteger(String str) {
        try {
            return Integer.valueOf(str);
        } catch (Exception e) {
            return null;
        }
    }
}
