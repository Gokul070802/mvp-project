package com.mvp.mvp_backend.repository;

import com.mvp.mvp_backend.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    
    // Find by owner
    Page<Task> findByOwnerId(Long ownerId, Pageable pageable);
    
    // Find by owner and completed status
    Page<Task> findByOwnerIdAndCompleted(Long ownerId, boolean completed, Pageable pageable);
    
    // Search by title
    @Query("SELECT t FROM Task t WHERE t.owner.id = :ownerId AND LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Task> searchByTitleForOwner(@Param("ownerId") Long ownerId, @Param("search") String search, Pageable pageable);
    
    // Search and filter by completion status
    @Query("SELECT t FROM Task t WHERE t.owner.id = :ownerId AND LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')) AND t.completed = :completed")
    Page<Task> searchAndFilterByCompletion(@Param("ownerId") Long ownerId, @Param("search") String search, @Param("completed") boolean completed, Pageable pageable);
    
    // Count completed tasks by owner
    long countByOwnerIdAndCompleted(Long ownerId, boolean completed);
    
    // For backward compatibility (find all tasks - used in dashboard)
    List<Task> findByOwnerId(Long ownerId);
}
