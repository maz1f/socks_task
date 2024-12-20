package org.example.sockstask.repository.custom;

import org.example.sockstask.entity.Sock;
import org.example.sockstask.util.Comparison;

import java.util.List;

public interface SockRepo {

    List<Sock> findAllWithFilters(String color, Comparison comparison, List<Float> cottonPercentage);

}
