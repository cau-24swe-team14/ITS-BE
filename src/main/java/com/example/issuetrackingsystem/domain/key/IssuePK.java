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
public class IssuePK implements Serializable {

  @Column(name = "project_id", nullable = false)
  private Long projectId;

  @Column(name = "id", nullable = false)
  private Long issueId;

}
