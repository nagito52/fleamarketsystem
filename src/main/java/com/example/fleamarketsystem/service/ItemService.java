package com.example.fleamarketsystem.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.fleamarketsystem.entity.Item;
import com.example.fleamarketsystem.entity.User;
import com.example.fleamarketsystem.repository.ItemRepository;

@Service
public class ItemService {

	private final ItemRepository itemRepository;
	private final CloudinaryService cloudinaryService;

	public ItemService(ItemRepository itemRepository, CloudinaryService cloudinaryService) {
		this.itemRepository = itemRepository;
		this.cloudinaryService = cloudinaryService;
	}

	public Page<Item> searchItems(String keyword, Long categoryId, int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		if (keyword != null && !keyword.isEmpty() && categoryId != null) {
			return itemRepository.findByNameContainingIgnoreCaseAndCategoryIdAndStatus(keyword, categoryId, "出品中",
					pageable);
		} else if (keyword != null && !keyword.isEmpty()) {
			return itemRepository.findByNameContainingIgnoreCaseAndStatus(keyword, "出品中", pageable);
		} else if (categoryId != null) {
			return itemRepository.findByCategoryIdAndStatus(categoryId, "出品中", pageable);
		} else {
			return itemRepository.findByStatus("出品中", pageable);
		}
	}

	public List<Item> getAllItems() {
		return itemRepository.findAll();
	}

	public Optional<Item> getItemById(Long id) {
	    return itemRepository.findById(id);
	}

	// 管理者削除チェック用の新しいメソッドを追加
	public Item getItemByIdOrThrow(Long id) {
	    return itemRepository.findById(id)
	            .orElseThrow(() -> new IllegalArgumentException("指定された商品が見つかりません。ID: " + id));
	}

	public Item saveItem(Item item, MultipartFile imageFile) throws IOException {
		if (imageFile != null && !imageFile.isEmpty()) {
			String imageUrl = cloudinaryService.uploadFile(imageFile);
			item.setImageUrl(imageUrl);
		}
		return itemRepository.save(item);
	}

	public void deleteItem(Long id) {
		itemRepository.findById(id).ifPresent(item -> {
			if (item.getImageUrl() != null) {
				try {
					cloudinaryService.deleteFile(item.getImageUrl());
				} catch (IOException e) {
					System.err.println("Failed to delete image from Cloudinary: " + e.getMessage());
				}
			}
			itemRepository.deleteById(id);
		});
	}
	
	public List<Item> getItemsBySeller(User seller) {
		return itemRepository.findBySeller(seller);
	}
	
	public void markItemAsSold(Long itemId) {
		itemRepository.findById(itemId).ifPresent(item -> {
			item.setStatus("売却済");
			itemRepository.save(item);
		});
	}
	
	@Transactional
	public void updateItemStatus(Long itemId, String status) {
	    Item item = itemRepository.findById(itemId)
	            .orElseThrow(() -> new IllegalArgumentException("Item not found"));
	    item.setStatus(status);
	    itemRepository.save(item);
	}
	
	public List<Item> getRecentItemsForAdmin() {
	    // ページ指定なしで全件取得し、新しい順に並べる（あるいはRepositoryにTop5等のメソッドを作る）
	    // 今回はシンプルに全件取得のリストから最新5件などのロジックをコントローラーで使うか、
	    // Repositoryに専用メソッドを作るのが綺麗です。
	    return itemRepository.findAll(); 
	}
}
