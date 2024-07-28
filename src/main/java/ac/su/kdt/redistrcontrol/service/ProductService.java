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

    // create 동작 리턴은 Optional 이 아님!
    public Product createProduct(Product product) {
        // 일반적으로 생성(create) 연산에서는 Optional 을 사용하지 않는 것이 더 적합
        // - 생성 연산은 대부분 성공할 것으로 예상
        // - save 메서드는 정상적인 상황에서 null 을 반환하지 않음
        // - 실제로 문제가 발생한다면 null 이 아니라 주로 예외가 발생 (예: 데이터베이스 연결 실패)
        try {
            return productRepository.save(product);
        } catch (Exception e) {
            // 예외 처리 로직
            // 즉 create 에서는 Optional 적용보다 예외 처리가 더 적합
            throw new RuntimeException(e);
        }
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
