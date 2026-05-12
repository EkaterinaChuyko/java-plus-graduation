package ru.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.event.dto.category.CategoryDto;

@FeignClient(name = "category-service")
public interface CategoryClient {

    @GetMapping("/internal/categories/{id}")
    CategoryDto getCategory(@PathVariable Long id);

    @GetMapping("/internal/categories/{id}/exists")
    Boolean exists(@PathVariable Long id);
}
