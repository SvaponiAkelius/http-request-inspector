package io.github.svaponi;

import io.github.svaponi.resource.AnyResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration-test")
public class HelloControllerIT {

    @Autowired
    private TestRestTemplate template;

    @Test
    public void getAnyPathShouldReply() throws Exception {
        final ResponseEntity<AnyResource> response = template.getForEntity("/foo", AnyResource.class);
        assertThat(response.getBody().fields(), hasEntry(HelloController.REQUEST_METHOD, "GET"));
        assertThat(response.getBody().fields(), hasEntry(HelloController.REQUEST_URI, "/foo"));
    }

    @Test
    public void postAnyPathShouldReply() throws Exception {
        final Object body = new AnyResource().withField("foo", "bar").fields();
        final ResponseEntity<AnyResource> response = template.postForEntity("/foo", body, AnyResource.class);
        assertThat(response.getBody().fields(), hasEntry(HelloController.REQUEST_METHOD, "POST"));
        assertThat(response.getBody().fields(), hasEntry(HelloController.REQUEST_URI, "/foo"));
        assertThat(response.getBody().fields(), hasEntry(HelloController.BODY, body));
    }
}