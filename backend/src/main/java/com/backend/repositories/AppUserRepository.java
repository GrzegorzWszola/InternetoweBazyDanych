package com.backend.repositories;

import org.springframework.stereotype.Repository;
import com.backend.entities.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository 
public interface AppUserRepository extends JpaRepository<AppUser, Integer>{
    
}
