package ac.su.kdt.redistrcontrol.controller;

import ac.su.kdt.redistrcontrol.domain.*;
import ac.su.kdt.redistrcontrol.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryResponseDTO> createCategory(
        @RequestBody CategoryForm categoryForm
    ) {
        try {
            Category createdCategory = categoryService.createCategory(categoryForm.toEntity());
            return new ResponseEntity<>(CategoryResponseDTO.fromCategory(createdCategory), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
