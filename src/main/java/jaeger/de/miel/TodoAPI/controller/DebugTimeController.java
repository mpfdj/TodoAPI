package jaeger.de.miel.TodoAPI.controller;

import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
public class DebugTimeController {

    @GetMapping("/time")
    public Map<String, String> getServerTime() {
        Map<String, String> times = new HashMap<>();

        Instant instant = Instant.now();
        Date date = new Date();

        times.put("Instant.now()", instant.toString());
        times.put("new Date()", date.toString());
        times.put("System.currentTimeMillis()", String.valueOf(System.currentTimeMillis()));
        times.put("LocalDateTime.now(UTC)", LocalDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        times.put("LocalDateTime.now(SystemDefault)", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        times.put("Default Zone", ZoneId.systemDefault().toString());

        // Test JWT time generation
        times.put("Date.from(Instant.now())", Date.from(instant).toString());

        return times;
    }
}