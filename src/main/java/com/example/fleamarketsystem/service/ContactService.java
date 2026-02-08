package com.example.fleamarketsystem.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.fleamarketsystem.entity.Contact;
import com.example.fleamarketsystem.entity.User;
import com.example.fleamarketsystem.repository.ContactRepository;

@Service
public class ContactService {

	private final ContactRepository contactRepository;

	public ContactService(ContactRepository contactRepository) {
		this.contactRepository = contactRepository;
	}

	@Transactional
	public Contact saveContact(User user, String subject, String message) {
		Contact contact = new Contact();
		contact.setUser(user);
		contact.setSubject(subject);
		contact.setMessage(message);
		return contactRepository.save(contact);
	}

	public List<Contact> getAllContacts() {
		return contactRepository.findAllByOrderByCreatedAtDesc();
	}

	public long getUnreadCount() {
		return contactRepository.countByReadFalse();
	}

	@Transactional
	public void markAsRead(Long contactId) {
		Contact contact = contactRepository.findById(contactId)
				.orElseThrow(() -> new IllegalArgumentException("お問い合わせが見つかりません。"));
		contact.setRead(true);
		contactRepository.save(contact);
	}
}
