package guru.springframework.spring6restmvc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.spring6restmvc.model.Beer;
import guru.springframework.spring6restmvc.model.Customer;
import guru.springframework.spring6restmvc.services.BeerServiceImpl;
import guru.springframework.spring6restmvc.services.CustomerService;
import guru.springframework.spring6restmvc.services.CustomerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import static org.mockito.ArgumentMatchers.any;
import static org.hamcrest.core.Is.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest(CustomerController.class)
public class CustomerControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper; //spring provided jackson object mapper with sensible defautls

    @MockBean
    CustomerService customerService;

    CustomerServiceImpl customerServiceImp;
    @BeforeEach
    public void setBeerServiceImp()
    {
        customerServiceImp=new CustomerServiceImpl();//setting here so that for each test, its a new object
    }
    @Test
    void testDeleteCustomer() throws Exception {
        Customer customer=customerServiceImp.getAllCustomers().get(0);
        mockMvc.perform(delete("/api/v1/customer/" + customer.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        ArgumentCaptor<UUID> argumentCaptor=ArgumentCaptor.forClass(UUID.class);
        verify(customerService).deleteCustomerById(argumentCaptor.capture());
        assertThat(customer.getId()).isEqualTo(argumentCaptor.getValue());
    }
    @Test
    void testUpdateCustomer() throws Exception {
        Customer customer=customerServiceImp.getAllCustomers().get(0);
        customer.setName("Updated Name");

        mockMvc.perform(put("/api/v1/customer/"+customer.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isNoContent());
        verify(customerService).updateCustomerById(any(UUID.class), any(Customer.class));
    }
    @Test
    public void testSaveNewCustomer() throws Exception {
        Customer customer=customerServiceImp.getAllCustomers().get(0);
        customer.setVersion(null);
        customer.setId(null);

        given(customerService.saveNewCustomer(any(Customer.class))).willReturn(customerServiceImp.getAllCustomers().get(1));

        mockMvc.perform(post("/api/v1/customer")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customer)))
//                .andExpect(jsonPath("$.id", is(beerServiceImp.listBeers().get(1).getId()))) //in post we are not returing any object
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));



    }
    @Test
    public void getCustomerList() throws Exception {

        given(customerService.getAllCustomers()).willReturn(customerServiceImp.getAllCustomers());

        mockMvc.perform(get("/api/v1/customer")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()", is(3)));
    }

    @Test
    public void getCustomerById() throws Exception {
        Customer testCustomer=customerServiceImp.getAllCustomers().get(0);

        given(customerService.getCustomerById(testCustomer.getId())).willReturn(testCustomer);

        mockMvc.perform(get("/api/v1/customer/"+testCustomer.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testCustomer.getId().toString())))
                .andExpect(jsonPath("$.name", is(testCustomer.getName())));
    }

}
