package com.example.agentic.repository;

import com.example.agentic.models.ShortUrl;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class ShortUrlRepository {

    private final Map<String, ShortUrl> urlStore = new ConcurrentHashMap<>();

    public ShortUrl save(ShortUrl shortUrl) {

        urlStore.put(shortUrl.getShortCode(), shortUrl);

        return shortUrl;
    }

    public Optional<ShortUrl> findByShortCode(String shortCode) {

        return Optional.ofNullable(urlStore.get(shortCode));
    }

    public Collection<ShortUrl> findAll() {

        return urlStore.values();
    }

    public boolean existsByShortCode(String shortCode) {

        return urlStore.containsKey(shortCode);
    }

    public void delete(String shortCode) {

        urlStore.remove(shortCode);
    }

}