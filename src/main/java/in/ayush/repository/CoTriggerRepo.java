package in.ayush.repository;

import java.io.Serializable;

import org.springframework.data.jpa.repository.JpaRepository;

import in.ayush.entity.CoTriggerEntity;

public interface CoTriggerRepo extends JpaRepository<CoTriggerEntity, Serializable> {

}
