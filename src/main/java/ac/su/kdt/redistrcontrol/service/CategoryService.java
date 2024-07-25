package ac.su.kdt.redistrcontrol.service;

import ac.su.kdt.redistrcontrol.domain.Category;
import ac.su.kdt.redistrcontrol.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }
}
