package com.example.miniproject.repository.Member;

import com.example.miniproject.entity.Bookingtourdetail;
import com.example.miniproject.entity.Bookingtourdetailid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingtourdetailRepository 
        extends JpaRepository<Bookingtourdetail, Bookingtourdetailid> {
}