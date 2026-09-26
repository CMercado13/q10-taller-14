package co.com.taller14.api;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ApiRest {
//    private final MyUseCase useCase;


    @GetMapping(path = "/usecase/path")
    public Mono<String> commandName() {
//      return useCase.doAction();
        return Mono.just("");
    }
}
