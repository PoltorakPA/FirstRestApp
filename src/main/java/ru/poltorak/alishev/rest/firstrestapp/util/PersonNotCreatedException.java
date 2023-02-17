package ru.poltorak.alishev.rest.firstrestapp.util;

public class PersonNotCreatedException extends RuntimeException{
    public PersonNotCreatedException(String msg){
        super(msg);
    }

}
