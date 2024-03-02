package guru.springframework.spring6restmvc.controller.v2;

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.spring6restmvc.controller.NotFoundException;
import guru.springframework.spring6restmvc.entities.Beer;
import guru.springframework.spring6restmvc.mappers.BeerMapper;
import guru.springframework.spring6restmvc.model.BeerDTO;
import guru.springframework.spring6restmvc.model.BeerStyle;
import guru.springframework.spring6restmvc.repository.v2.BeerRepository;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class BeerControllerIT {

    @Autowired
    @Qualifier("beerRepositoryV2")
    BeerRepository beerRepositoryV2;
    @Autowired
    @Qualifier("beerControllerV2")
    BeerController beerControllerV2;
    @Autowired
    BeerMapper beerMapper;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    WebApplicationContext wac;//Need full springboot context here with all the repos/controllers etc

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc= MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    void testBeerListByAllParamsQueryShowInvPage() throws Exception {
        mockMvc.perform(get(BeerController.BEER_PATH)
                        .queryParam("beerStyle", BeerStyle.IPA.name())
                        .queryParam("beerName", "IPA")
                        .queryParam("showInventory", "FALSE")
                        .queryParam("pageNumber", "2")
                        .queryParam("pageSize", "50")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", is(50)))
                .andExpect(jsonPath("$.content[0].quantityOnHand").value(IsNull.nullValue()))
                .andExpect(status().isOk());

    }

    @Test
    void testBeerListByAllParamsQueryShowInvFalse() throws Exception {
        mockMvc.perform(get(guru.springframework.spring6restmvc.controller.BeerController.BEER_PATH)
                        .queryParam("beerStyle", BeerStyle.IPA.name())
                        .queryParam("beerName", "IPA")
                        .queryParam("showInventory", "FALSE")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()", is(310)))
                .andExpect(jsonPath("$.[0].quantityOnHand").value(IsNull.nullValue()))
                .andExpect(status().isOk());

    }
    @Test
    void testBeerListByAllParamsQueryShowInvTrue() throws Exception {
        mockMvc.perform(get(guru.springframework.spring6restmvc.controller.BeerController.BEER_PATH)
                        .queryParam("beerStyle", BeerStyle.IPA.name())
                        .queryParam("beerName", "IPA")
                        .queryParam("showInventory", "TRUE")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()", is(310)))
                .andExpect(jsonPath("$.[0].quantityOnHand").value(IsNull.notNullValue()))
                .andExpect(status().isOk());

    }
    @Test
    void testBeerListByStyleAndNameQuery() throws Exception {
        mockMvc.perform(get(guru.springframework.spring6restmvc.controller.BeerController.BEER_PATH)
                        .queryParam("beerStyle", BeerStyle.IPA.name())
                        .queryParam("beerName", "IPA")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()", is(310)))
                .andExpect(status().isOk());

    }
    @Test
    void testBeerListByStyleQuery() throws Exception {
        mockMvc.perform(get(guru.springframework.spring6restmvc.controller.BeerController.BEER_PATH)
                        .queryParam("beerStyle", BeerStyle.IPA.name())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()", is(548)))
                .andExpect(status().isOk());

    }

    @Test
    void testBeerListByNameQuery() throws Exception {
        mockMvc.perform(get(guru.springframework.spring6restmvc.controller.BeerController.BEER_PATH)
                        .queryParam("beerName", "IPA")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()", is(336)))
                .andExpect(status().isOk());

    }

    /***
     * The idea here is to test the patch operation where we
     * are not really performing the bean validation at controller level
     * so bad data gets passed onto the database layer
     * so we need a way to handle that particular exception
     * */
    @Test
    void testPatchBeerNameTooLong() throws Exception {
        Beer beer= beerRepositoryV2.findAll().get(0);

        Map<String, Object> beerMap=new HashMap<>();
        beerMap.put("beerName", "New updatedupdatedupdatedupdatedupdatedupdatedupdatedupdatedupdatedupdatedupdatedupdatedupdatedupdated");
        MvcResult mvcResult=mockMvc.perform(patch(BeerController.BEER_PATH_ID,beer.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beerMap)))
                .andExpect(status().isBadRequest())
                .andReturn();

        System.out.println(mvcResult.getResponse().getContentAsString());


    }

    @Test
    void deleteBeerByIdNotFound() {
        assertThrows(NotFoundException.class,()->
                beerControllerV2.deleteById(UUID.randomUUID()));
    }

    @Rollback
    @Transactional
    @Test
    void deleteBeerByIdFound() {
        Beer beer= beerRepositoryV2.findAll().get(0);

        ResponseEntity responseEntity= beerControllerV2.deleteById(beer.getId());

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(204));
//        Beer foundBeer=beerRepository.findById(beer.getId()).get();
//        assertThat(foundBeer).isNull();
    }

    @Rollback
    @Transactional
    @Test
    void updateExistingBeer() {
        Beer beer = beerRepositoryV2.findAll().get(0);
        BeerDTO beerDTO = beerMapper.beerToBeerDto(beer);
        beerDTO.setId(null);
        beerDTO.setVersion(null);
        final String beerName = "UPDATED";
        beerDTO.setBeerName(beerName);

        ResponseEntity responseEntity = beerControllerV2.updateById(beer.getId(), beerDTO);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(204));

        Beer updatedBeer = beerRepositoryV2.findById(beer.getId()).get();
        assertThat(updatedBeer.getBeerName()).isEqualTo(beerName);
    }
    @Test
    void testNotFoundUpdateBeer() {
        assertThrows(NotFoundException.class,()->
                beerControllerV2.updateById(UUID.randomUUID(),BeerDTO.builder().build()));
    }

    @Rollback
    @Transactional
    @Test
    void testSaveNewBeer() {
        BeerDTO beerDTO=BeerDTO.builder()
                .beerName("ber")
                .build();

        ResponseEntity responseEntity= beerControllerV2.handlePost(beerDTO);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(201));
        assertThat(responseEntity.getHeaders().getLocation()).isNotNull();

        String[] locationSplit=responseEntity.getHeaders().getLocation().getPath().split("/");
        UUID savedUuid=UUID.fromString(locationSplit[4]);

        assertThat(beerRepositoryV2.findById(savedUuid)).isNotNull();

    }

    @Test
    void testNotFoundBeerById() {
        assertThrows(NotFoundException.class,()->
                beerControllerV2.getBeerById(UUID.randomUUID())
                );
    }

    @Test
    void testGetBeerById() {
        Beer beer= beerRepositoryV2.findAll().get(0);
        BeerDTO beerDTO= beerControllerV2.getBeerById(beer.getId());
        assertThat(beerDTO).isNotNull();
    }

    @Test
    void testListBeers() {
        List<BeerDTO> beerDTOList= beerControllerV2.listBeers(null,null, Boolean.FALSE, 0,25).getContent();
        assertThat(beerDTOList.size()).isEqualTo(25);
    }

    @Rollback
    @Transactional
    @Test
    void testEmptyListBeers() {
        beerRepositoryV2.deleteAll();;
        List<BeerDTO> beerDTOList= beerControllerV2.listBeers(null,null, Boolean.FALSE,0,25).getContent();
        assertThat(beerDTOList.size()).isEqualTo(0);
    }
}