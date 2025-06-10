package com.example.utils;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.List;

@Component
public class ArrayUtils<E> {

    public Boolean contains(E[] arr,E element){
        List<E> es = Arrays.asList(arr);
        return es.contains(element);
    }
}
