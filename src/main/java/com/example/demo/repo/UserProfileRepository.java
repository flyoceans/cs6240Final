package com.example.demo.repo;

import com.example.demo.entity.UserProfile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;


public interface UserProfileRepository extends CrudRepository<UserProfile, Long> {


    @Query(value = "select * from user_profile where id  in ?1 order by pr desc", nativeQuery = true)
    List<UserProfile> findUserProfileByIds(@Param("ids") Set<Long> ids);

    @Query(value = "select * from user_profile where id  in ?1 order by pr desc limit 3", nativeQuery = true)
    List<UserProfile> findUserProfileByIdsLimit3(@Param("ids") List<Long> ids);

}
