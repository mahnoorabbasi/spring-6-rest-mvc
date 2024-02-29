package guru.springframework.spring6restmvc.controller;

import guru.springframework.spring6restmvc.entities.Customer;
import guru.springframework.spring6restmvc.model.CustomerDTO;
import guru.springframework.spring6restmvc.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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