package ai.lab.inlive.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@Table(name = "acc_search_request_dictionary")
public class AccSearchRequestDictionary extends AbstractEntity<Long> {
    @ManyToOne(optional = false)
    @JoinColumn(name = "acc_s_r_id")
    private AccSearchRequest searchRequest;

    @ManyToOne(optional = false)
    @JoinColumn(name = "dict_id")
    private Dictionary dictionary;
}