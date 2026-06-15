package br.com.helpdesk.services;

import br.com.helpdesk.dtos.AuditResponse;
import br.com.helpdesk.entities.Audit;
import br.com.helpdesk.entities.User;
import br.com.helpdesk.enums.AuditEntityType;
import br.com.helpdesk.repositories.AuditRepository;
import br.com.helpdesk.utils.DtoMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditService {
    private final AuditRepository auditRepository;
    private final ObjectMapper objectMapper;

    public AuditService(AuditRepository auditRepository, ObjectMapper objectMapper) {
        this.auditRepository = auditRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void record(User actor, String action, AuditEntityType entityType, Long entityId, Object oldValue, Object newValue) {
        if (actor == null) {
            return;
        }

        Audit audit = new Audit();
        audit.setUser(actor);
        audit.setAction(action);
        audit.setEntityType(entityType.getCode());
        audit.setEntityId(entityId);
        audit.setOldValue(toJson(oldValue));
        audit.setNewValue(toJson(newValue));
        auditRepository.saveAndFlush(audit);
    }

    @Transactional(readOnly = true)
    public List<AuditResponse> listAll() {
        return auditRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(DtoMapper::toAuditResponse)
                .toList();
    }

    private JsonNode toJson(Object value) {
        return value == null ? null : objectMapper.valueToTree(value);
    }
}
