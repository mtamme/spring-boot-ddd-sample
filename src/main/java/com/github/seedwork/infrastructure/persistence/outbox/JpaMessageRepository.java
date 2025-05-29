package com.github.seedwork.infrastructure.persistence.outbox;

import com.github.seedwork.infrastructure.outbox.Message;
import com.github.seedwork.infrastructure.outbox.MessageCounts;
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
public interface JpaMessageRepository extends JpaMessageEnqueuer, Repository<Message, Long> {

  @NativeQuery(name = "Message.count")
  MessageCounts count(@Param("max_attempt_count") int maxAttemptCount,
                      @Param("counted_at") Instant countedAt);

  @NativeQuery(name = "Message.peek")
  Optional<Message> peek(@Param("sequence_number") Long sequenceNumber);

  @NativeQuery(name = "Message.peekAll")
  List<Message> peekAll(@Param("offset") long offset, @Param("limit") int limit);

  @NativeQuery(name = "Message.lockAllNextActive")
  @Modifying
  int lockAllNextActive(@Param("available_at") Instant availableAt,
                        @Param("lock_id") UUID lockId,
                        @Param("limit") int limit,
                        @Param("max_attempt_count") int maxAttemptCount,
                        @Param("locked_at") Instant lockedAt);

  @NativeQuery(name = "Message.lockAllNextFailed")
  @Modifying
  int lockAllNextFailed(@Param("available_at") Instant availableAt,
                        @Param("lock_id") UUID lockId,
                        @Param("limit") int limit,
                        @Param("max_attempt_count") int maxAttemptCount,
                        @Param("locked_at") Instant lockedAt);

  @NativeQuery(name = "Message.peekAllLocked")
  List<Message> peekAllLocked(@Param("lock_id") UUID lockId,
                              @Param("peeked_at") Instant peekedAt);

  @NativeQuery(name = "Message.requeueLocked")
  @Modifying
  int requeueLocked(@Param("sequence_number") Long sequenceNumber,
                    @Param("lock_id") UUID lockId,
                    @Param("requeued_at") Instant requeuedAt);

  @NativeQuery(name = "Message.requeueAllLocked")
  @Modifying
  int requeueAllLocked(@Param("sequence_numbers") List<Long> sequenceNumbers,
                       @Param("lock_id") UUID lockId,
                       @Param("requeued_at") Instant requeuedAt);

  @NativeQuery(name = "Message.dequeueLocked")
  @Modifying
  int dequeueLocked(@Param("sequence_number") Long sequenceNumber,
                    @Param("lock_id") UUID lockId,
                    @Param("dequeued_at") Instant dequeuedAt);

  @NativeQuery(name = "Message.dequeueAllLocked")
  @Modifying
  int dequeueAllLocked(@Param("sequence_numbers") List<Long> sequenceNumbers,
                       @Param("lock_id") UUID lockId,
                       @Param("dequeued_at") Instant dequeuedAt);
}
