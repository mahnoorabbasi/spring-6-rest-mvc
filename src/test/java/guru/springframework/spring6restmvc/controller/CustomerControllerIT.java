package guru.springframework.spring6restmvc.controller;

import guru.springframework.spring6restmvc.entities.Customer;
import guru.springframework.spring6restmvc.model.CustomerDTO;
import guru.springframework.spring6restmvc.repository.CustomerRepository;
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
class CustomerControllerIT {

    @Autowired
    CustomerRepository customerRepository;
    @Autowired
    CustomerController customerController;

    @Rollback
    @Transactional
    @Test
    void saveNewCustomer() {
        CustomerDTO customerDTO=CustomerDTO.builder()
                .name("mahn")
                .build();
        ResponseEntity responseEntity=customerController.handlePost(customerDTO);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(201));
        assertThat(responseEntity.getHeaders().getLocation()).isNotNull();
        String[] locations=responseEntity.getHeaders().getLocation().getPath().split("/");
        UUID savedUuid=UUID.fromString(locations[4]);


        assertThat(customerRepository.findById(savedUuid)).isNotNull();

    }

    @Test
    void testNotFoundCustomerById() {
        assertThrows(NotFoundException.class,()->
                customerController.getCustomerById(UUID.randomUUID())
        );
    }

    @Test
    void testGetCustomerById() {
        Customer customer= customerRepository.findAll().get(0);
        CustomerDTO customerDTO= customerController.getCustomerById(customer.getId());
        assertThat(customerDTO).isNotNull();
    }

    @Test
    void testListCustomers() {
        List<CustomerDTO> CustomerDTOList= customerController.listAllCustomers();
        assertThat(CustomerDTOList.size()).isEqualTo(3);
    }

    @Rollback
    @Transactional
    @Test
    void testEmptyListCustomers() {
        customerRepository.deleteAll();;
        List<CustomerDTO> CustomerDTOList= customerController.listAllCustomers();
        assertThat(CustomerDTOList.size()).isEqualTo(0);
    }
}