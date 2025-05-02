package org.novize.api.repository;


import org.novize.api.enums.RoleEnum;
import org.novize.api.model.Role;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface RoleRepository extends CrudRepository<Role, String> {
    Optional<Role> findByName(RoleEnum name);
}