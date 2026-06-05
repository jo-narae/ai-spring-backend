package com.sesac.aibackend.controller;

import com.sesac.aibackend.domain.Item;
import com.sesac.aibackend.dto.ItemRequest;
import com.sesac.aibackend.dto.ItemResponse;
import com.sesac.aibackend.error.NotFoundException;
import com.sesac.aibackend.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    public List<ItemResponse> list() {
        return itemService.findAll().stream().map(ItemResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ItemResponse get(@PathVariable Long id) {
        Item item = itemService.findById(id)
                .orElseThrow(() -> NotFoundException.of("item", id));
        return ItemResponse.from(item);
    }

    @PostMapping
    public ResponseEntity<ItemResponse> create(@Valid @RequestBody ItemRequest req) {
        Item saved = itemService.save(req.toEntity());
        URI location = URI.create("/items/" + saved.getId());
        return ResponseEntity.created(location).body(ItemResponse.from(saved));
    }

    @PutMapping("/{id}")
    public ItemResponse update(@PathVariable Long id, @Valid @RequestBody ItemRequest req) {
        Item item = itemService.findById(id)
                .orElseThrow(() -> NotFoundException.of("item", id));
        item.setName(req.name());
        item.setPrice(req.price());
        return ItemResponse.from(itemService.save(item));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!itemService.existsById(id)) {
            throw NotFoundException.of("item", id);
        }
        itemService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
