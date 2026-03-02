package com.audition;

import com.audition.integration.AuditionIntegrationClient;
import com.audition.model.AuditionPost;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;


@SpringBootTest
@Slf4j
class AuditionApplicationTests {

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

    @Autowired
    private AuditionIntegrationClient client;

    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = restTemplateBuilder.build();
        mockServer = MockRestServiceServer.bindTo(restTemplate)
                .ignoreExpectOrder(true)
                .build();
        // Re-create client with the SAME RestTemplate
        client = new AuditionIntegrationClient(restTemplate);
    }

    @Test
    void contextLoads() {
        assertThat(client).isNotNull();
    }

    @Test
    void shouldReturnPostsSuccessfully() {
        String mockResponse = """
                [
                  {
                    "userId": 1,
                    "id": 1,
                    "title": "Test Title",
                    "body": "Test Body"
                  }
                ]
                """;

        mockServer.expect(requestTo("https://jsonplaceholder.typicode.com/posts"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(mockResponse, MediaType.APPLICATION_JSON));

        List<AuditionPost> posts = client.getPosts();
        log.info(posts.toString());
        assertThat(posts).hasSize(1);
        assertThat(posts.get(0).getTitle()).isEqualTo("Test Title");

        mockServer.verify();
    }
}
