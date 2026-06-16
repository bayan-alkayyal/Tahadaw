package org.example.tahadaw.Repository;

import org.example.tahadaw.Model.SelectedProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SelectedProductRepository extends JpaRepository<SelectedProduct, Long> {

    Optional<SelectedProduct> findSelectedProductById(Long id);
}
