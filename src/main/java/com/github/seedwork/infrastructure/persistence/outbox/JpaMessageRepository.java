package com.github.seedwork.infrastructure.persistence.outbox;

import com.github.seedwork.infrastructure.outbox.Message;
import com.github.seedwork.infrastructure.persistence.JpaRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ConditionalOnMissingBean(JpaMessageRepository.class)
public interface JpaMessageRepository extends Repository<Message, Long>, JpaRepository<Message> {

  @Query(name = "Message.count", nativeQuery = true)
  int count(@Param("scheduled_at") Instant scheduledAt,
            @Param("min_requeue_count") int minRequeueCount,
            @Param("max_requeue_count") int maxRequeueCount);

  @Query(name = "Message.find", nativeQuery = true)
  Optional<Message> find(@Param("message_id") UUID messageId);

  @Query(name = "Message.findAll", nativeQuery = true)
  List<Message> findAll(@Param("scheduled_at") Instant scheduledAt,
                        @Param("min_requeue_count") int minRequeueCount,
                        @Param("max_requeue_count") int maxRequeueCount,
                        @Param("offset") long offset,
                        @Param("limit") int limit);

  @Modifying
  @Query(name = "Message.delete", nativeQuery = true)
  int delete(@Param("message_id") UUID messageId);

  @Modifying
  @Query(name = "Message.update", nativeQuery = true)
  int update(@Param("message_id") UUID messageId,
             @Param("scheduled_at") Instant scheduledAt,
             @Param("requeue_count") int requeueCount);
}
