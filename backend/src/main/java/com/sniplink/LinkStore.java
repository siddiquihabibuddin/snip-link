package com.sniplink;

import java.time.Clock;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Repository;

/**
 * In-memory store of links. Deliberately not backed by a database yet - swapping this
 * for Spring Data JPA + Flyway is one of the planned CI/CD learning exercises.
 */
@Repository
public class LinkStore {

    private final ConcurrentMap<String, Link> byCode = new ConcurrentHashMap<>();
    private final Clock clock;

    public LinkStore() {
        this(Clock.systemUTC());
    }

    LinkStore(Clock clock) {
        this.clock = clock;
    }

    public boolean exists(String code) {
        return byCode.containsKey(code);
    }

    public Link save(String code, String url) {
        Link link = new Link(code, url, clock.instant());
        byCode.put(code, link);
        return link;
    }

    public Optional<Link> find(String code) {
        return Optional.ofNullable(byCode.get(code));
    }

    /** All links, newest first. */
    public List<Link> findAll() {
        return byCode.values().stream()
                .sorted(Comparator.comparing(Link::createdAt).reversed())
                .toList();
    }

    int size() {
        return byCode.size();
    }
}
