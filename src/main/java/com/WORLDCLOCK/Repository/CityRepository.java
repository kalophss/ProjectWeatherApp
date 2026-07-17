package com.WORLDCLOCK.Repository;
import com.WORLDCLOCK.model.CityInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface CityRepository extends JpaRepository<CityInfo,Long> {
}
