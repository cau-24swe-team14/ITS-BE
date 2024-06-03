package com.example.issuetrackingsystem.domain.key;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class CommentPK implements Serializable {

  @Column(name = "id", nullable = false)
  private Long commentId;

  @Column(name = "issue_id", nullable = false)
  private IssuePK issueId;

}
