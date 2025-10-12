package com.example.project_chat.controller;

import com.example.project_chat.dto.response.ApiResponse;
import com.example.project_chat.dto.search.SearchResultDTO;
import com.example.project_chat.service.SearchService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
public class SearchController {
    private SearchService searchService;
    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<SearchResultDTO>> search(@RequestParam String keyword) {
        SearchResultDTO result = searchService.search(keyword);
        ApiResponse<SearchResultDTO> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Tìm kiếm thành công!",
                result
        );
        return ResponseEntity.ok(response);
    }
}
