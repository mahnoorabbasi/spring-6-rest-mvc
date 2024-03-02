package guru.springframework.spring6restmvc.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class CustomErrorController {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity handleBindingErrors(MethodArgumentNotValidException exception){
//        List<String> errors=new ArrayList<>();
//        exception.getBindingResult().getAllErrors().stream().forEach(
//                x->        errors.add(x.getObjectName()+ x.getDefaultMessage())
//
//        );
        List<Map<String, String>> errors= exception.getFieldErrors().stream()
                .map(fieldError-> {
                    Map<String, String > errorMap=new HashMap<>();
                    errorMap.put(fieldError.getField(),fieldError.getDefaultMessage());
                    return errorMap;//using map so that we can have a json type format of the errors
                }).collect(Collectors.toList());
        return ResponseEntity.badRequest().body(errors);
    }
}
