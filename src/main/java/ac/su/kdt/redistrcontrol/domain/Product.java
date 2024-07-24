package ac.su.kdt.redistrcontrol.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class Product {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private int price;

    // 재고량 및 판매량
    private int stockQuantity;
    private int salesQuantity;

    // 카테고리
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
}
