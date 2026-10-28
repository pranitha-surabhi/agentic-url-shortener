package com.example.agentic.repository;

import com.example.agentic.models.ClickAnalytics;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Repository
public class AnalyticsRepository {

    private final List<ClickAnalytics> analyticsStore =
            new CopyOnWriteArrayList<>();

    public void save(ClickAnalytics analytics) {

        analyticsStore.add(analytics);
    }

    public List<ClickAnalytics> findByShortCode(String shortCode) {

        return analyticsStore.stream()
                .filter(item -> item.getShortCode().equals(shortCode))
                .collect(Collectors.toList());
    }

    public List<ClickAnalytics> findAll() {

        return List.copyOf(analyticsStore);
    }
}