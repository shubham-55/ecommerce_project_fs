package com.ecommerce.project.controller;

import com.ecommerce.project.config.AppConstants;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.payload.CategoryDTO;
import com.ecommerce.project.payload.CategoryResponse;
import com.ecommerce.project.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api")
public class CategoryController {

 private CategoryService categoryService;

 @GetMapping("/echo")
 public ResponseEntity<String> echoMessage(@RequestParam(name = "message") String message){
     return new ResponseEntity<>("Echomessage:" + message, HttpStatus.OK);
 }

 public CategoryController(CategoryService categoryService){
     this.categoryService = categoryService;
 }

    @GetMapping("/public/categories")
    public ResponseEntity<CategoryResponse> getAllCategoryies(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE) Integer pageSize,
            @RequestParam(name = "sortBy",defaultValue = AppConstants.SORT_BY) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIRECTION) String sortOrder)
    {
        CategoryResponse categoryResponse = categoryService.getAllCategories(pageNumber,pageSize,sortBy,sortOrder);
        return ResponseEntity.ok(categoryResponse);
    }

    @PostMapping("/public/categories")
    public ResponseEntity<CategoryDTO> createCategory(@RequestBody CategoryDTO categoryDTO) {
       CategoryDTO savedCategoryDTO =  categoryService.createCategory(categoryDTO);
        return  new ResponseEntity<>(savedCategoryDTO, HttpStatus.CREATED);
    }
    @DeleteMapping("/admin/categories/{categoryId}")
    public ResponseEntity<CategoryDTO> deleteCategory(@PathVariable Long categoryId){
         CategoryDTO status = categoryService.deleteCategory(categoryId);
       //  return  new ResponseEntity<>(status, HttpStatus.OK);  other way also good
         return ResponseEntity.ok(status);

    }

    @PutMapping("/admin/categories/{categoryId}")
    //@RequestMapping(value = "/public/categories{categoryId}", method = RequestMethod.PUT)
    public ResponseEntity<CategoryDTO> updateCategory(@RequestBody CategoryDTO categoryDTO, @PathVariable Long categoryId){
        CategoryDTO saveCategoryDTO = categoryService.updateCategory(categoryDTO, categoryId);
        return new ResponseEntity<>(saveCategoryDTO , HttpStatus.OK);

    }
}
