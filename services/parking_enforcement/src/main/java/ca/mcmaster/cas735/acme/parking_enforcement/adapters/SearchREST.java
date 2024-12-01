package ca.mcmaster.cas735.acme.parking_enforcement.adapters;

import ca.mcmaster.cas735.acme.parking_enforcement.dto.MemberDTO;
import ca.mcmaster.cas735.acme.parking_enforcement.dto.SearchRequestDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

@Component
public class SearchREST {

    private final RestTemplate restTemplate;

    @Value("${search.url}")
    private String searchRequestUrl;

    public SearchREST(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public MemberDTO lookupByMemberId(SearchRequestDTO request) {
        return restTemplate.postForObject(
                searchRequestUrl + "/api/member/search",
                request,
                MemberDTO.class
        );
    }
}