package com.example.issuetrackingsystem.domain;

import com.example.issuetrackingsystem.domain.enums.IssueKeyword;
import com.example.issuetrackingsystem.domain.enums.IssuePriority;
import com.example.issuetrackingsystem.domain.enums.IssueStatus;
import com.example.issuetrackingsystem.domain.key.IssuePK;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Getter
@Entity
@Table(name = "issue")
@Builder
@DynamicInsert
@DynamicUpdate
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class Issue {

  @EmbeddedId
  private IssuePK id;

  @MapsId("projectId")
  @ManyToOne
  @JoinColumn(name = "project_id", referencedColumnName = "id", nullable = false)
  private Project project;

  @Column(name = "title", nullable = false)
  private String title;

  @Column(name = "description", length = 2000)
  private String description;

  @Column(name = "keyword")
  private IssueKeyword keyword;

  @ManyToOne
  @JoinColumn(name = "reporter", referencedColumnName = "id", nullable = false)
  private Account reporter;

  @Column(name = "reported_date", nullable = false, insertable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
  private LocalDateTime reportedDate;

  @Column(name = "due_date", nullable = false, columnDefinition = "TIMESTAMP")
  private LocalDate dueDate;

  @ManyToOne
  @JoinColumn(name = "manager", referencedColumnName = "id")
  private Account manager;

  @ManyToOne
  @JoinColumn(name = "assignee", referencedColumnName = "id")
  private Account assignee;

  @ManyToOne
  @JoinColumn(name = "fixer", referencedColumnName = "id")
  private Account fixer;

  @Column(name = "priority", nullable = false)
  @ColumnDefault("2")
  private IssuePriority priority;

  @Column(name = "status", nullable = false)
  @ColumnDefault("0")
  private IssueStatus status;

  @Column(name = "closed_date", columnDefinition = "TIMESTAMP")
  private LocalDateTime closedDate;

}
