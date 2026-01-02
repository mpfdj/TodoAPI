package jaeger.de.miel.TodoAPI.snippets;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;

public class snippetTest {

    @Test
    public void test() {
        Date date = Date.from(Instant.now().plusSeconds(3600));
        System.out.println(date);
    }
}
