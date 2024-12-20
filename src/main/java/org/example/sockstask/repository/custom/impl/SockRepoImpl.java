package org.example.sockstask.repository.custom.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.example.sockstask.entity.Sock;
import org.example.sockstask.repository.custom.SockRepo;
import org.example.sockstask.util.Comparison;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class SockRepoImpl implements SockRepo {

    @PersistenceContext
    private final EntityManager entityManager;

    public List<Sock> findAllWithFilters(String color, Comparison comparison, List<Float> cottonPercentage) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Sock> cq = cb.createQuery(Sock.class);
        Root<Sock> root = cq.from(Sock.class);

        List<Predicate> predicates = new ArrayList<>();
        if (color != null) {
            predicates.add(cb.equal(root.get("color"), color));
        }
        if (cottonPercentage != null) {
            predicates.add(
                    switch (comparison) {
                        case moreThan -> cb.greaterThan(root.get("cottonPercentage"), cottonPercentage.get(0));
                        case lessThan -> cb.lessThan(root.get("cottonPercentage"), cottonPercentage.get(0));
                        case equal -> cb.equal(root.get("cottonPercentage"), cottonPercentage.get(0));
                        case between -> cb.between(root.get("cottonPercentage"), cottonPercentage.get(0), cottonPercentage.get(1));
                    }
            );
        }
        return entityManager.createQuery(
                cq.where(predicates.toArray(Predicate[]::new))
        ).getResultList();
    }

}
