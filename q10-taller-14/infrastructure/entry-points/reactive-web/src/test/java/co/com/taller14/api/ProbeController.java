package co.com.taller14.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/usecase/path")
public class ProbeController {

    @GetMapping
    Mono<Void> ok() {
        return Mono.empty();
    }
}
