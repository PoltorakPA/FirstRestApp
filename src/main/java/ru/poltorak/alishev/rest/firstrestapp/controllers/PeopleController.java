package ru.poltorak.alishev.rest.firstrestapp.controllers;

import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.poltorak.alishev.rest.firstrestapp.dto.PersonDTO;
import ru.poltorak.alishev.rest.firstrestapp.models.Person;
import ru.poltorak.alishev.rest.firstrestapp.services.PeopleService;
import ru.poltorak.alishev.rest.firstrestapp.util.PersonErrorResponse;
import ru.poltorak.alishev.rest.firstrestapp.util.PersonNotCreatedException;
import ru.poltorak.alishev.rest.firstrestapp.util.PersonNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/people")
public class PeopleController {
    private final PeopleService peopleService;
    private final ModelMapper modelMapper;

    public PeopleController(PeopleService peopleService, ModelMapper modelMapper) {
        this.peopleService = peopleService;
        this.modelMapper = modelMapper;
    }


    @GetMapping
    public List<PersonDTO> getPeople() {
        return peopleService.findAll().stream().map(this::convertToPersonDTO)
                .collect(Collectors.toList());//jackson конвертирует эти объекты в JSON
    }

    @GetMapping("/{id}")
    public PersonDTO getPerson(@PathVariable("id") int id) {
        return convertToPersonDTO(peopleService.findOne(id));//jackson конвертирует в JSON
    }

    @ExceptionHandler
    private ResponseEntity<PersonErrorResponse> handlerException(PersonNotFoundException e) {
        PersonErrorResponse response = new PersonErrorResponse(
                "Person with this id wasn't found!",
                System.currentTimeMillis()
        );
        //В http ответе тело ответа (response) и статус в заголовке
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @PostMapping
    public ResponseEntity<HttpStatus> create(@RequestBody @Valid PersonDTO personDTO,
                                             BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder errorMessage = new StringBuilder();
            List<FieldError> errors = bindingResult.getFieldErrors();
            for (FieldError error : errors) {
                errorMessage.append(error.getField())
                        .append(" - ").append((error.getDefaultMessage()))
                        .append(";");
            }
            throw new PersonNotCreatedException(errorMessage.toString());
        }
        peopleService.save(convertToPerson(personDTO));
        return ResponseEntity.ok(HttpStatus.OK);
    }

    private Person convertToPerson(PersonDTO personDTO) {

        return modelMapper.map(personDTO, Person.class);


    }


    @ExceptionHandler
    private ResponseEntity<PersonErrorResponse> handlerException(PersonNotCreatedException e) {
        PersonErrorResponse response = new PersonErrorResponse(
                e.getMessage(),
                System.currentTimeMillis()
        );
        //В http ответе тело ответа (response) и статус в заголовке
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    private PersonDTO convertToPersonDTO(Person person) {
        return modelMapper.map(person, PersonDTO.class);
    }
}
