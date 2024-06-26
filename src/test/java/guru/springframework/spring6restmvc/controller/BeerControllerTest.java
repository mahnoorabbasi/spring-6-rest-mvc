package guru.springframework.spring6restmvc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.spring6restmvc.config.SpringSecConfig;
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
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import static org.mockito.ArgumentMatchers.any;
import static org.hamcrest.core.Is.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

//@SpringBootTest
@WebMvcTest(BeerController.class)
@Import(SpringSecConfig.class)

class BeerControllerTest {

    public static final String USER_1 = "user1";
    public static final String PASSWORD = "password";
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
    public static final SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwtRequestPostProcessor =
            jwt().jwt(jwt -> {
                jwt.claims(claims -> {
                            claims.put("scope", "message-read");
                            claims.put("scope", "message-write");
                        })
                        .subject("messaging-client")
                        .notBefore(Instant.now().minusSeconds(5l));
            });
    @Test
    void testUpdateBeerNullRequiredParams() throws Exception {
        BeerDTO beerDTO= beerServiceImp.listBeers(null, null, Boolean.FALSE,0,25).getContent().get(0);
        beerDTO.setBeerName(null);
        beerDTO.setUpc("");
        beerDTO.setPrice(null);


        given(beerService.updateBeerById(any(UUID.class),any(BeerDTO.class))).willReturn(Optional.of(beerDTO));

        MvcResult mvcResult=mockMvc.perform(put(BeerController.BEER_PATH_ID, beerDTO.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beerDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(
                                /*httpBasic(USER_1, PASSWORD)*/
                                jwtRequestPostProcessor
                        ))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()", is(4)))
                .andReturn();

        System.out.println(mvcResult.getResponse().getContentAsString());


    }
    @Test
    void testCreateBeerNullRequiredParams() throws Exception {
      BeerDTO beerDTO= BeerDTO.builder().build();


        given(beerService.saveNewBeer(any(BeerDTO.class))).willReturn(beerServiceImp.listBeers(null, null, Boolean.FALSE,0,25)
                        .getContent()
                .get(1));

        MvcResult mvcResult=mockMvc.perform(post(BeerController.BEER_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(beerDTO))
                .contentType(MediaType.APPLICATION_JSON)
                                .with(/*httpBasic(USER_1, PASSWORD)*/jwtRequestPostProcessor))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()", is(6)))
                .andReturn();

        System.out.println(mvcResult.getResponse().getContentAsString());


    }

    @Test
    void testPatchBeer() throws Exception {
        BeerDTO beer=beerServiceImp.listBeers(null, null, Boolean.FALSE,0,25).getContent().get(0);

        Map<String, Object> beerMap=new HashMap<>();
        beerMap.put("beerName", "New updated");
        mockMvc.perform(patch(BeerController.BEER_PATH_ID,beer.getId())
                        .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(beerMap))
                                .with(/*httpBasic(USER_1, PASSWORD)*/jwtRequestPostProcessor))
                .andExpect(status().isNoContent());


        verify(beerService).patchBeerById(uuidArgumentCaptor.capture(), beerArgumentCaptor.capture());
        assertThat(beerMap.get("beerName")).isEqualTo(beerArgumentCaptor.getValue().getBeerName());


    }

    @Test
    void testDeleteBeer() throws Exception {
        BeerDTO beer=beerServiceImp.listBeers(null, null, Boolean.FALSE,0,25).getContent().get(0);
        given(beerService.deleteById(any())).willReturn(true);

        mockMvc.perform(delete(BeerController.BEER_PATH_ID , beer.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .with(/*httpBasic(USER_1, PASSWORD)*/jwtRequestPostProcessor))
                .andExpect(status().isNoContent());
        verify(beerService).deleteById(uuidArgumentCaptor.capture());
        assertThat(beer.getId()).isEqualTo(uuidArgumentCaptor.getValue());
    }
    @Test
    void testUpdateBeer() throws Exception {
        BeerDTO beer=beerServiceImp.listBeers(null, null, Boolean.FALSE,0,25).getContent().get(0);
        beer.setBeerName("Updated");

        given(beerService.updateBeerById(any(UUID.class),any(BeerDTO.class))).willReturn(Optional.of(beer));

        mockMvc.perform(put(BeerController.BEER_PATH_ID,beer.getId())
                .accept(MediaType.APPLICATION_JSON)
                        .with(/*httpBasic(USER_1, PASSWORD)*/jwtRequestPostProcessor)
                        .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(beer)))
                .andExpect(status().isNoContent());
        verify(beerService).updateBeerById(any(UUID.class), any(BeerDTO.class));
    }

    @Test
    public void testSaveNewBeer() throws Exception {
        BeerDTO beer=beerServiceImp.listBeers(null, null, Boolean.FALSE,0,25).getContent().get(0);
        beer.setVersion(null);
        beer.setId(null);

        given(beerService.saveNewBeer(any(BeerDTO.class))).willReturn(beerServiceImp.listBeers(null, null, Boolean.FALSE,0,25).getContent().get(1));

        mockMvc.perform(post(BeerController.BEER_PATH)
                .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(beer))
                                .with(/*httpBasic(USER_1, PASSWORD)*/jwtRequestPostProcessor))
//                .andExpect(jsonPath("$.id", is(beerServiceImp.listBeers().get(1).getId()))) //in post we are not returing any object
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));



    }
    @Test
    void getBeerList() throws Exception {
        given(beerService.listBeers(any(), any(),any(),any(),any()))
                .willReturn(( beerServiceImp.listBeers(null, null, Boolean.FALSE,0,2)));

        mockMvc.perform(get(BeerController.BEER_PATH)
                .accept(MediaType.APPLICATION_JSON)
                                .with(/*httpBasic(USER_1, PASSWORD)*/jwtRequestPostProcessor))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()", is(3)));
    }

    @Test
    void getBeerByIdNotFound() throws Exception {

        given(beerService.getBeerById(any(UUID.class))).willReturn(Optional.empty());

        mockMvc.perform(get(BeerController.BEER_PATH_ID, UUID.randomUUID())
                        .with(/*httpBasic(USER_1, PASSWORD)*/jwtRequestPostProcessor))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBeerById() throws Exception {
        BeerDTO testBeer = beerServiceImp.listBeers(null, null, Boolean.FALSE,0,25).getContent().get(0);

//        given(beerService.getBeerById(any(UUID.class))).willReturn(testBeer);
        given(beerService.getBeerById(testBeer.getId())).willReturn(Optional.of(testBeer));
        mockMvc.perform( get(BeerController.BEER_PATH_ID,testBeer.getId())
                .accept(MediaType.APPLICATION_JSON)
                        .with(/*httpBasic(USER_1, PASSWORD)*/jwtRequestPostProcessor))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testBeer.getId().toString())))
                .andExpect(jsonPath("$.beerName",is(testBeer.getBeerName())));
    }
}