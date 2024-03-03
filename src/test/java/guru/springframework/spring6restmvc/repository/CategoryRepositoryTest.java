package guru.springframework.spring6restmvc.repository;

import guru.springframework.spring6restmvc.entities.Beer;
import guru.springframework.spring6restmvc.entities.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class CategoryRepositoryTest {

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    BeerRepository beerRepository;

    Beer testBeer;

    @BeforeEach
    void setUp() {
        testBeer=beerRepository.findAll().get(0);
    }

    @Test
    @Transactional
    void testBeerCategoryAddition() {
        Category savedCategory=categoryRepository.save(Category.builder()
                        .description("im cate")
                .build());

        testBeer.addCategory(savedCategory);
        beerRepository.save(testBeer);

        System.out.println(testBeer.getCategories());
        System.out.println(savedCategory.getBeers());
    }
}