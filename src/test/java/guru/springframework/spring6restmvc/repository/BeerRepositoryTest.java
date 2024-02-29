package guru.springframework.spring6restmvc.repository;

import guru.springframework.spring6restmvc.entities.Beer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
@DataJpaTest
class BeerRepositoryTest {

    @Autowired
    BeerRepository beerRepository;

    @Test
    void testBeerRepository() {
        Beer savedBeer=beerRepository.save(Beer.builder()
                .beerName("I am new")
                .build());
        assertThat(savedBeer.getBeerName()).isNotNull();
        assertThat(savedBeer.getId()).isNotNull();
    }
}