package ac.su.kdt.redistrcontrol.repository;

import ac.su.kdt.redistrcontrol.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}
