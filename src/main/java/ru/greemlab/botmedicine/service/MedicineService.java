package ru.greemlab.botmedicine.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.greemlab.botmedicine.dto.MedicineResponse;
import ru.greemlab.botmedicine.dto.MedicineResponse.MedicineViewList;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedicineService {

    private final RestTemplate restTemplate;

    @Value("${medicine.api.url}")
    private String baseUrl;

    public List<MedicineViewList> getAllMedicines() {
        List<MedicineViewList> all = new ArrayList<>();
        String nextUrl = baseUrl;

        while (nextUrl != null) {
            MedicineResponse response = fetchPage(nextUrl);
            if (response != null
                && response._embedded() != null
                && response._embedded().medicineViewList() != null) {
                all.addAll(response._embedded().medicineViewList());
            }
            nextUrl = extractNextLink(response);
        }
        return all;
    }

    public List<MedicineViewList> getRedMedicines() {
        return getAllMedicines().stream()
                .filter(m -> "red".equalsIgnoreCase(m.color()))
                .toList();
    }

    private MedicineResponse fetchPage(String url) {
        try {
            var secureUrl = url.replaceFirst("^http://", "https://");
            log.info("Fetching medicines from URL: {}", url);
            var response = restTemplate.getForEntity(secureUrl, MedicineResponse.class);
            return response.getBody();
        } catch (Exception ex) {
            log.error("Ошибка при запросе к {}: {}", url, ex.getMessage());
            return null;
        }
    }

    private String extractNextLink(MedicineResponse response) {
        if (response != null && response._links() != null) {
            MedicineResponse.Link nextLink = response._links().next();
            if (nextLink != null && nextLink.href() != null && !nextLink.href().isBlank()) {
                return nextLink.href();
            }
        }
        return null;
    }
}
