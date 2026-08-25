package com.example.miniproject.repository.Homestay;

import com.example.miniproject.entity.Homestayowner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HomestayOwnerRepository extends JpaRepository<Homestayowner, String> {
    

     List<Homestayowner> findByAccountstatus(String accountstatus);
    List<Homestayowner> findByVerificationstatus(Boolean verificationstatus);
      // ค้นหาด้วย email
    @Query("SELECT o FROM Homestayowner o WHERE o.email = :email")
    Optional<Homestayowner> findByEmail(@Param("email") String email);

    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END " +
           "FROM Homestayowner o WHERE o.email = :email")
    boolean existsByEmail(@Param("email") String email);

    // ตรวจสอบว่า email มีอยู่แล้วหรือยัง (ยกเว้น owner ตัวเอง)
    @Query("SELECT COUNT(o) > 0 FROM Homestayowner o WHERE o.email = :email AND o.ownerid <> :ownerid")
    boolean existsByEmailAndNotId(@Param("email") String email, @Param("ownerid") String  ownerid);
 
    // ดึงเฉพาะ firstname, lastname, email, phone
    @Query("SELECT o FROM Homestayowner o WHERE o.ownerid = :ownerid")
    Optional<Homestayowner> findProfileById(@Param("ownerid") String  ownerid);
 
    // อัปเดตข้อมูลส่วนตัว (ไม่รวม password)
    @Modifying
    @Query("""
        UPDATE Homestayowner o
        SET o.firstname = :firstname,
            o.lastname  = :lastname,
            o.email     = :email,
            o.phone     = :phone
        WHERE o.ownerid = :ownerid
        """)
    int updateProfile(
        @Param("ownerid")    String     ownerid,
        @Param("firstname")  String firstname,
        @Param("lastname")   String lastname,
        @Param("email")      String email,
        @Param("phone")      String phone
    );
 
    // อัปเดตรหัสผ่าน
    @Modifying
    @Query("UPDATE Homestayowner o SET o.password = :newPassword WHERE o.ownerid = :ownerid")
    int updatePassword(
        @Param("ownerid")     String     ownerid,
        @Param("newPassword") String newPassword
    );
 
    // ดึง password เดิมเพื่อตรวจสอบ
    @Query("SELECT o.password FROM Homestayowner o WHERE o.ownerid = :ownerid")
    Optional<String> findPasswordById(@Param("ownerid") String  ownerid);

    @Query("""
    SELECT MAX(CAST(SUBSTRING(o.ownerid, 3) AS integer))
    FROM Homestayowner o
    WHERE o.ownerid LIKE 'OW%'
""")
Integer findMaxOwnerNumber();

}
