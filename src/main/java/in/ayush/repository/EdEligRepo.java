package in.ayush.repository;

import java.io.Serializable;

import org.springframework.data.jpa.repository.JpaRepository;

import in.ayush.entity.EdEligDtls;

public interface EdEligRepo extends JpaRepository<EdEligDtls, Serializable> {

}
