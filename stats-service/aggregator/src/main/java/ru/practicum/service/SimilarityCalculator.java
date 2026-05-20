package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SimilarityCalculator {

    public double calculate(
            double minSum,
            double sumA,
            double sumB
    ) {
        if (sumA == 0 || sumB == 0) {
            return 0.0;
        }

        return minSum /
               (Math.sqrt(sumA) * Math.sqrt(sumB));
    }
}
