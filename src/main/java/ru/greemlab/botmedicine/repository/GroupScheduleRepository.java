package ru.greemlab.botmedicine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.greemlab.botmedicine.entity.GroupSchedule;

@Repository
public interface GroupScheduleRepository extends JpaRepository<GroupSchedule, Long> {
}
