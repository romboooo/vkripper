package org.example.finance.jira;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JiraIssueRequest {

    private Long paymentId;
    private Long orderId;
    private Long userId;
    private String summary;
    private String description;
    private String errorCode;
}