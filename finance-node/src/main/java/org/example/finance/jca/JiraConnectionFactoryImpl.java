package org.example.finance.jca;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JiraConnectionFactoryImpl implements JiraConnectionFactory {

    private final JiraManagedConnectionFactory managedConnectionFactory;

    @Override
    public JiraConnection getConnection() {
        return managedConnectionFactory.createManagedConnection();
    }
}