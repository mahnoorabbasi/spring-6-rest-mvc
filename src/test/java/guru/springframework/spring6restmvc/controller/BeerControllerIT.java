package guru.springframework.spring6restmvc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.spring6restmvc.entities.Beer;
import guru.springframework.spring6restmvc.mappers.BeerMapper;
import guru.springframework.spring6restmvc.model.BeerDTO;
import guru.springframework.spring6restmvc.model.BeerStyle;
import guru.springframework.spring6restmvc.repository.BeerRepository;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
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
    BeerRepository beerRepository;
    @Autowired
    BeerController beerController;
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
        mockMvc.perform(get(BeerController.BEER_PATH)
                        .queryParam("beerName", "IPA")
                        .queryParam("beerStyle", BeerStyle.IPA.name())
                        .queryParam("showInventory", "true")
                        .queryParam("pageSize", "800")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", is(310)))
                .andExpect(jsonPath("$.content[0].quantityOnHand").value(IsNull.notNullValue()));

    }
    @Test
    void testBeerListByAllParamsQueryShowInvTrue() throws Exception {
        mockMvc.perform(get(BeerController.BEER_PATH)
                        .queryParam("beerStyle", BeerStyle.IPA.name())
                        .queryParam("beerName", "IPA")
                        .queryParam("showInventory", "TRUE")
                        .queryParam("pageSize", "800")
                        .queryParam("pageNumber", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.size()", is(310)))
                .andExpect(jsonPath("$.content[0].quantityOnHand").value(IsNull.notNullValue()))
                .andExpect(status().isOk());

    }
    @Test
    void testBeerListByStyleAndNameQuery() throws Exception {
        mockMvc.perform(get(BeerController.BEER_PATH)
                        .queryParam("beerStyle", BeerStyle.IPA.name())
                        .queryParam("beerName", "IPA")
                        .queryParam("pageSize", "800")
                        .queryParam("pageNumber", "1")

                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.size()", is(310)))
                .andExpect(status().isOk());

    }
    @Test
    void testBeerListByStyleQuery() throws Exception {
        mockMvc.perform(get(BeerController.BEER_PATH)
                        .queryParam("beerStyle", BeerStyle.IPA.name())
                        .queryParam("pageSize", "800")
                        .queryParam("pageNumber", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.size()", is(548)))
                .andExpect(status().isOk());

    }

    @Test
    void testBeerListByNameQuery() throws Exception {
        mockMvc.perform(get(BeerController.BEER_PATH)
                        .queryParam("beerName", "IPA")
                        .queryParam("pageSize", "380")

                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.size()", is(336)))
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
        Beer beer=beerRepository.findAll().get(0);

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
        List<BeerDTO> beerDTOList=beerController.listBeers(null,null, Boolean.FALSE,0,2413).getContent();
        assertThat(beerDTOList.size()).isEqualTo(1000);
    }

    @Rollback
    @Transactional
    @Test
    void testEmptyListBeers() {
        beerRepository.deleteAll();;
        List<BeerDTO> beerDTOList=beerController.listBeers(null,null, Boolean.FALSE,0,25).getContent();
        assertThat(beerDTOList.size()).isEqualTo(0);
    }
}