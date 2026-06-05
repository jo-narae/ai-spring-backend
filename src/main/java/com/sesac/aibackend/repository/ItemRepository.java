package com.sesac.aibackend.repository;

import com.sesac.aibackend.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
}
