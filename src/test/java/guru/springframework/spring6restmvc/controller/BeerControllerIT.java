package guru.springframework.spring6restmvc.controller;

import guru.springframework.spring6restmvc.entities.Beer;
import guru.springframework.spring6restmvc.mappers.BeerMapper;
import guru.springframework.spring6restmvc.model.BeerDTO;
import guru.springframework.spring6restmvc.repository.BeerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class BeerControllerIT {

    @Autowired
    BeerRepository beerRepository;
    @Autowired
    BeerController beerController;
    @Autowired
    BeerMapper beerMapper;
    


    @Test
    void deleteBeerByIdNotFound() {
        assertThrows(NotFoundException.class,()->
                beerController.deleteById(UUID.randomUUID()));
    }

    @Rollback
    @Transactional
    @Test
    void deleteBeerByIdFound() {
        Beer beer=beerRepository.findAll().get(0);

        ResponseEntity responseEntity=beerController.deleteById(beer.getId());

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(204));
//        Beer foundBeer=beerRepository.findById(beer.getId()).get();
//        assertThat(foundBeer).isNull();
    }

    @Rollback
    @Transactional
    @Test
    void updateExistingBeer() {
        Beer beer = beerRepository.findAll().get(0);
        BeerDTO beerDTO = beerMapper.beerToBeerDto(beer);
        beerDTO.setId(null);
        beerDTO.setVersion(null);
        final String beerName = "UPDATED";
        beerDTO.setBeerName(beerName);

        ResponseEntity responseEntity = beerController.updateById(beer.getId(), beerDTO);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(204));

        Beer updatedBeer = beerRepository.findById(beer.getId()).get();
        assertThat(updatedBeer.getBeerName()).isEqualTo(beerName);
    }
    @Test
    void testNotFoundUpdateBeer() {
        assertThrows(NotFoundException.class,()->
                beerController.updateById(UUID.randomUUID(),BeerDTO.builder().build()));
    }

    @Rollback
    @Transactional
    @Test
    void testSaveNewBeer() {
        BeerDTO beerDTO=BeerDTO.builder()
                .beerName("ber")
                .build();

        ResponseEntity responseEntity=beerController.handlePost(beerDTO);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(201));
        assertThat(responseEntity.getHeaders().getLocation()).isNotNull();

        String[] locationSplit=responseEntity.getHeaders().getLocation().getPath().split("/");
        UUID savedUuid=UUID.fromString(locationSplit[4]);

        assertThat(beerRepository.findById(savedUuid)).isNotNull();

    }

    @Test
    void testNotFoundBeerById() {
        assertThrows(NotFoundException.class,()->
                beerController.getBeerById(UUID.randomUUID())
                );
    }

    @Test
    void testGetBeerById() {
        Beer beer=beerRepository.findAll().get(0);
        BeerDTO beerDTO=beerController.getBeerById(beer.getId());
        assertThat(beerDTO).isNotNull();
    }

    @Test
    void testListBeers() {
        List<BeerDTO> beerDTOList=beerController.listBeers();
        assertThat(beerDTOList.size()).isEqualTo(3);
    }

    @Rollback
    @Transactional
    @Test
    void testEmptyListBeers() {
        beerRepository.deleteAll();;
        List<BeerDTO> beerDTOList=beerController.listBeers();
        assertThat(beerDTOList.size()).isEqualTo(0);
    }
}