package com.admin.portal.controller;

import com.admin.portal.entity.ContactMessage;
import com.admin.portal.repository.ContactMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ContactMessageController {

    @Autowired
    private ContactMessageRepository repository;

    @PostMapping("/contact")
    public ResponseEntity<?> createContact(@RequestBody ContactMessage message) {
        ContactMessage saved = repository.save(message);
        return ResponseEntity.ok(saved);
    }

    @GetMapping({"/contact", "/contacts"})
    public List<ContactMessage> getAllContacts() {
        return repository.findAll();
    }

    @DeleteMapping({"/contact/{id}", "/contacts/{id}"})
    public ResponseEntity<?> deleteContact(@PathVariable Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return ResponseEntity.ok("Contact request deleted successfully.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
