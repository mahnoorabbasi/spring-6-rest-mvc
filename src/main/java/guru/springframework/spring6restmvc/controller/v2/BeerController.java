//package guru.springframework.spring6restmvc.controller.v2;
//
//import guru.springframework.spring6restmvc.controller.NotFoundException;
//import guru.springframework.spring6restmvc.model.BeerDTO;
//import guru.springframework.spring6restmvc.model.BeerStyle;
//import guru.springframework.spring6restmvc.services.v2.BeerService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.context.annotation.Primary;
//import org.springframework.context.annotation.Profile;
//import org.springframework.data.domain.Page;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.validation.annotation.Validated;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.UUID;
//
//@Slf4j
//@RequiredArgsConstructor
//@RestController(value = "beerControllerV2")
////@Profile("v2")
//public class BeerController {
//
//    public static final String BEER_PATH = "/api/v2/beer";
//    public static final String BEER_PATH_ID = BEER_PATH + "/{beerId}";
//
//    private final BeerService beerService;
//
//    @PatchMapping(BEER_PATH_ID)
//    public ResponseEntity updateBeerPatchById(@PathVariable("beerId") UUID beerId, @RequestBody BeerDTO beer){
//
//        beerService.patchBeerById(beerId, beer);
//
//        return new ResponseEntity(HttpStatus.NO_CONTENT);
//    }
//
//    @DeleteMapping(BEER_PATH_ID)
//    public ResponseEntity deleteById(@PathVariable("beerId") UUID beerId){
//
//        if(!beerService.deleteById(beerId)){
//            throw new NotFoundException();
//        }
//
//        return new ResponseEntity(HttpStatus.NO_CONTENT);
//    }
//
//    @PutMapping(BEER_PATH_ID)
//    public ResponseEntity updateById(@PathVariable("beerId")UUID beerId, @Validated @RequestBody BeerDTO beer){
//
//        if(beerService.updateBeerById(beerId, beer).isEmpty()){
//            throw new NotFoundException();
//        }
//        return new ResponseEntity(HttpStatus.NO_CONTENT);
//    }
//
//    @PostMapping(BEER_PATH)
//    public ResponseEntity handlePost(@Valid @RequestBody BeerDTO beer){
//
//        BeerDTO savedBeer = beerService.saveNewBeer(beer);
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("Location", BEER_PATH + "/" + savedBeer.getId().toString());
//
//        return new ResponseEntity(headers, HttpStatus.CREATED);
//    }
//
//    @GetMapping(value = BEER_PATH)
//    public Page<BeerDTO> listBeers(@RequestParam(required = false) String beerName,
//                                   @RequestParam(required = false) BeerStyle beerStyle,
//                                   @RequestParam(required = false) Boolean showInventory,
//                                   @RequestParam(required = false) int pageNumber,
//                                   @RequestParam(required = false) int pageSize){
//        return beerService.listBeers(beerName, beerStyle, showInventory,pageNumber,pageSize);
//    }
//
//
//    @GetMapping(value = BEER_PATH_ID)
//    public BeerDTO getBeerById(@PathVariable("beerId") UUID beerId){
//
//        log.debug("Get Beer by Id - in controller");
//
//        return beerService.getBeerById(beerId).orElseThrow(NotFoundException::new);
//    }
//
//}
