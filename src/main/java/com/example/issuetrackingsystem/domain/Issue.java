package com.example.issuetrackingsystem.domain;

import com.example.issuetrackingsystem.domain.enums.IssuePriority;
import com.example.issuetrackingsystem.domain.enums.IssueStatus;
import com.example.issuetrackingsystem.domain.key.IssuePK;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicUpdate;

@Getter
@Entity
@Table(name = "issue")
@Builder
@DynamicUpdate
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Issue {

  @EmbeddedId
  private IssuePK id;

  @MapsId("projectId")
  @ManyToOne
  @JoinColumn(name = "project_id", referencedColumnName = "id", nullable = false)
  private Project project;

  @Column(name = "title", nullable = false)
  private String title;

  @Column(name = "description")
  private String description;

  @ManyToOne
  @JoinColumn(name = "reporter", referencedColumnName = "id", nullable = false)
  private Account reporter;

  @Column(name = "reported_date", nullable = false)
  private LocalDateTime reportedDate;

  @ManyToOne
  @JoinColumn(name = "assignee", referencedColumnName = "id", nullable = false)
  private Account assignee;

  @ManyToOne
  @JoinColumn(name = "fixer", referencedColumnName = "id", nullable = false)
  private Account fixer;

  @Enumerated(EnumType.STRING)
  @Column(name = "priority", nullable = false)
  @ColumnDefault("'MAJOR'")
  private IssuePriority priority;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  @ColumnDefault("'NEW'")
  private IssueStatus status;

  @Column(name = "closed_date", nullable = false)
  private LocalDateTime closedDate;

}
