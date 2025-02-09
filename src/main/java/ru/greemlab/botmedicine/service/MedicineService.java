package ru.greemlab.botmedicine.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.greemlab.botmedicine.dto.MedicineResponse;
import ru.greemlab.botmedicine.dto.MedicineResponse.MedicineViewList;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedicineService {

    private final WebClient webClient;

    @Value("${medicine.api.url}")
    private String url;

    public Flux<MedicineViewList> getAllMedicines() {
        return fetchPage(url)
                .expand(response -> {
                    var nextUrl = extractNextLink(response);
                    if (nextUrl == null) {
                        return Mono.empty();
                    }
                    return fetchPage(nextUrl);
                })
                .flatMap(response -> {
                   if (response._embedded() == null ||
                       response._embedded().medicineViewList() == null) {
                      return Flux.empty();
                   }
                   return Flux.fromIterable(response._embedded().medicineViewList());
                });
    }

    public Flux<MedicineViewList> getRedMedicines() {
        return getAllMedicines()
                .filter(m -> "red".equalsIgnoreCase(m.color()));
    }

    private Mono<MedicineResponse> fetchPage(String url) {
        log.info("Fetching medicines from URL: {}", url);
        return webClient.get()
                .uri(url)
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        Mono.error(new RuntimeException("HTTP error: " + clientResponse.statusCode())))
                .bodyToMono(MedicineResponse.class)
                .timeout(Duration.ofSeconds(10))
                .doOnError(ex -> log.error("Ошибка при запросе к {}: {}", url, ex.toString()));
    }

    private String extractNextLink(MedicineResponse response) {
        if (response == null || response._links() == null) {
            return null;
        }
        var nextLink = response._links().next();
        if (nextLink != null && nextLink.href() != null && !nextLink.href().isBlank()) {
            return nextLink.href();
        }
        return null;
    }
}
