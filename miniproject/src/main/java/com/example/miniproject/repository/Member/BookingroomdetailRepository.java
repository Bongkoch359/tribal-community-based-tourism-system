package com.example.miniproject.repository.Member;

import com.example.miniproject.entity.Bookingroomdetail;
import com.example.miniproject.entity.Bookingroomdetailid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingroomdetailRepository 
        extends JpaRepository<Bookingroomdetail, Bookingroomdetailid> {
}