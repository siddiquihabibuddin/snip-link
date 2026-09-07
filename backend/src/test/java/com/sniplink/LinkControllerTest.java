package com.sniplink;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "app.base-url=http://short.test")
@AutoConfigureMockMvc
class LinkControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper json;

    @Test
    void createReturns201WithShortUrl() throws Exception {
        mvc.perform(post("/api/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"url": "https://example.com/some/long/path"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").isNotEmpty())
                .andExpect(jsonPath("$.url").value("https://example.com/some/long/path"))
                .andExpect(jsonPath("$.shortUrl").value(org.hamcrest.Matchers.startsWith("http://short.test/")));
    }

    @Test
    void redirectsToOriginalUrl() throws Exception {
        String code = createLink("https://example.com/redirect-target");

        mvc.perform(get("/{code}", code))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://example.com/redirect-target"));
    }

    @Test
    void unknownCodeReturns404() throws Exception {
        mvc.perform(get("/{code}", "nope404"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listContainsCreatedLink() throws Exception {
        createLink("https://example.com/listed");

        mvc.perform(get("/api/links"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(org.hamcrest.Matchers.greaterThanOrEqualTo(1))));
    }

    @Test
    void rejectsInvalidUrlWith400() throws Exception {
        mvc.perform(post("/api/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"url": "not-a-real-url"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void rejectsBlankUrlWith400() throws Exception {
        mvc.perform(post("/api/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"url": "   "}"""))
                .andExpect(status().isBadRequest());
    }

    private String createLink(String url) throws Exception {
        String body = mvc.perform(post("/api/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new LinkController.CreateLinkRequest(url))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode node = json.readTree(body);
        return node.get("code").asText();
    }
}
