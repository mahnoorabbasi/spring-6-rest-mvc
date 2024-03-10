package guru.springframework.spring6restmvc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.spring6restmvc.config.SpringSecConfig;
import guru.springframework.spring6restmvc.model.CustomerDTO;
import guru.springframework.spring6restmvc.services.CustomerService;
import guru.springframework.spring6restmvc.services.CustomerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import static org.mockito.ArgumentMatchers.any;
import static org.hamcrest.core.Is.*;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest(CustomerController.class)
@Import(SpringSecConfig.class)
public class CustomerControllerTest {

    public static final String USER_1 = "user1";
    public static final String PASSWORD = "password";
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper; //spring provided jackson object mapper with sensible defautls

    @MockBean
    CustomerService customerService;

    CustomerServiceImpl customerServiceImp;
    @Captor
    ArgumentCaptor<UUID> uuidArgumentCaptor;
    @Captor
    ArgumentCaptor<CustomerDTO> customerArgumentCaptor;
    @BeforeEach
    public void setBeerServiceImp()
    {
        customerServiceImp=new CustomerServiceImpl();//setting here so that for each test, its a new object
    }

    @Test
    void testPatchCustomer() throws Exception {
        CustomerDTO customer=customerServiceImp.getAllCustomers().get(0);

        Map<String, Object> customerMap=new HashMap<>();
        customerMap.put("name", "Updated name");
        mockMvc.perform(patch(CustomerController.CUSTOMER_PATH_ID,customer.getId())
                .accept(MediaType.APPLICATION_JSON)
                        .with(httpBasic(USER_1, PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customerMap)))
                .andExpect(status().isNoContent());

        verify(customerService).patchCustomerById(uuidArgumentCaptor.capture(), customerArgumentCaptor.capture());
        assertThat(customerMap.get("name")).isEqualTo(customerArgumentCaptor.getValue().getName());

    }

    @Test
    void testDeleteCustomer() throws Exception {
        CustomerDTO customer=customerServiceImp.getAllCustomers().get(0);

        given(customerService.deleteCustomerById(any())).willReturn(true);

        mockMvc.perform(delete(CustomerController.CUSTOMER_PATH_ID, customer.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .with(httpBasic(USER_1, PASSWORD)))
                .andExpect(status().isNoContent());
        verify(customerService).deleteCustomerById(uuidArgumentCaptor.capture());
        assertThat(customer.getId()).isEqualTo(uuidArgumentCaptor.getValue());
    }
    @Test
    void testUpdateCustomer() throws Exception {
        CustomerDTO customer=customerServiceImp.getAllCustomers().get(0);
        customer.setName("Updated Name");

        given(customerService.updateCustomerById(any(),any())).willReturn(Optional.of(customer));

        mockMvc.perform(put(CustomerController.CUSTOMER_PATH_ID,customer.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(httpBasic(USER_1, PASSWORD))
                .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isNoContent());
        verify(customerService).updateCustomerById(any(UUID.class), any(CustomerDTO.class));
    }
    @Test
    public void testSaveNewCustomer() throws Exception {
        CustomerDTO customer=customerServiceImp.getAllCustomers().get(0);
        customer.setVersion(null);
        customer.setId(null);

        given(customerService.saveNewCustomer(any(CustomerDTO.class))).willReturn(customerServiceImp.getAllCustomers().get(1));

        mockMvc.perform(post(CustomerController.CUSTOMER_PATH)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(httpBasic(USER_1, PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customer)))
//                .andExpect(jsonPath("$.id", is(beerServiceImp.listBeers().get(1).getId()))) //in post we are not returing any object
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));



    }
    @Test
    public void getCustomerList() throws Exception {

        given(customerService.getAllCustomers()).willReturn(customerServiceImp.getAllCustomers());

        mockMvc.perform(get(CustomerController.CUSTOMER_PATH)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(httpBasic(USER_1, PASSWORD)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()", is(3)));
    }

    @Test
    void getCustomerByIdNotFound() throws Exception {

        given(customerService.getCustomerById(any(UUID.class))).willReturn(Optional.empty());

        mockMvc.perform(get(CustomerController.CUSTOMER_PATH_ID, UUID.randomUUID())
                .with(httpBasic(USER_1, PASSWORD)))
                .andExpect(status().isNotFound());
    }
    @Test
    public void getCustomerById() throws Exception {
        CustomerDTO testCustomer=customerServiceImp.getAllCustomers().get(0);

        given(customerService.getCustomerById(testCustomer.getId())).willReturn(Optional.of(testCustomer));

        mockMvc.perform(get(CustomerController.CUSTOMER_PATH_ID,testCustomer.getId())
                .accept(MediaType.APPLICATION_JSON)
                .with(httpBasic(USER_1, PASSWORD)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testCustomer.getId().toString())))
                .andExpect(jsonPath("$.name", is(testCustomer.getName())));
    }

}
