package org.example.jira.adapter.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JiraIssueRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long paymentId;
    private Long orderId;
    private Long userId;
    private String summary;
    private String description;
    private String errorCode;
}
