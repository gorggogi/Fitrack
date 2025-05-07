package com.app.fitrack.repository;

import com.app.fitrack.model.Goal;
import com.app.fitrack.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {
    List<Goal> findByUserOrderByTargetDateDesc(User user);
    List<Goal> findByUserAndStatus(User user, String status);
    List<Goal> findByUserAndGoalType(User user, String goalType);
    List<Goal> findByUserAndArchivedFalse(User user);
} 