package ru.practicum.service.category;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.model.Category;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {

        if (categoryRepository.existsByName(newCategoryDto.getName())) {
            throw new ConflictException("Category already exists");
        }

        Category category = categoryMapper.toEntity(newCategoryDto);
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long catId, CategoryDto categoryDto) {

        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category not found"));

        String newName = categoryDto.getName();

        if (categoryRepository.existsByName(newName)
            && !category.getName().equals(newName)) {
            throw new ConflictException("Category already exists");
        }

        category.setName(newName);

        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deleteCategory(Long catId) {
        if (!categoryRepository.existsById(catId)) {
            throw new NotFoundException("Category not found");
        }

        if (eventRepository.existsByCategoryId(catId)) {
            throw new ConflictException("Category is not empty");
        }

        categoryRepository.deleteById(catId);
    }

    @Override
    public List<CategoryDto> getAllCategories(int from, int size) {

        int page = from / size;

        return categoryRepository.findAll(
                        PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"))
                )
                .stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto getCategoryById(Long catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category not found"));
        return categoryMapper.toDto(category);
    }
}