package com.maven.pos.repositories;

import com.maven.pos.entities.Item2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository2 extends JpaRepository<Item2,Long> {
}
