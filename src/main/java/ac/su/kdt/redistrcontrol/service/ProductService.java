package ac.su.kdt.redistrcontrol.service;

import ac.su.kdt.redistrcontrol.domain.Product;
import ac.su.kdt.redistrcontrol.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Product createProduct(Product product) {  // create 동작 리턴은 Optional 이 아님!
        return productRepository.save(product);
    }

    public Optional<Product> updateProduct(Long id, Product product) {
        return productRepository.findById(id)
            .map(existingProduct -> {
                existingProduct.setName(product.getName());
                existingProduct.setPrice(product.getPrice());
                existingProduct.setStockQuantity(product.getStockQuantity());
                existingProduct.setSalesQuantity(product.getSalesQuantity());
                existingProduct.setCategory(product.getCategory());
                return productRepository.save(existingProduct);
            });
    }

    public boolean deleteProduct(Long id) {
        return productRepository.findById(id)
            .map(product -> {
                productRepository.delete(product);
                return true;
            })
            .orElse(false);
    }
}
