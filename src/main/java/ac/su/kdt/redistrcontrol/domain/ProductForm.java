package ac.su.kdt.redistrcontrol.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class ProductForm {
    // Form 은 데이터 수신 용도의 DTO 로서 오해가 가장 적은 네이밍 컨벤션으로 백엔드 에서도 폭넓게 쓰임
    // Form + Serializer 쌍을 이루어서 REST API 응답에 많이 사용합니다.
    // 이때, Serializing 동작은 JSON String 으로 변환 - DeSerializing 동작은 객체로 다시 파싱 하는 동작입니다.
    private Long id;
    private String name;
    private int price;
    private int stockQuantity;
    private int salesQuantity;
    private Long categoryId;

    // DTO 생성 시에는 Entity 와의 객체 교환 부분을 함께 구현
    public static ProductForm fromProduct(Product product) {
        return new ProductForm(
            product.getId(),
            product.getName(),
            product.getPrice(),
            product.getStockQuantity(),
            product.getSalesQuantity(),
            product.getCategory().getId()
        );
    }

    public Product toEntity() {
        Category category = new Category();
        category.setId(this.getCategoryId());
        return new Product(
            this.getId(),
            this.getName(),
            this.getPrice(),
            this.getStockQuantity(),
            this.getSalesQuantity(),
            category
        );
    }

}
