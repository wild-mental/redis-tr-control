package ac.su.kdt.redistrcontrol.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
// NoArgsConstructor
// : Entity 로 Spring Persistent 라이브러리에서 요구하는 사항
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
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
