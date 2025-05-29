package com.github.seedwork.infrastructure.persistence.outbox;

import com.github.seedwork.infrastructure.outbox.Message;
import com.github.seedwork.infrastructure.persistence.JpaRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ConditionalOnBean(JpaOutboxConfiguration.class)
public interface JpaMessageRepository extends Repository<Message, Long>, JpaRepository<Message> {

  @NativeQuery(name = "Message.count")
  int count(@Param("scheduled_at") Instant scheduledAt,
            @Param("min_requeue_count") int minRequeueCount,
            @Param("max_requeue_count") int maxRequeueCount);

  @NativeQuery(name = "Message.find")
  Optional<Message> find(@Param("message_id") UUID messageId);

  @NativeQuery(name = "Message.findAll")
  List<Message> findAll(@Param("scheduled_at") Instant scheduledAt,
                        @Param("min_requeue_count") int minRequeueCount,
                        @Param("max_requeue_count") int maxRequeueCount,
                        @Param("offset") long offset,
                        @Param("limit") int limit);

  @NativeQuery(name = "Message.update")
  @Modifying
  int update(@Param("message_id") UUID messageId,
             @Param("scheduled_at") Instant scheduledAt,
             @Param("requeue_count") int requeueCount);

  @NativeQuery(name = "Message.delete")
  @Modifying
  int delete(@Param("message_id") UUID messageId);
}
