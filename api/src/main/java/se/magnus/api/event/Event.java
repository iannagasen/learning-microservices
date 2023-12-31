package se.magnus.api.event;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer;

import lombok.Getter;

@Getter
public class Event<K, T> {
  
  public enum Type {
    CREATE,
    DELETE
  }

  private final Type eventType;
  private final K key;
  private final T data;
  private final ZonedDateTime eventCreatedAt;

  public Event() {
    this.eventType = null;
    this.key = null;
    this.data = null;
    this.eventCreatedAt = null;
  }

  public Event(Type eventType, K key, T data) {
    this.eventType = eventType;
    this.key = key;
    this.data = data;
    this.eventCreatedAt = ZonedDateTime.now();
  }

  @JsonSerialize(using = ZonedDateTimeSerializer.class)
  public ZonedDateTime getEventCreatedAt() {
    return eventCreatedAt;
  }

}
