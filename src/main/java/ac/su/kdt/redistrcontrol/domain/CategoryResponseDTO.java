package ac.su.kdt.redistrcontrol.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter @Setter
@AllArgsConstructor
public class CategoryResponseDTO {
    private String id;
    private String name;
    private int depth;  // 최상위 1, 그 아래부터 2, 3, 4, ...

    // 순환참조 해결 기본 전략 #################
    // -> 순환참조가 일어나는 필드를 포함하지 않게끔
    //    1) Long (id 값)
    //    2) String (toString() 호출값)
    //    3)Map<>
    // 등으로 핸들링 #########################

    // 전략 1) 가장 단순한 형태로 참조 문제 해결하기 (객체 사용 포기)
    private Long parentId;  // 상위 카테고리 객체 타입이 아니라 id 로만 다루기

    public static CategoryResponseDTO fromCategory(Category category) {
        return new CategoryResponseDTO(
            "/categories/" + category.getId(),
            category.getName(),
            category.getDepth(),
            category.getParent() != null ? category.getParent().getId() : null,
            null
        );
    }

    // 전략 2) 단일 String 값으로 다루기 -> 예제 생략

    // 전략 3) Entity 이름과 URL 을 포함하는 Map<> 자료형으로 다루기
    private List<Map<String, String>> parentsUrl;

    public static CategoryResponseDTO fromCategoryWithParentsUrlList(Category category) {
        return new CategoryResponseDTO(
            "/categories/" + category.getId(),
            category.getName(),
            category.getDepth(),
            category.getParent() != null ? category.getParent().getId() : null,
            category.getParentsMapAsc()
        );
    }
}