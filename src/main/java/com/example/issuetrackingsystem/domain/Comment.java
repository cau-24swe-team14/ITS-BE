package com.example.issuetrackingsystem.domain;

import com.example.issuetrackingsystem.domain.key.CommentPK;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Getter
@Entity
@Table(name = "comment")
@Builder
@DynamicInsert
@DynamicUpdate
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {

  @EmbeddedId
  private CommentPK id;

  @MapsId("issueId")
  @ManyToOne
  @JoinColumns({
      @JoinColumn(name = "issue_id", referencedColumnName = "id", nullable = false),
      @JoinColumn(name = "project_id", referencedColumnName = "project_id", nullable = false)
  })
  private Issue issue;

  @ManyToOne
  @JoinColumn(name = "account_id", referencedColumnName = "id", nullable = false)
  private Account account;

  @Column(name = "content", length = 2000, nullable = false)
  private String content;

  @Column(name = "date", nullable = false, insertable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
  private LocalDateTime date;

}
