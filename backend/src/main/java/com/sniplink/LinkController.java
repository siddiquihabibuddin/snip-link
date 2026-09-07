package com.sniplink;

import java.net.URI;
import java.time.Instant;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LinkController {

    private final LinkStore store;
    private final ShortCodeGenerator codeGenerator;
    private final UrlValidator urlValidator;
    private final String baseUrl;

    public LinkController(LinkStore store,
                          ShortCodeGenerator codeGenerator,
                          UrlValidator urlValidator,
                          @Value("${app.base-url:http://localhost:8080}") String baseUrl) {
        this.store = store;
        this.codeGenerator = codeGenerator;
        this.urlValidator = urlValidator;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    @PostMapping("/api/links")
    public ResponseEntity<LinkResponse> create(@Valid @RequestBody CreateLinkRequest request) {
        String url = request.url().trim();
        if (!urlValidator.isValid(url)) {
            throw new InvalidUrlException("url must be an absolute http(s) URL");
        }
        String code = codeGenerator.generateUnique(store::exists);
        Link link = store.save(code, url);
        LinkResponse body = toResponse(link);
        return ResponseEntity.created(URI.create(body.shortUrl())).body(body);
    }

    @GetMapping("/api/links")
    public List<LinkResponse> list() {
        return store.findAll().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{code}")
    public ResponseEntity<Void> redirect(@PathVariable String code) {
        return store.find(code)
                .map(link -> ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create(link.url()))
                        .<Void>build())
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private LinkResponse toResponse(Link link) {
        return new LinkResponse(link.code(), link.url(), baseUrl + "/" + link.code(), link.createdAt());
    }

    public record CreateLinkRequest(@NotBlank String url) {
    }

    public record LinkResponse(String code, String url, String shortUrl, Instant createdAt) {
    }

    static class InvalidUrlException extends RuntimeException {
        InvalidUrlException(String message) {
            super(message);
        }
    }
}
