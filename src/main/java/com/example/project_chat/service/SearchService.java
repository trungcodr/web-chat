package com.example.project_chat.service;

import com.example.project_chat.dto.search.SearchResultDTO;

public interface SearchService {
    SearchResultDTO search(String keyword);
}
