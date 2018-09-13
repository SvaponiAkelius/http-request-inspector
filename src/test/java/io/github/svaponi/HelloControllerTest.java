package io.github.svaponi;

import io.github.svaponi.resource.AnyResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("unit-test")
public class HelloControllerTest {

    private final HelloController controller;

    public HelloControllerTest() {
        this.controller = new HelloController();
    }

    @Test
    public void root() throws Exception {
        final HttpEntity<?> response = controller.root(null, null, null);
        assertThat(response.getBody(), instanceOf(AnyResource.class));
    }
}