package com.project.payment.dao.entity;

import jakarta.persistence.PreRemove;

public class SoftDeleteListener {

    @PreRemove
    public void preventPhysicalDeletion(Object entity) {
        if (entity instanceof Deletable) {
            ((Deletable) entity).setDeleted(true);
        }
    }
}
