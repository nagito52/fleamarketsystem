package com.example.fleamarketsystem.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.fleamarketsystem.entity.Category;
import com.example.fleamarketsystem.repository.CategoryRepository;

@Service
public class CategoryService {
	
	private final CategoryRepository categoryRepository;
	public CategoryService(CategoryRepository categoryRepository) {
		this.categoryRepository = categoryRepository;
	}
	
	public List<Category> getAllCategories() {
		return categoryRepository.findAll();
	}
	
	public Optional<Category> getCategoryById(Long id) {
		return categoryRepository.findById(id);
	}
	
	public Optional<Category> getCategoryByName(String name) {
		return categoryRepository.findByName(name);
	}
	
	public Category saveCategory(Category category) {
		return categoryRepository.save(category);
	}
	
	public void deleteCategory(Long id) {
		categoryRepository.deleteById(id);
	}
}
