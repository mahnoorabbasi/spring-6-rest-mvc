package guru.springframework.spring6restmvc.repository;

import guru.springframework.spring6restmvc.entities.*;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BeerOrderRepositoryTest {
    @Autowired
    BeerOrderRepository beerOrderRepository;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    BeerRepository beerRepository;

    Customer testCustomer;
    Beer testBeer;

    @BeforeEach
    void setUp() {
        testCustomer = customerRepository.findAll().get(0);
        testBeer = beerRepository.findAll().get(0);
    }

   /* @Test
    void testBeerOrdersButCustomerBeerOrderNotFound() {
        BeerOrder beerOrder= BeerOrder.builder()
                .customerRef("i am cust")
                .customer(testCustomer)
                .build();

        BeerOrder savedBeerOrder=beerOrderRepository.save(beerOrder);// this will save the beer order and set
        //customer relation to it, but in customer object itself, beer order will not be present, and would have lazy
        //init error. That is because we have not set it manually, and jpa have not really persisted the object to
        //db yet. So either we could force this persistence using saveAndFlush,
        // or we could set the contxt as Transactional, then when we try to get the lazy loaded obj we will get it
        //as all the operations will be happening in the same transaction context
        //Transactional - uses two queries to get the data
        //Better approach is to use JPQL @Query(select ....left outer join ...) List<T> findAllC();

        assertThrows(LazyInitializationException.class, ()->{
                    System.out.println(savedBeerOrder.getCustomer().getBeerOrders());
                }
               );

    }*/
    @Test
    @Transactional
    void testBeerOrdersButCustomerBeerOrderFound() {
        BeerOrderShipment beerOrderShipment=BeerOrderShipment.builder().build();
        BeerOrder beerOrder= BeerOrder.builder()
                .customerRef("i am cust")
                .customer(testCustomer)
                .beerOrderShipment(beerOrderShipment)
                .build();

        BeerOrder savedBeerOrder=beerOrderRepository.save(beerOrder);// this will save the beer order and set

//        assertThrows(LazyInitializationException.class, ()->{
                   assertThat(savedBeerOrder.getCustomer().getBeerOrders()).isNotNull();
//                }
//        );

    }
  /*  @Test
    void testBeerOrdersButCustomerBeerOrderSaveFlushFound() {
        BeerOrder beerOrder= BeerOrder.builder()
                .customerRef("i am cust")
                .customer(testCustomer)
                .build();

        BeerOrder savedBeerOrder=beerOrderRepository.saveAndFlush(beerOrder);// this will save the beer order and set

//        assertThrows(LazyInitializationException.class, ()->{
        assertThat(savedBeerOrder.getCustomer().getBeerOrders()).isNotNull();
//                }
//        );

    }*/

    @Test
    @Transactional
    void testBeerOrdersButCustomerBeerFoundAfterAssociatedMtehods() {
        BeerOrder beerOrder= BeerOrder.builder()
                .customerRef("i am cust")
                .customer(testCustomer)
                .beerOrderShipment(BeerOrderShipment.builder()
                        .trackingNumber("1234")
                        .build())
                .build();

        BeerOrder savedBeerOrder=beerOrderRepository.save(beerOrder);
        System.out.println(savedBeerOrder.getCustomer().getBeerOrders());
        System.out.println(savedBeerOrder.getBeerOrderShipment());
        System.out.println(savedBeerOrder.getBeerOrderShipment().getBeerOrder().getCustomerRef());

        assertThat(savedBeerOrder.getCustomer().getBeerOrders()).isNotNull();


    }



}