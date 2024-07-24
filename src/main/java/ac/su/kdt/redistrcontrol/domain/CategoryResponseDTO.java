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

    // 1) 가장 단순한 형태로 참조 문제 해결하기 (객체 사용 포기)
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

    // 2) 현재 카테고리에 이르기까지 상위 항목만 리스트로 응답 (객체사용 -> 순환참조 재개됨)
//    private List<Category> parents;
//
//    public static CategoryResponseDTO fromCategoryWithParentsList(Category category) {
//        return new CategoryResponseDTO(
//            category.getId(),
//            category.getName(),
//            category.getDepth(),
//            category.getParent() != null ? category.getParent().getId() : null,
//            category.getParentsAsc(),
//        );
//    }  // 응답 포맷이 깨지므로 사용 불가

    // 3) 리스트 응답시 REST API 설계를 고려해서 응답한 결과로 바로 페이지 호출 가능하도록
    //    URL 형태 (String) 응답 (HyperLink 연속성 추구)
    //    - Depth 를 List index 로 대체 (추가 필드 X)
    //    - 실제 categoryId(후속 요청)를 URL 에 반영 (String)
    //    - 카테고리 이름도 반환 (String)
    private List<Map<String, String>> parentsUrl;

    public static CategoryResponseDTO fromCategoryWithParentsUrlList(Category category) {
        return new CategoryResponseDTO(
            "/categories/" + category.getId(),
            category.getName(),
            category.getDepth(),
            category.getParent() != null ? category.getParent().getId() : null,
            category.getParentsMapAsc()
        );
    }  // 응답 포맷이 깨지므로 사용 불가
}
