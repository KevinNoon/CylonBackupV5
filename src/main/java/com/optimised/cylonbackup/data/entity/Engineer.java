package com.optimised.cylonbackup.data.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Entity
@Data
@Table
public class Engineer {
    @Id
  @GeneratedValue(generator = "engineer_id_seq", strategy = GenerationType.SEQUENCE)
    //@GeneratedValue(strategy = GenerationType.AUTO)
    @SequenceGenerator(
        name = "engineer_id_seq",
        sequenceName = "user_id_seq",
        allocationSize = 50,
        initialValue = 101
    )
    private Long id;
    @Size(min = 3, max = 24)
    private String forename;
    @Size(min = 3, max = 24)
    private String lastname;
    @NotEmpty
    private String email;
//    @OneToMany(mappedBy = "engineer", fetch = FetchType.EAGER)
//    private Set<Site> site;

    public String getFullName() {
        return forename + " " + lastname;
    }
}
