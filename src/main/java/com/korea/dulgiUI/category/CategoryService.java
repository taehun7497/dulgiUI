package com.korea.dulgiUI.category;

import com.korea.dulgiUI.error.DataNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository; // 카테고리 리포지토리

    public Category getCategoryByTitle(String title) {
        Optional<Category> category = this.categoryRepository.findByTitle(title); // 제목으로 카테고리를 조회합니다.

        if (category.isEmpty()) { // 카테고리가 없는 경우
            category = Optional.ofNullable(create(title)); // 새로운 카테고리를 생성합니다.
        }

        if (category.isPresent()) { // 카테고리가 존재하는 경우
            return category.get(); // 카테고리를 반환합니다.
        } else { // 카테고리가 없는 경우
            throw new DataNotFoundException("question Not Found"); // 데이터를 찾을 수 없다는 예외를 던집니다.
        }
    }

    public Category create(String title) {
        Category category = new Category(); // 새로운 카테고리 객체를 생성합니다.
        category.setTitle(title); // 제목을 설정합니다.
        category.setCreateDate(LocalDateTime.now()); // 생성일을 설정합니다.
        this.categoryRepository.save(category); // 카테고리를 저장합니다.
        return category; // 생성된 카테고리 객체를 반환합니다.
    }
}