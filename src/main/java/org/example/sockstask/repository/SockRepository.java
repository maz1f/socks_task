package org.example.sockstask.repository;

import org.example.sockstask.entity.Sock;
import org.example.sockstask.repository.custom.SockRepo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SockRepository extends JpaRepository<Sock, Long>, SockRepo {

    Optional<Sock> findByColorAndCottonPercentage(String color, float cottonPercentage);

}
