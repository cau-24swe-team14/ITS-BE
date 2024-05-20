package com.example.issuetrackingsystem.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

@Getter
@Entity
@Table(name = "account")
@Builder
@DynamicUpdate
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account {

  @Id
  @Column(name = "id", nullable = false)
  private Long accountId;

  @Column(name = "username", length = 20, unique = true, nullable = false)
  private String username;

  @Column(name = "password", length = 30, nullable = false)
  private String password;

}
