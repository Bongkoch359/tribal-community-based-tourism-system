package com.example.miniproject.repository.Member;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.miniproject.entity.Activitypost;

public interface ActivitypostRepository extends JpaRepository<Activitypost, String> {
    List<Activitypost> findByTitleContainingIgnoreCase(String title);
    List<Activitypost> findByLocationContainingIgnoreCase(String location);
}