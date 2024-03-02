package guru.springframework.spring6restmvc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.spring6restmvc.model.BeerDTO;
import guru.springframework.spring6restmvc.services.BeerService;
import guru.springframework.spring6restmvc.services.BeerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import static org.mockito.ArgumentMatchers.any;
import static org.hamcrest.core.Is.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

//@SpringBootTest
@WebMvcTest(BeerController.class)
class BeerControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper; //spring provided jackson object mapper with sensible defautls

    @MockBean
    BeerService beerService;

    BeerServiceImpl beerServiceImp;
    @Captor
    ArgumentCaptor<UUID> uuidArgumentCaptor;
    @Captor
    ArgumentCaptor<BeerDTO> beerArgumentCaptor;

    @BeforeEach
    public void setBeerServiceImp()
    {
        beerServiceImp=new BeerServiceImpl();//setting here so that for each test, its a new object
    }
    @Test
    void testUpdateBeerNullRequiredParams() throws Exception {
        BeerDTO beerDTO= beerServiceImp.listBeers(null, null, Boolean.FALSE).get(0);
        beerDTO.setBeerName(null);
        beerDTO.setUpc("");
        beerDTO.setPrice(null);


        given(beerService.updateBeerById(any(UUID.class),any(BeerDTO.class))).willReturn(Optional.of(beerDTO));

        MvcResult mvcResult=mockMvc.perform(put(BeerController.BEER_PATH_ID, beerDTO.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beerDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()", is(4)))
                .andReturn();

        System.out.println(mvcResult.getResponse().getContentAsString());


    }
    @Test
    void testCreateBeerNullRequiredParams() throws Exception {
      BeerDTO beerDTO= BeerDTO.builder().build();


        given(beerService.saveNewBeer(any(BeerDTO.class))).willReturn(beerServiceImp.listBeers(null, null, Boolean.FALSE).get(1));

        MvcResult mvcResult=mockMvc.perform(post(BeerController.BEER_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(beerDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()", is(6)))
                .andReturn();

        System.out.println(mvcResult.getResponse().getContentAsString());


    }

    @Test
    void testPatchBeer() throws Exception {
        BeerDTO beer=beerServiceImp.listBeers(null, null, Boolean.FALSE).get(0);

        Map<String, Object> beerMap=new HashMap<>();
        beerMap.put("beerName", "New updated");
        mockMvc.perform(patch(BeerController.BEER_PATH_ID,beer.getId())
                        .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(beerMap)))
                .andExpect(status().isNoContent());


        verify(beerService).patchBeerById(uuidArgumentCaptor.capture(), beerArgumentCaptor.capture());
        assertThat(beerMap.get("beerName")).isEqualTo(beerArgumentCaptor.getValue().getBeerName());


    }

    @Test
    void testDeleteBeer() throws Exception {
        BeerDTO beer=beerServiceImp.listBeers(null, null, Boolean.FALSE).get(0);
        given(beerService.deleteById(any())).willReturn(true);

        mockMvc.perform(delete(BeerController.BEER_PATH_ID , beer.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        verify(beerService).deleteById(uuidArgumentCaptor.capture());
        assertThat(beer.getId()).isEqualTo(uuidArgumentCaptor.getValue());
    }
    @Test
    void testUpdateBeer() throws Exception {
        BeerDTO beer=beerServiceImp.listBeers(null, null, Boolean.FALSE).get(0);
        beer.setBeerName("Updated");

        given(beerService.updateBeerById(any(UUID.class),any(BeerDTO.class))).willReturn(Optional.of(beer));

        mockMvc.perform(put(BeerController.BEER_PATH_ID,beer.getId())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(beer)))
                .andExpect(status().isNoContent());
        verify(beerService).updateBeerById(any(UUID.class), any(BeerDTO.class));
    }

    @Test
    public void testSaveNewBeer() throws Exception {
        BeerDTO beer=beerServiceImp.listBeers(null, null, Boolean.FALSE).get(0);
        beer.setVersion(null);
        beer.setId(null);

        given(beerService.saveNewBeer(any(BeerDTO.class))).willReturn(beerServiceImp.listBeers(null, null, Boolean.FALSE).get(1));

        mockMvc.perform(post(BeerController.BEER_PATH)
                .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(beer)))
//                .andExpect(jsonPath("$.id", is(beerServiceImp.listBeers().get(1).getId()))) //in post we are not returing any object
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));



    }
    @Test
    void getBeerList() throws Exception {
        given(beerService.listBeers(any(), any(),any())).willReturn(beerServiceImp.listBeers(null, null, Boolean.FALSE));

        mockMvc.perform(get(BeerController.BEER_PATH)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()", is(3)));
    }

    @Test
    void getBeerByIdNotFound() throws Exception {

        given(beerService.getBeerById(any(UUID.class))).willReturn(Optional.empty());

        mockMvc.perform(get(BeerController.BEER_PATH_ID, UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBeerById() throws Exception {
        BeerDTO testBeer = beerServiceImp.listBeers(null, null, Boolean.FALSE).get(0);

//        given(beerService.getBeerById(any(UUID.class))).willReturn(testBeer);
        given(beerService.getBeerById(testBeer.getId())).willReturn(Optional.of(testBeer));
        mockMvc.perform( get(BeerController.BEER_PATH_ID,testBeer.getId())
                .accept(MediaType.APPLICATION_JSON) )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testBeer.getId().toString())))
                .andExpect(jsonPath("$.beerName",is(testBeer.getBeerName())));
    }
}