package guru.springframework.spring6restmvc.repository;

import guru.springframework.spring6restmvc.bootstrap.BootstrapData;
import guru.springframework.spring6restmvc.entities.Beer;
import guru.springframework.spring6restmvc.model.BeerStyle;
import guru.springframework.spring6restmvc.services.BeerCsvServiceImpl;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
@DataJpaTest
@Import({BootstrapData.class, BeerCsvServiceImpl.class})
class BeerRepositoryTest {

    @Autowired
    BeerRepository beerRepository;


    @Test
    void findAllByBeerNameIsLikeIgnoreCaseTest() {
        List<Beer> beerList=beerRepository.findAllByBeerNameIsLikeIgnoreCase("%IPA%",null).getContent();
        assertThat(beerList.size()).isEqualTo(336);
    }

    /**
     * Hibernate Persistence Context: When you perform a save operation using a JPA repository
     * like beerRepository.save(...), Hibernate doesn't necessarily execute the SQL insert statement
     * immediately. Instead, it might delay the insert until it's necessary, typically until the end
     * of the transaction or when you explicitly flush the persistence context. If you flush the
     * database explicitly, Hibernate will try to synchronize the in-memory changes with the database,
     * which can trigger constraint violations to be detected and exceptions to be thrown.*/
    @Test
    void testBeerRepository() {
        Beer savedBeer=beerRepository.save(Beer.builder()
                .beerName("I am new")
                .beerStyle(BeerStyle.LAGER)
                .upc("12324213213")
                .price(new BigDecimal("10"))
                .build());
        beerRepository.flush();
        assertThat(savedBeer.getBeerName()).isNotNull();
        assertThat(savedBeer.getId()).isNotNull();

        //Applying entity validation are needed because in patch we are not doing any
        //validation, so its necessary that our validaiton error still get caught
    }
    @Test
    void testBeerNameTooLongRepository() {
        assertThrows(ConstraintViolationException.class,()->{
                    Beer savedBeer=beerRepository.save(Beer.builder()
                            .beerName("I am new 123124325454423412312432545442341231243254544234123124325454423412312432545442341231243254544234")
                            .beerStyle(BeerStyle.LAGER)
                            .upc("12324213213")
                            .price(new BigDecimal("10"))
                            .build());
                    beerRepository.flush();
                }

        );


    }
}