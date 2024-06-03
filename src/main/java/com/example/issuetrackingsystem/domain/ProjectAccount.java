package com.example.issuetrackingsystem.domain;

import com.example.issuetrackingsystem.domain.enums.ProjectAccountRole;
import com.example.issuetrackingsystem.domain.key.ProjectAccountPK;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Getter
@Entity
@Table(name = "project_account")
@Builder
@DynamicInsert
@DynamicUpdate
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class ProjectAccount {

  @EmbeddedId
  private ProjectAccountPK id;

  @MapsId("projectId")
  @ManyToOne
  @JoinColumn(name = "project_id", nullable = false)
  private Project project;

  @MapsId("accountId")
  @ManyToOne
  @JoinColumn(name = "account_id", nullable = false)
  private Account account;

  @Column(name = "role", nullable = false)
  private ProjectAccountRole role;

  @Column(name = "date", nullable = false, insertable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
  private LocalDateTime date;

}
