package org.resource.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.RequestMethod;

@Entity
@Table(name = "BINARY_RESOURCE_IDS")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BinaryResourceModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private long resourceId;
    private String name;
    private String storageType;
    private RequestMethod method;

}
